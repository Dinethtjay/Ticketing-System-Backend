package com.iit.TicketingSystem.service;

import com.iit.TicketingSystem.websocket.LogWebSocketHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TicketPool {
    private final List<String> tickets;
    private final int maxCapacity;
    private final List<Thread> vendorThreads = new ArrayList<>();
    private final List<Thread> customerThreads = new ArrayList<>();
    private final LogWebSocketHandler logWebSocketHandler;
    private boolean running = false; // Track if the system is active

    public TicketPool(int initialTickets, int maxCapacity, LogWebSocketHandler logWebSocketHandler) {
        this.tickets = Collections.synchronizedList(new ArrayList<>());
        this.maxCapacity = maxCapacity;
        this.logWebSocketHandler = logWebSocketHandler;
        for (int i = 0; i < initialTickets; i++) {
            tickets.add("Ticket: " + (i + 1));
        }
    }

    public synchronized void addTickets(int count, String threadName) {
        while (tickets.size() + count > maxCapacity) {
            try {
                logWebSocketHandler.addLog(threadName + ": Max capacity reached. Waiting to add tickets...");
                System.out.println(threadName + ": Max capacity reached. Waiting to add tickets...");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        for (int i = 0; i < count; i++) {
            tickets.add("Ticket: " + (tickets.size() + 1));
        }
        logWebSocketHandler.addLog(threadName + " added " + count + " tickets. Total ticket count: " + tickets.size());
        System.out.println(threadName + " added " + count + " tickets. Total ticket count: " + tickets.size());
        notifyAll();
    }

    public synchronized void removeTickets(int count, String threadName) {
        while (tickets.isEmpty()) {
            try {
                logWebSocketHandler.addLog(threadName + ": No tickets available. Waiting...");
                System.out.println(threadName + ": No tickets available. Waiting...");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        for (int i = 0; i < count && !tickets.isEmpty(); i++) {
            tickets.remove(0);
        }
        logWebSocketHandler.addLog(threadName + " purchased " + count + " ticket(s). Remaining tickets: " + tickets.size());
        System.out.println(threadName + " purchased " + count + " ticket(s). Remaining tickets: " + tickets.size());
        notifyAll();
    }

    public synchronized int getTicketCount() {
        return tickets.size();
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public void startTicketingSystem(int numVendors, int numCustomers, int ticketReleaseRate, int customerRetrievalRate) {
        if (running) {
            throw new IllegalStateException("Ticketing system is already running.");
        }
        running = true;

        // Start vendor threads
        for (int i = 0; i < numVendors; i++) {
            Thread vendorThread = new Thread(() -> {
                try {
                    while (running) {
                        addTickets(1, Thread.currentThread().getName());
                        Thread.sleep(ticketReleaseRate);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Vendor " + (i + 1));
            vendorThreads.add(vendorThread);
            vendorThread.start();
        }

        // Start customer threads
        for (int i = 0; i < numCustomers; i++) {
            Thread customerThread = new Thread(() -> {
                try {
                    while (running) {
                        removeTickets(1, Thread.currentThread().getName());
                        Thread.sleep(customerRetrievalRate);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Customer " + (i + 1));
            customerThreads.add(customerThread);
            customerThread.start();
        }
    }

    public void stopTicketingSystem() {
        running = false;

        // Interrupt and clear threads
        for (Thread vendorThread : vendorThreads) {
            vendorThread.interrupt();
        }
        vendorThreads.clear();

        for (Thread customerThread : customerThreads) {
            customerThread.interrupt();
        }
        customerThreads.clear();

        logWebSocketHandler.addLog("Ticketing system stopped.");
        System.out.println("Ticketing system stopped.");
    }
}

