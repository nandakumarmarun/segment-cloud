package com.ship.proxy;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@RestController
public class ProxyClientController {
    private static final String OFFSHORE_PROXY_HOST = "localhost";
    private static final int OFFSHORE_PROXY_PORT = 9090;

    @GetMapping("/*")
    public ResponseEntity<String> proxyRequest(@RequestParam String url) {
        try (Socket socket = new Socket(OFFSHORE_PROXY_HOST, OFFSHORE_PROXY_PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            // Send request to offshore proxy
            writer.println(url);
            // Read the response
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            return ResponseEntity.ok(response.toString());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

}
