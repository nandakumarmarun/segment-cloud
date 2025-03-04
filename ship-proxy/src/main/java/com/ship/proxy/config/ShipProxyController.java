package com.ship.proxy.config;


import okhttp3.*;
import okhttp3.MediaType;
import org.springframework.http.*;
import org.springframework.web.server.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@RestController
@RequestMapping("/")
public class ShipProxyController {

    String OFFSHORE_PROXY_URL = "http://localhost:8082/proxy/forward";
    private final OkHttpClient client = new OkHttpClient();
    private final BlockingQueue<Request> requestQueue = new LinkedBlockingQueue<>();

    public ShipProxyController() {
        System.out.println("Hello");
        startRequestProcessor();
    }

    @PostMapping("/proxy")
    public ResponseEntity<String> handleRequest(@RequestBody String body, @RequestHeader HttpHeaders headers) {
        try {

            Request request = new Request.Builder()
                .url(OFFSHORE_PROXY_URL)
                .post(okhttp3.RequestBody.create(body, MediaType.parse("application/json")))
                .build();
            requestQueue.put(request); // Add request to queue
            return ResponseEntity.ok("Request added to queue");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request");
        }
    }


    private void startRequestProcessor() {
        new Thread(() -> {
            while (true) {
                try {
                    System.out.println(requestQueue);
                    Request request = requestQueue.take(); // Process requests one by one
                    Response response = client.newCall(request).execute();
                    System.out.println("Processed request with response: " + response.code());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
