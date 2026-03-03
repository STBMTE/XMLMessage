package com.example.xmlclient;

import com.example.message.MessageDocument;
import com.example.message.MessageType;
import com.example.message.RequestType;
import com.example.message.StatusType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Calendar;
import java.util.TimeZone;

@Component
public class XmlClientRunner implements CommandLineRunner {

    private static final int CONNECT_TIMEOUT_MILLIS = 3000;
    private static final XmlOptions PRETTY_XML = new XmlOptions()
            .setSavePrettyPrint()
            .setSaveAggressiveNamespaces();

    private final RestClient restClient;
    private final XmlClientProperties properties;

    public XmlClientRunner(RestClient restClient, XmlClientProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        if (!isServerReachable()) {
            System.out.println("Could not connect to server: " + properties.baseUrl());
            return;
        }

        Thread commandReaderThread = new Thread(this::runCommandLoop, "xml-client-command-reader");
        commandReaderThread.start();

        try {
            commandReaderThread.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void runCommandLoop() {
        printHelp();
        System.out.println("Connected to " + properties.baseUrl());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    return;
                }

                if (line.equals("-h")) {
                    System.out.println("Client stopped.");
                    return;
                }
                if (line.startsWith("-m")) {
                    handleMessageCommand(line);
                    continue;
                }

                System.out.println("Unknown command: " + line);
                printHelp();
            }
        } catch (IOException ex) {
            System.out.println("Console input error: " + ex.getMessage());
        }
    }

    private void handleMessageCommand(String commandLine) {
        if (commandLine.length() == 2) {
            System.out.println("Usage: -m <message text>");
            return;
        }
        if (!Character.isWhitespace(commandLine.charAt(2))) {
            System.out.println("Unknown command: " + commandLine);
            printHelp();
            return;
        }

        String text = commandLine.substring(3).trim();
        if (text.isBlank()) {
            System.out.println("Usage: -m <message text>");
            return;
        }

        String requestXml = buildRequestXml(System.getProperty("user.name", "unknown"), text);

        try {
            String responseXml = sendXml(requestXml);
            StatusType status = parseStatus(responseXml);
            if (status.getCode() == 0) {
                System.out.println("Accepted");
            } else {
                System.out.println("Rejected: " + status.getReason());
            }
        } catch (RestClientException ex) {
            System.out.println("Request failed: " + ex.getMessage());
        } catch (XmlException ex) {
            System.out.println("Failed to parse XML response: " + ex.getMessage());
        }
    }

    private boolean isServerReachable() {
        URI uri;
        try {
            uri = URI.create(properties.baseUrl());
        } catch (IllegalArgumentException ex) {
            return false;
        }

        String host = uri.getHost();
        int port = uri.getPort();
        if (host == null) {
            return false;
        }
        if (port == -1) {
            port = "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private String buildRequestXml(String user, String text) {
        MessageDocument requestDocument = MessageDocument.Factory.newInstance();
        MessageType message = requestDocument.addNewMessage();
        message.addNewHeader().setTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")));

        RequestType requestType = message.addNewRequest();
        requestType.setUser(user);
        requestType.setText(text);

        return requestDocument.xmlText(PRETTY_XML);
    }

    private String sendXml(String requestXml) {
        String responseXml = restClient.post()
                .uri(properties.path())
                .contentType(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .body(requestXml)
                .retrieve()
                .body(String.class);

        if (responseXml == null || responseXml.isBlank()) {
            throw new IllegalStateException("Server returned empty XML response");
        }

        return responseXml;
    }

    private static StatusType parseStatus(String responseXml) throws XmlException {
        MessageDocument responseDocument = MessageDocument.Factory.parse(responseXml);
        return responseDocument.getMessage().getResponse().getStatus();
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("  -m <message text>  Send message");
        System.out.println("  -h                 Exit");
    }
}
