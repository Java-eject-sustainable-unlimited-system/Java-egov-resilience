package com.jesus.system.health;

import com.jesus.system.core.VitalsRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;

import static org.assertj.core.api.Assertions.assertThat;

class WatchdogHealthIndicatorTest {

    private final VitalsRegistry registry = new VitalsRegistry();
    private final WatchdogHealthIndicator indicator = new WatchdogHealthIndicator(registry);

    @Test
    void reportsUpWhenNoFailures() {
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);

        registry.reportUp("actuator-ping", "responsive");
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
        assertThat(indicator.health().getDetails()).containsKey("actuator-ping");
    }

    @Test
    void reportsDownWhenAnyMonitorFails() {
        registry.reportUp("actuator-ping", "responsive");
        registry.reportDown("disk-io-guard", "file system locked");

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
        assertThat(indicator.health().getDetails().get("disk-io-guard").toString())
                .contains("DOWN");
    }
}
