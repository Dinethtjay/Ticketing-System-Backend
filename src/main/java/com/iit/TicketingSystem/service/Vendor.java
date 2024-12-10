package com.iit.TicketingSystem.service;

public class Vendor implements Runnable {
    private final TicketPool ticketPool;
    private final int releaseRate;

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
                Thread.sleep(releaseRate);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
