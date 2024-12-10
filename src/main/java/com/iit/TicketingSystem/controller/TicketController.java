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

/**
 * Controller for managing the ticketing system.
 * Provides endpoints for system control, configuration management, and logs.
 */
@RestController
@RequestMapping("/api")
public class TicketController {

    @Autowired
    private LogWebSocketHandler logWebSocketHandler;

    @Autowired
    private ConfigurationService configurationService;

    private TicketPool ticketPool;
    private Configuration lastSavedConfiguration;

    /**
     * Starts the ticketing system based on the last saved configuration.
     *
     * @return ResponseEntity with a success or error message.
     */
@PostMapping("/start")
public ResponseEntity<String> startTicketingSystem() {
    // Checking whether TicketPool is initialized with a configuration before starting the system
    if (ticketPool == null) {
        if (lastSavedConfiguration == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot start the system. Provide new configurations or load a previous one first.");
        }

        //Initialize a new TicketPool with the saved configuration
        ticketPool = new TicketPool(
                lastSavedConfiguration.getTotalTickets(),
                lastSavedConfiguration.getMaxTicketCapacity(),
                logWebSocketHandler
        );
    }

    //Start the system if it is not already running
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

    /**
     * Stops the ticketing system and preserves the remaining tickets in the configuration.
     *
     * @return ResponseEntity with a success or error message.
     */
    @PostMapping("/stop")
    public ResponseEntity<String> stopTicketingSystem() {
        // Check if the ticketing system is currently running
        if (ticketPool == null || !ticketPool.isRunning()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ticketing system is not currently running.");
        }

        //Stop the threads but keep the ticketPool object
        ticketPool.stopTicketingSystem();

        //Update the last saved configuration
        if (lastSavedConfiguration != null) {
            lastSavedConfiguration.setTotalTickets(ticketPool.getTicketCount());
        }

        return ResponseEntity.ok("Ticketing system stopped. Tickets remaining: " + ticketPool.getTicketCount());
    }

    /**
     * Saves a new configuration for the ticketing system.
     *
     * @param configuration The configuration to be saved.
     * @return ResponseEntity with a success or error message.
     */
    @PostMapping("/config")
    public ResponseEntity<String> saveConfiguration(@RequestBody Configuration configuration) {
        // Save or update the ticketing system configuration
        try {
            lastSavedConfiguration = configuration; //Save the new configuration
            configurationService.saveConfiguration(configuration); //Save to the database
            return ResponseEntity.ok("Configuration saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save configuration: " + e.getMessage());
        }
    }

    /**
     * Retrieves the last saved configuration.
     *
     * @return ResponseEntity containing the last saved configuration or an error message.
     */
    @GetMapping("/config")
    public ResponseEntity<?> getLastConfiguration() {
        //Retrieve the most recently saved configuration
        Optional<Configuration> configuration = configurationService.loadLastConfiguration();
        if (configuration.isPresent()) {
            lastSavedConfiguration = configuration.get();
            return ResponseEntity.ok(configuration.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No previous configuration found.");
        }
    }

    /**
     * Adds a log to the system and broadcasts it to connected WebSocket clients.
     *
     * @param log The log message to be added.
     * @return ResponseEntity with a success message.
     */
    @PostMapping("/logs")
    public ResponseEntity<String> addLog(@RequestBody String log) {
        //Passing logs to the connected websocket clients
        logWebSocketHandler.addLog(log);
        return ResponseEntity.ok("Log added and broadcasted.");
    }

    /**
     * Retrieves the current ticket count.
     *
     * @return ResponseEntity with the current ticket count or an error message.
     */
    @GetMapping("/count")
    public ResponseEntity<Integer> getTicketCount() {
        //Return the current number of tickets available in the system
        if (ticketPool == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(0);
        }
        return ResponseEntity.ok(ticketPool.getTicketCount());
    }
}

