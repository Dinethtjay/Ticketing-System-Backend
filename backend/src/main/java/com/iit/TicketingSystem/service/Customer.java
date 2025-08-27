package com.iit.TicketingSystem.service;

/**
 * Represents a customer that retrieves tickets from the {@link TicketPool}.
 * Operates in a separate thread, continuously removing tickets at a specified interval.
 */
public class Customer implements Runnable {
    private final TicketPool ticketPool;
    private final int customerRetrievalRate;

    /**
     * Constructs a new {@link Customer}.
     *
     * @param ticketPool           the ticket pool to retrieve tickets from
     * @param customerRetrievalRate the time interval (in milliseconds) between ticket retrievals
     */
    public Customer(TicketPool ticketPool, int customerRetrievalRate) {
        this.ticketPool = ticketPool;
        this.customerRetrievalRate = customerRetrievalRate;
    }

    //Customer thread behaviour
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ticketPool.removeTickets(1, Thread.currentThread().getName());
                Thread.sleep(customerRetrievalRate*1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
