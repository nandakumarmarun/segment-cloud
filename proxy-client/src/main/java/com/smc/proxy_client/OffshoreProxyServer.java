package com.smc.proxy_client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

@Component
public class OffshoreProxyServer implements CommandLineRunner {
    private static final int PORT = 9090;

    private final WebClient webClient;

    public OffshoreProxyServer() {
        this.webClient = WebClient.builder().build();
    }

    @Override
    public void run(String... args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Offshore Proxy is running on port " + PORT);

            while (true) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                    handleClientRequest(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (socket != null && !socket.isClosed()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void handleClientRequest(Socket socket) {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String url = reader.readLine();
            System.out.println("Processing request for: " + url);

            String response = forwardRequest(url);
            System.out.println(response);

            writer.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String forwardRequest(String url) {
        try {
//            RestTemplate restTemplate = new RestTemplate();
//            return restTemplate.getForObject(url, String.class);

            return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // Blocking call (for synchronous behavior)


        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Handle HTTP errors (4xx and 5xx)
            System.out.println(e.getMessage());
            return "Error: Received HTTP " + e.getStatusCode() + " from " + url;
        } catch (ResourceAccessException e) {
            // Handle connection timeouts
            System.out.println(e.getMessage());
            return "Error: Unable to reach " + url;
        } catch (Exception e) {
            // Handle unexpected errors
            System.out.println(e.getMessage());
            return "Error: Unexpected issue while forwarding request to " + url;
        }
    }
}
