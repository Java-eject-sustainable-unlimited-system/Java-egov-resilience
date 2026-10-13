package com.jesus.system.monitor;

import com.jesus.system.config.WatchdogProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.Objects;

/**
 * Shared HTTP vitals probe (Java 17 HttpClient) used by the monitors.
 * Replaces the duplicated legacy HttpURLConnection checks.
 */
@Component
public class HttpVitalsProbe {

    private static final Logger log = LoggerFactory.getLogger(HttpVitalsProbe.class);

    private final WatchdogProperties properties;
    private final HttpClient httpClient;

    public HttpVitalsProbe(WatchdogProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getHttpTimeout())
                .build();
    }

    /**
     * @return true if the endpoint answers with a non-5xx status within the configured timeout
     */
    public boolean isAlive(String endpoint) {
        URI uri;
        try {
            uri = URI.create(endpoint);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("Invalid health endpoint URI");
            return false;
        }

        Origin endpointOrigin = Origin.from(uri);
        if (endpointOrigin == null || properties.getHealthEndpointAllowedOrigins() == null
                || properties.getHealthEndpointAllowedOrigins().stream()
                        .filter(Objects::nonNull)
                        .map(Origin::fromAllowedOrigin)
                        .noneMatch(endpointOrigin::equals)) {
            log.error("Health endpoint origin is not allowlisted");
            return false;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(properties.getHttpTimeout())
                    .GET()
                    .build();
            int status = httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            return status >= 200 && status < 500;
        } catch (IOException e) {
            log.debug("HTTP vitals probe failed due to an I/O error");
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IllegalArgumentException e) {
            log.error("Invalid health endpoint URI");
            return false;
        }
    }

    private record Origin(String scheme, String host, int port) {

        private static Origin from(URI uri) {
            if (uri == null || uri.getUserInfo() != null || uri.getHost() == null) {
                return null;
            }

            String scheme = uri.getScheme();
            if (scheme == null) {
                return null;
            }
            scheme = scheme.toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) {
                return null;
            }

            String host = uri.getHost().toLowerCase(Locale.ROOT);
            if (host.startsWith("[") && host.endsWith("]")) {
                host = host.substring(1, host.length() - 1);
            }
            int port = uri.getPort();
            if (port == -1) {
                port = scheme.equals("https") ? 443 : 80;
            }
            return new Origin(scheme, host, port);
        }

        private static Origin fromAllowedOrigin(String allowedOrigin) {
            try {
                URI uri = URI.create(allowedOrigin);
                String path = uri.getRawPath();
                if ((path != null && !path.isEmpty() && !path.equals("/"))
                        || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                    return null;
                }
                return from(uri);
            } catch (IllegalArgumentException | NullPointerException e) {
                return null;
            }
        }
    }
}
