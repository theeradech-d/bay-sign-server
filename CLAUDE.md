# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java 8 Maven application that provides digital signature services for BAY (Bank of Ayudhya) payment systems. The application exposes an HTTP API server on port 3002 that creates RSA-based digital signatures for payment transactions.

## Core Architecture

### Main Components

-   **Main.java**: HTTP server using Java's built-in `HttpServer` class with three endpoints:

    -   `GET /`: Root health check endpoint
    -   `GET /health`: Health check endpoint
    -   `POST /sign`: Creates digital signatures for payment requests

-   **BAYSign.java**: Core cryptographic library that handles:
    -   RSA public/private key parsing from Base64 strings
    -   SHA-256 hashing of payment parameters
    -   RSA encryption for digital signature creation
    -   Parameter sorting and encoding for payment processing

### Key Features

-   **Header-based Public Key Injection**: The `/sign` endpoint accepts `x-bay-sign-public-key` header to dynamically specify which public key to use
-   **Environment Variable Fallback**: If no header is provided, falls back to `PUBLIC_KEY` environment variable
-   **JSON API**: Accepts payment data as JSON and returns base64-encoded digital signatures

## Development Commands

### Building and Running

```bash
# Compile the project
mvn compile

# Run the application (development)
mvn exec:java -Dexec.mainClass="org.example.Main"

# Run with environment variable
export PUBLIC_KEY="your-base64-public-key"
mvn exec:java -Dexec.mainClass="org.example.Main"

# Build executable JAR with dependencies
mvn clean package
java -cp "target/sprbaysign-1.0-SNAPSHOT-jar-with-dependencies.jar" org.example.Main

# Build Docker image
docker build -t spr-bay-sign .

# Run Docker container
docker run -p 3002:3002 spr-bay-sign
```

### Testing the API

```bash
# Health check
curl http://localhost:3002/health

# Create signature without header (uses env variable)
curl -X POST http://localhost:3002/sign \
  -H "Content-Type: application/json" \
  -d '{"bizMchId":"1234","billerId":"1234","channel":"2","reference1":"1234","reference2":"1234","terminalId":"1234","amount":"100","remark":"test"}'

# Create signature with custom public key
curl -X POST http://localhost:3002/sign \
  -H "Content-Type: application/json" \
  -H "x-bay-sign-public-key: YOUR_BASE64_PUBLIC_KEY_HERE" \
  -d '{"bizMchId":"1234","billerId":"1234","channel":"2","reference1":"1234","reference2":"1234","terminalId":"1234","amount":"100","remark":"test"}'
```

## Key Implementation Details

### Cryptographic Process

1. Payment parameters are sorted alphabetically and concatenated with "&" separators
2. The concatenated string is hashed using SHA-256
3. The hash is encrypted using RSA/ECB/PKCS1Padding with the public key
4. The result is base64-encoded and returned as the signature

### Public Key Handling

-   Primary source: `x-bay-sign-public-key` HTTP header
-   Fallback source: `PUBLIC_KEY` environment variable
-   Default value: Hardcoded key in `BAYSign.java` (should not be used in production)

### Dependencies

-   Java 8 (target/source compatibility)
-   Maven for build management
-   commons-codec (1.10) for Base64 encoding and SHA-256
-   commons-lang (2.4) for string utilities
-   org.json (20210307) for JSON processing
-   JUnit (4.13.2) for testing

## Security Considerations

-   The application handles RSA public keys but does not store private keys
-   Public keys are expected in base64-encoded X.509 format
-   Payment data is logged at DEBUG level for troubleshooting
-   The server runs on port 3002 and accepts connections from any interface
