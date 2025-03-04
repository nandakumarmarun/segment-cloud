package com.ship.proxy.config;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.*;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/proxy")
public class OffshoreProxyController {
    private final OkHttpClient client = new OkHttpClient();

    @PostMapping("/forward")
    public ResponseEntity<String> forwardRequest(
        @RequestHeader(value = "Target-URL")  String targetUrl, // The actual internet URL
        @RequestBody(required = false) String requestBody,
        @RequestHeader HttpHeaders headers) {

        try {
            // Build the HTTP request to the target URL
            Request.Builder requestBuilder = new Request.Builder()
                .url(targetUrl);

            // Handle HTTP Method (Assuming POST for now)
            if (requestBody != null && !requestBody.isEmpty()) {
                requestBuilder.post(okhttp3.RequestBody.create(requestBody, MediaType.parse("application/json")));
            } else {
                requestBuilder.get();
            }

            // Copy headers
            headers.forEach((key, values) -> values.forEach(value -> requestBuilder.addHeader(key, value)));

            Request request = requestBuilder.build();

            // Execute the HTTP request
            Response response = client.newCall(request).execute();

            return ResponseEntity
                .status(response.code())
                .body(response.body().string());

        } catch (IOException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error forwarding request: " + e.getMessage());
        }
    }
}
