package com.jesus.system.monitor;

import com.jesus.system.config.WatchdogProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class HttpVitalsProbeTest {

    @Test
    void rejectsUnapprovedOriginsBeforeMakingARequest() throws IOException {
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = startServer(exchange -> {
            requests.incrementAndGet();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        try {
            String endpoint = "http://127.0.0.1:" + server.getAddress().getPort() + "/health";

            assertThat(probeWithDefaults().isAlive(endpoint)).isFalse();
            assertThat(probeWithDefaults().isAlive("http://169.254.169.254/latest/meta-data/")).isFalse();
            assertThat(probeWithDefaults().isAlive("file:///etc/passwd")).isFalse();
            assertThat(requests).hasValue(0);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void reportsHealthyForAnAllowedEndpoint() throws IOException {
        HttpServer server = startServer(exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        try {
            assertThat(probeFor(server).isAlive(endpointFor(server))).isTrue();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void reportsUnhealthyWhenTheAllowedEndpointRefusesConnections() throws IOException {
        int unusedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            unusedPort = socket.getLocalPort();
        }
        String origin = "http://127.0.0.1:" + unusedPort;
        WatchdogProperties properties = new WatchdogProperties();
        properties.setHealthEndpointAllowedOrigins(List.of(origin));

        assertThat(new HttpVitalsProbe(properties).isAlive(origin + "/health")).isFalse();
    }

    @Test
    void reportsUnhealthyWhenTheAllowedEndpointTimesOut() throws IOException {
        HttpServer server = startServer(exchange -> {
            try {
                Thread.sleep(500);
                exchange.sendResponseHeaders(200, -1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        try {
            WatchdogProperties properties = propertiesFor(server);
            properties.setHttpTimeout(Duration.ofMillis(50));

            assertThat(new HttpVitalsProbe(properties).isAlive(endpointFor(server))).isFalse();
        } finally {
            server.stop(0);
        }
    }

    private static HttpVitalsProbe probeWithDefaults() {
        return new HttpVitalsProbe(new WatchdogProperties());
    }

    private static HttpVitalsProbe probeFor(HttpServer server) {
        return new HttpVitalsProbe(propertiesFor(server));
    }

    private static WatchdogProperties propertiesFor(HttpServer server) {
        WatchdogProperties properties = new WatchdogProperties();
        properties.setHealthEndpointAllowedOrigins(List.of(
                "http://127.0.0.1:" + server.getAddress().getPort()));
        return properties;
    }

    private static String endpointFor(HttpServer server) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/health";
    }

    private static HttpServer startServer(HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/health", handler::handle);
        server.start();
        return server;
    }

    @FunctionalInterface
    private interface HttpHandler {
        void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException;
    }
}
