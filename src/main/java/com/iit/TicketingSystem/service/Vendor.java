package com.iit.TicketingSystem.service;

import com.iit.TicketingSystem.websocket.LogWebSocketHandler;

public class Vendor implements Runnable {
    private final TicketPool ticketPool;
    private final int releaseRate;
    private final LogWebSocketHandler logHandler;

    public Vendor(TicketPool ticketPool, int releaseRate, LogWebSocketHandler logHandler) {
        this.ticketPool = ticketPool;
        this.releaseRate = releaseRate;
        this.logHandler = logHandler;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ticketPool.addTickets(1);
                logHandler.addLog(Thread.currentThread().getName() + " added a ticket. Total: " + ticketPool.getTicketCount());
                Thread.sleep(releaseRate);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
