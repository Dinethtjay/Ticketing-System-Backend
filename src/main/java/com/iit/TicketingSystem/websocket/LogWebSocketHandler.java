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

@Component
public class LogWebSocketHandler extends TextWebSocketHandler {

    private static final String LOG_FILE = "ticket_system_logs.txt";
    private final List<String> logs = new LinkedList<>();
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public synchronized void addLog(String log) {
        logs.add(log);
        if (logs.size() > 100) {
            logs.remove(0); // Limit logs to the last 100 entries
        }
        saveLogToFile(log);
        broadcastLog(log);
    }

    private void saveLogToFile(String log) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(log);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing log to file: " + e.getMessage());
        }
    }

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

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("Client connected: " + session.getId());
    }


    @Override
    public synchronized void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Optionally handle incoming client messages
    }

}

