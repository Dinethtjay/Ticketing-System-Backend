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


    public TicketPool(int initialTickets, int maxCapacity) {
        this.tickets = Collections.synchronizedList(new ArrayList<>());
        this.maxCapacity = maxCapacity;
        for (int i = 0; i < initialTickets; i++) {
            tickets.add("Ticket: " + (i + 1));
        }
    }

    public synchronized void addTickets(int count) {
        while (tickets.size() + count > maxCapacity) {
            try {
                System.out.println(Thread.currentThread().getName() + ": Max capacity reached. Waiting to add tickets...");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        for (int i = 0; i < count; i++) {
            tickets.add("Ticket: " + (tickets.size() + 1));
        }

        System.out.println(Thread.currentThread().getName() + " added " + count + " tickets. Total ticket count: " + tickets.size());
        notifyAll();
    }

    public synchronized void removeTickets(int count) {
        while (tickets.isEmpty()) {
            try {
                System.out.println(Thread.currentThread().getName() + ": No tickets available. Waiting...");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        for (int i = 0; i < count && !tickets.isEmpty(); i++) {
            tickets.remove(0);
        }
        System.out.println(Thread.currentThread().getName() + " purchased " + count + " ticket(s). Remaining tickets: " + tickets.size());
        notifyAll();
    }

    public synchronized int getTicketCount() {
        return tickets.size();
    }

    public void startTicketingSystem(int numVendors, int numCustomers, int ticketReleaseRate, int customerRetrievalRate, LogWebSocketHandler logWebSocketHandler) {
        for (int i = 0; i < numVendors; i++) {
            Thread vendorThread = new Thread(new Vendor(this, ticketReleaseRate, logWebSocketHandler), "Vendor " + (i + 1));
            vendorThreads.add(vendorThread);
            vendorThread.start();
        }

        for (int i = 0; i < numCustomers; i++) {
            Thread customerThread = new Thread(new Customer(this, customerRetrievalRate, logWebSocketHandler), "Customer " + (i + 1));
            customerThreads.add(customerThread);
            customerThread.start();
        }
    }

    public void stopTicketingSystem() {
        for (Thread vendorThread : vendorThreads) {
            vendorThread.interrupt();
        }
        vendorThreads.clear();

        for (Thread customerThread : customerThreads) {
            customerThread.interrupt();
        }
        customerThreads.clear();

        System.out.println("Ticketing system stopped.");
    }
}
