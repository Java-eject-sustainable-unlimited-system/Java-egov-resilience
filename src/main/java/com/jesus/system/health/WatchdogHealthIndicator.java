package com.jesus.system.health;

import com.jesus.system.core.VitalsRegistry;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Actuator health indicator backed by the real vitals reported by every
 * monitor — replaces the previous implementation that was hardcoded to
 * always return UP.
 */
@Component
public class WatchdogHealthIndicator implements HealthIndicator {

    private final VitalsRegistry vitals;

    public WatchdogHealthIndicator(VitalsRegistry vitals) {
        this.vitals = vitals;
    }

    @Override
    public Health health() {
        Map<String, VitalsRegistry.Vital> snapshot = vitals.snapshot();
        Health.Builder builder = vitals.hasFailures() ? Health.down() : Health.up();
        builder.withDetail("system", "J.E.S.U.S Watchdog");

        if (snapshot.isEmpty()) {
            builder.withDetail("monitors", "no vitals reported yet");
        } else {
            snapshot.forEach((component, vital) ->
                    builder.withDetail(component, vital.status() + " - " + vital.message()));
        }
        return builder.build();
    }
}
