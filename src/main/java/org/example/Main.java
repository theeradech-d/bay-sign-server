package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.*;

public class Main {

    private static final String strPublicKey = System.getenv("PUBLIC_KEY") != null ? System.getenv("PUBLIC_KEY") : "DEFAULT";

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(3002), 0);

            server.createContext("/", exchange -> {
                JSONObject response = new JSONObject();
                response.put("status", "UP");
                sendJsonResponse(exchange, response.toString(), 200);
            });

            server.createContext("/health", exchange -> {
                JSONObject response = new JSONObject();
                response.put("status", "UP");
                sendJsonResponse(exchange, response.toString(), 200);
            });

            server.createContext("/sign", new SignHandler());

            server.setExecutor(null);
            server.start();

            System.out.println("HTTP server started on port 3002");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ฟังก์ชันช่วยเหลือในการส่ง JSON response
    private static void sendJsonResponse(HttpExchange exchange, String jsonResponse, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        OutputStream output = exchange.getResponseBody();
        output.write(jsonResponse.getBytes());
        output.close();
    }

    static class SignHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                JSONObject response = new JSONObject();
                response.put("error", "Only POST method is allowed for this endpoint");
                sendJsonResponse(exchange, response.toString(), 405); // 405 Method Not Allowed
                return;
            }

            // Get x-bay-sign-public-key header
            String headerPublicKey = exchange.getRequestHeaders().getFirst("x-bay-sign-public-key");
            String publicKeyToUse = (headerPublicKey != null) ? headerPublicKey : strPublicKey;

            InputStream requestBody = exchange.getRequestBody();
            String body = new Scanner(requestBody, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();

            JSONObject response = new JSONObject();
            try {
                JSONObject jsonRequest = new JSONObject(body);
                BAYSign baySign = new BAYSign();
                String signature = baySign.createSignature(jsonRequest, publicKeyToUse);

                response.put("status", "success");
                response.put("signature", signature);
            } catch (Exception e) {
                response.put("status", "error");
                response.put("message", e.getMessage());
                sendJsonResponse(exchange, response.toString(), 500); // 500 Internal Server Error
                return;
            }

            sendJsonResponse(exchange, response.toString(), 200); // ส่ง response กลับ
        }
    }
}
