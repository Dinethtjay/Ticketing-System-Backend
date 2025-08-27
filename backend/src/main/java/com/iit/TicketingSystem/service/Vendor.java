package com.iit.TicketingSystem.service;

/**
 * Represents a vendor that adds tickets to the {@link TicketPool}.
 * Each vendor operates in its own thread, continuously adding tickets to the pool
 * at a specified interval until interrupted.
 */
public class Vendor implements Runnable {
    private final TicketPool ticketPool;
    private final int releaseRate;

    /**
     * Constructs a new {@link Vendor}.
     *
     * @param ticketPool the ticket pool to which tickets are added
     * @param releaseRate the time interval (in milliseconds) between ticket additions
     */
    public Vendor(TicketPool ticketPool, int releaseRate) {
        this.ticketPool = ticketPool;
        this.releaseRate = releaseRate;
    }

    //Vendor thread behaviour
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ticketPool.addTickets(1, Thread.currentThread().getName());
                Thread.sleep(releaseRate*1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
