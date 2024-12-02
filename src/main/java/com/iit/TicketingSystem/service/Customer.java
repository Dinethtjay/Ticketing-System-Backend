package com.iit.TicketingSystem.service;

import com.iit.TicketingSystem.websocket.LogWebSocketHandler;

public class Customer implements Runnable {
    private final TicketPool ticketPool;
    private final int customerRetrievalRate;
    private final LogWebSocketHandler logHandler;

    public Customer(TicketPool ticketPool, int customerRetrievalRate, LogWebSocketHandler logHandler) {
        this.ticketPool = ticketPool;
        this.customerRetrievalRate = customerRetrievalRate;
        this.logHandler = logHandler;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ticketPool.removeTickets(1);
                logHandler.addLog(Thread.currentThread().getName() + " removed a ticket. Remaining: " + ticketPool.getTicketCount());
                Thread.sleep(customerRetrievalRate);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
