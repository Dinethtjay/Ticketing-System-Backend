package com.iit.TicketingSystem.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TicketPool {
    private final List<String> tickets;
    private final int maxCapacity;

    public TicketPool(int initialTickets, int maxCapacity) {
        this.tickets = Collections.synchronizedList(new ArrayList<>());
        this.maxCapacity = maxCapacity;
        for (int i = 0; i < initialTickets; i++) {
            tickets.add("Ticket: " + (i + 1));
        }
    }
//check controller class where threads start. there we should create new customers and vendors in an loop. number of vendors and and customers needed
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
//        System.out.println("Getting ticket count: " + tickets.size());
        return tickets.size();
    }
}
