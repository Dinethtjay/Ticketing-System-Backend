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

@Component
public class LogWebSocketHandler extends TextWebSocketHandler {

    private static final String LOG_FILE = "ticket_system_logs.txt";
    private List<String> logs = new LinkedList<>();

    public synchronized void addLog(String log) {
        logs.add(log);
        if (logs.size() > 100) {
            logs.remove(0); // Limit logs to the last 100 entries
        }
        saveLogToFile(log);
    }

    private void saveLogToFile(String log) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(log);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing log to file: " + e.getMessage());
        }
    }

    public synchronized String getLogs() {
        return String.join("\n", logs);
    }

    @Override
    public synchronized void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        session.sendMessage(new TextMessage(getLogs()));
    }
}
