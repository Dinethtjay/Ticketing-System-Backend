package com.iit.TicketingSystem.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WebSocket handler for broadcasting log messages to connected clients.
 * Handles adding logs, saving them to a file, and broadcasting them over WebSocket connections.
 */
@Component
public class LogWebSocketHandler extends TextWebSocketHandler {

    private static final String LOG_FILE = "ticket_system_logs.txt";
    private final List<String> logs = new LinkedList<>();
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    /**
     * Adds a log message to the system and broadcasts it to connected clients.
     *
     * @param log the log message
     */
    public synchronized void addLog(String log) {
        logs.add(log);
        if (logs.size() > 100) {
            logs.remove(0); // Limit logs to the last 100 entries
        }
        saveLogToFile(log);
        broadcastLog(log);
    }

    /**
     * Saves a log message to a file.
     *
     * @param log the log message to save
     */
    private void saveLogToFile(String log) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(log);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing log to file: " + e.getMessage());
        }
    }

    /**
     * Broadcasts a log message to all connected WebSocket clients.
     *
     * @param log the log message to broadcast
     */
    private void broadcastLog(String log) {
        TextMessage message = new TextMessage(log);
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (IOException e) {
                System.err.println("Error sending log to client: " + e.getMessage());
            }
        }
    }

    /**
     * Called when a new WebSocket connection is established. Adds the session to the list of connected clients.
     *
     * @param session the WebSocket session of the newly connected client
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("Client connected: " + session.getId());
    }


}

