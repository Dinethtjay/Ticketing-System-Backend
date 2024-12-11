package com.iit.TicketingSystem.service;

import com.iit.TicketingSystem.websocket.LogWebSocketHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class representing a pool of tickets and managing concurrent access.
 * Handles ticket addition and removal with synchronized methods to ensure thread safety.
 */
public class TicketPool {
    private final List<String> tickets;
    private final int maxCapacity;
    private final List<Thread> vendorThreads = new ArrayList<>();
    private final List<Thread> customerThreads = new ArrayList<>();
    private final LogWebSocketHandler logWebSocketHandler;
    private boolean running = false;

    /**
     * Constructs a new {@link TicketPool}.
     *
     * @param initialTickets      the initial number of tickets in the pool
     * @param maxCapacity         the maximum number of tickets the pool can hold
     * @param logWebSocketHandler the WebSocket handler used for logging system events
     */
    public TicketPool(int initialTickets, int maxCapacity, LogWebSocketHandler logWebSocketHandler) {
        this.tickets = Collections.synchronizedList(new ArrayList<>());
        this.maxCapacity = maxCapacity;
        this.logWebSocketHandler = logWebSocketHandler;

        for (int i = 0; i < initialTickets; i++) {
            tickets.add("Ticket: " + (i + 1));
        }
    }

    /**
     * Adds tickets to the pool while ensuring the maximum capacity is not exceeded.
     * If the pool is full, the calling thread will wait until there is space available.
     *
     * @param count      the number of tickets to add
     * @param threadName the name of the thread performing the operation
     */
    public synchronized void addTickets(int count, String threadName) {
        // Add tickets to the pool, ensuring the maximum capacity is not exceeded
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
        logWebSocketHandler.addLog(threadName + " added a ticket. Total ticket count: " + tickets.size());
        System.out.println(threadName + " added a ticket. Total ticket count: " + tickets.size());
        notifyAll(); //Notify waiting threads
    }

    /**
     * Removes tickets from the pool while ensuring availability.
     * If the pool is empty, the calling thread will wait until tickets become available.
     *
     * @param count      the number of tickets to remove
     * @param threadName the name of the thread performing the operation
     */
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
        logWebSocketHandler.addLog(threadName + " purchased a ticket. Remaining tickets: " + tickets.size());
        System.out.println(threadName + " purchased a ticket. Remaining tickets: " + tickets.size());
        notifyAll();
    }

    /**
     * Retrieves the current number of tickets in the pool.
     *
     * @return the current ticket count
     */
    public synchronized int getTicketCount() {
        return tickets.size();
    }

    /**
     * Checks whether the ticketing system is currently running.
     *
     * @return {@code true} if the system is running, {@code false} otherwise
     */
    public synchronized boolean isRunning() {
        return running;
    }


    /**
     * Starts the ticketing system by initializing and starting vendor and customer threads.
     *
     * @param numVendors            the number of vendor threads to start
     * @param numCustomers          the number of customer threads to start
     * @param ticketReleaseRate     the time interval for vendors to add tickets (in milliseconds)
     * @param customerRetrievalRate the time interval for customers to retrieve tickets (in milliseconds)
     * @throws IllegalStateException if the system is already running
     */
    public void startTicketingSystem(int numVendors, int numCustomers, int ticketReleaseRate, int customerRetrievalRate) {
        if (running) {
            throw new IllegalStateException("Ticketing system is already running.");
        }
        running = true;

        // Start vendor threads
        for (int i = 0; i < numVendors; i++) {
            Vendor vendor = new Vendor(this, ticketReleaseRate);
            Thread vendorThread = new Thread(vendor, "Vendor " + (i + 1));
            vendorThreads.add(vendorThread);
            vendorThread.start();
        }

        // Start customer threads
        for (int i = 0; i < numCustomers; i++) {
            Customer customer = new Customer(this, customerRetrievalRate);
            Thread customerThread = new Thread(customer, "Customer " + (i + 1));
            customerThreads.add(customerThread);
            customerThread.start();
        }
    }


    /**
     * Stops the ticketing system by interrupting all vendor and customer threads.
     * Clears the thread lists and logs the shutdown operation.
     */
    public void stopTicketingSystem() {
        running = false;

        // Interrupt vendor threads
        for (Thread vendorThread : vendorThreads) {
            vendorThread.interrupt();
        }
        vendorThreads.clear();

        // Interrupt customer threads
        for (Thread customerThread : customerThreads) {
            customerThread.interrupt();
        }
        customerThreads.clear();

        logWebSocketHandler.addLog("Ticketing system stopped.");
        System.out.println("Ticketing system stopped.");
    }
}


