package com.jesus.system.monitor;

import com.jesus.system.config.WatchdogProperties;
import com.jesus.system.core.ProcessLifecycleManager;
import com.jesus.system.core.VitalsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Low-level raw TCP socket probe (detection-only).
 *
 * <p>Bypasses HTTP overhead and directly checks whether the legacy port
 * still accepts TCP handshakes. Reports into the vitals registry so
 * /actuator/health reflects socket-level freezes; ejection is left to
 * the threshold-based monitors.
 *
 * <p>Formerly {@code JesusAbyssProbeComponent}.
 */
@Component
public class AbyssProbeMonitor {

    private static final Logger log = LoggerFactory.getLogger(AbyssProbeMonitor.class);
    private static final String COMPONENT = "raw-socket-probe";

    private final WatchdogProperties properties;
    private final VitalsRegistry vitals;
    private final ProcessLifecycleManager lifecycleManager;
    private final FailureCounter failures = new FailureCounter();

    public AbyssProbeMonitor(WatchdogProperties properties, VitalsRegistry vitals,
                             ProcessLifecycleManager lifecycleManager) {
        this.properties = properties;
        this.vitals = vitals;
        this.lifecycleManager = lifecycleManager;
    }

    @Scheduled(fixedRate = 1000)
    public void probeRawSocket() {
        try (Socket socket = new Socket()) {
            socket.connect(
                    new InetSocketAddress(properties.getProbeHost(), properties.getProbePort()),
                    (int) properties.getProbeTimeout().toMillis());
            handleProbeResult(true);
        } catch (IOException e) {
            handleProbeResult(false);
        }
    }

    void handleProbeResult(boolean alive) {
        if (alive) {
            failures.reset();
            vitals.reportUp(COMPONENT, "TCP handshake OK on port " + properties.getProbePort());
            return;
        }

        int failureCount = failures.recordFailure();
        vitals.reportDown(COMPONENT,
                "Raw socket handshake failed on port %d (%d/%d)"
                        .formatted(properties.getProbePort(), failureCount, properties.getFailureThreshold()));
        log.warn("[☠️ ABYSS ALERT] Raw socket handshake failed on port {} ({}/{}) — target may be deadlocked.",
                properties.getProbePort(), failureCount, properties.getFailureThreshold());

        if (failures.hasReached(properties.getFailureThreshold())) {
            lifecycleManager.ejectAndResurrect("RAW_SOCKET_UNRESPONSIVE");
            failures.reset();
        }
    }
}
