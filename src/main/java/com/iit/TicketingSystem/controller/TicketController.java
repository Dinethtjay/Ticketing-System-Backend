package com.iit.TicketingSystem.controller;

import com.iit.TicketingSystem.model.Configuration;
import com.iit.TicketingSystem.service.ConfigurationService;
import com.iit.TicketingSystem.service.TicketPool;
import com.iit.TicketingSystem.websocket.LogWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class TicketController {

    @Autowired
    private LogWebSocketHandler logWebSocketHandler;

    @Autowired
    private ConfigurationService configurationService;

    private TicketPool ticketPool;  // Declare ticketPool at the class level
    private Configuration lastSavedConfiguration; // Track the last saved configuration

@PostMapping("/start")
public ResponseEntity<String> startTicketingSystem() {
    if (ticketPool == null) {
        if (lastSavedConfiguration == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot start the system. Provide new configurations or load a previous one first.");
        }

        // Initialize a new TicketPool with the saved configuration
        ticketPool = new TicketPool(
                lastSavedConfiguration.getTotalTickets(),
                lastSavedConfiguration.getMaxTicketCapacity(),
                logWebSocketHandler
        );
    }

    // Start the system if it is not already running
    if (!ticketPool.isRunning()) {
        ticketPool.startTicketingSystem(
                lastSavedConfiguration.getNumVendors(),
                lastSavedConfiguration.getNumCustomers(),
                lastSavedConfiguration.getTicketReleaseRate(),
                lastSavedConfiguration.getCustomerRetrievalRate()
        );
        return ResponseEntity.ok("Ticketing system started successfully.");
    }

    return ResponseEntity.status(HttpStatus.CONFLICT).body("Ticketing system is already running.");
}


    @PostMapping("/stop")
    public ResponseEntity<String> stopTicketingSystem() {
        if (ticketPool == null || !ticketPool.isRunning()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ticketing system is not currently running.");
        }

        // Stop the threads but keep the ticketPool object
        ticketPool.stopTicketingSystem();

        // Only update the remaining tickets in the last saved configuration
        if (lastSavedConfiguration != null) {
            lastSavedConfiguration.setTotalTickets(ticketPool.getTicketCount());
        }

        return ResponseEntity.ok("Ticketing system stopped. Tickets remaining: " + ticketPool.getTicketCount());
    }

    @PostMapping("/config")
    public ResponseEntity<String> saveConfiguration(@RequestBody Configuration configuration) {
        try {
            lastSavedConfiguration = configuration; // Save the new configuration
            configurationService.saveConfiguration(configuration); // Save to the database
            return ResponseEntity.ok("Configuration saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save configuration: " + e.getMessage());
        }
    }

    @GetMapping("/config")
    public ResponseEntity<?> getLastConfiguration() {
        Optional<Configuration> configuration = configurationService.loadLastConfiguration();

        if (configuration.isPresent()) {
            lastSavedConfiguration = configuration.get(); // Load and set the last saved configuration
            return ResponseEntity.ok(configuration.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No previous configuration found.");
        }
    }


    @PostMapping("/logs")
    public ResponseEntity<String> addLog(@RequestBody String log) {
        logWebSocketHandler.addLog(log);
        return ResponseEntity.ok("Log added and broadcasted.");
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getTicketCount() {
        if (ticketPool == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(0); // Return 0 instead of a 400 status.
        }
        return ResponseEntity.ok(ticketPool.getTicketCount());
    }
}

