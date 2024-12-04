package com.iit.TicketingSystem.controller;

import com.iit.TicketingSystem.model.Configuration;
import com.iit.TicketingSystem.service.ConfigurationService;
import com.iit.TicketingSystem.service.Customer;
import com.iit.TicketingSystem.service.TicketPool;
import com.iit.TicketingSystem.service.Vendor;
import com.iit.TicketingSystem.websocket.LogWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TicketController {

    @Autowired
    private LogWebSocketHandler logWebSocketHandler;

    @Autowired
    private ConfigurationService configurationService;

    private static final String LOG_FILE = "ticketing_logs.txt";

    private TicketPool ticketPool;
    private List<Thread> vendorThreads = new ArrayList<>();
    private List<Thread> customerThreads = new ArrayList<>();

    @PostMapping("/start")
    public ResponseEntity<String> startTicketingSystem(@RequestBody Configuration configuration) {
        // Validate configuration
        if (configuration == null ||
                configuration.getTotalTickets() <= 0 ||
                configuration.getTicketReleaseRate() <= 0 ||
                configuration.getCustomerRetrievalRate() <= 0 ||
                configuration.getMaxTicketCapacity() <= 0 ||
                configuration.getNumVendors() <= 0 ||
                configuration.getNumCustomers() <= 0) {
            return ResponseEntity.badRequest().body("Invalid configuration values");
        }

        int totalTickets = configuration.getTotalTickets();
        int ticketReleaseRate = configuration.getTicketReleaseRate();
        int customerRetrievalRate = configuration.getCustomerRetrievalRate();
        int maxTicketCapacity = configuration.getMaxTicketCapacity();
        int numVendors = configuration.getNumVendors();
        int numCustomers = configuration.getNumCustomers();

        try {
            this.ticketPool = new TicketPool(totalTickets, maxTicketCapacity);  // Initialize ticket pool

            // Create and start Vendor threads
            for (int i = 0; i < numVendors; i++) {
                Thread vendorThread = new Thread(new Vendor(ticketPool, ticketReleaseRate, logWebSocketHandler), "Vendor " + (i + 1));
                vendorThreads.add(vendorThread);
                vendorThread.start();
            }

            // Create and start Customer threads
            for (int i = 0; i < numCustomers; i++) {
                Thread customerThread = new Thread(new Customer(ticketPool, customerRetrievalRate, logWebSocketHandler), "Customer " + (i + 1));
                customerThreads.add(customerThread);
                customerThread.start();
            }

            return ResponseEntity.ok("Ticketing system started with " + numVendors + " vendors and " + numCustomers + " customers.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start ticketing system: " + e.getMessage());
        }

    }



    @PostMapping("/stop")
    public ResponseEntity<String> stopTicketingSystem() {
        // Stop all Vendor threads
        for (Thread vendorThread : vendorThreads) {
            vendorThread.interrupt();
        }
        vendorThreads.clear();

        // Stop all Customer threads
        for (Thread customerThread : customerThreads) {
            customerThread.interrupt();
        }
        customerThreads.clear();

        System.out.println("Ticketing system stopped");
        return ResponseEntity.ok("Ticketing system stopped");

    }

    @PostMapping("/config")
    public ResponseEntity<?> saveConfiguration(@RequestBody Configuration configuration) {
        configurationService.saveConfiguration(configuration);
        return ResponseEntity.ok("Configuration saved successfully");
    }

    @GetMapping("/config")
    public ResponseEntity<?> getLastConfiguration() {
        return configurationService.loadLastConfiguration()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(new Configuration()));
    }

    @GetMapping("/logs")
    public ResponseEntity<String> getLogs() {
        return ResponseEntity.ok(logWebSocketHandler.getLogs());
    }

    @GetMapping("/logs/file")
    public String getLogFileContent() {
        Path logFilePath = Paths.get(LOG_FILE);
        try {
            return Files.readString(logFilePath);
        } catch (IOException e) {
            return "Error reading log file: " + e.getMessage();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getTicketCount() {
        if (ticketPool == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(0); // Return 0 instead of a 400 status.
        }
        return ResponseEntity.ok(ticketPool.getTicketCount());
    }

}
