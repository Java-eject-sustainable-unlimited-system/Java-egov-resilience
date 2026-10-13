package com.jesus.system.monitor;

import com.jesus.system.config.WatchdogProperties;
import com.jesus.system.core.ProcessLifecycleManager;
import com.jesus.system.core.VitalsRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AbyssProbeMonitorTest {

    private final WatchdogProperties properties = new WatchdogProperties();
    private final VitalsRegistry vitals = new VitalsRegistry();
    private final ProcessLifecycleManager lifecycleManager = mock(ProcessLifecycleManager.class);
    private final AbyssProbeMonitor monitor =
            new AbyssProbeMonitor(properties, vitals, lifecycleManager);

    @Test
    void recoversTargetOnlyAfterSustainedSocketFailures() {
        int threshold = properties.getFailureThreshold();

        for (int i = 1; i < threshold; i++) {
            monitor.handleProbeResult(false);
        }
        verifyNoInteractions(lifecycleManager);

        monitor.handleProbeResult(false);

        verify(lifecycleManager).ejectAndResurrect("RAW_SOCKET_UNRESPONSIVE");
        assertThat(vitals.snapshot().get("raw-socket-probe").message())
                .contains(threshold + "/" + threshold);
    }

    @Test
    void successfulProbeClearsFailuresBeforeThreshold() {
        monitor.handleProbeResult(false);
        monitor.handleProbeResult(true);
        monitor.handleProbeResult(false);

        verifyNoInteractions(lifecycleManager);
        assertThat(vitals.snapshot().get("raw-socket-probe").status())
                .isEqualTo(VitalsRegistry.Status.DOWN);
    }

    @Test
    void repeatedOutagesCanTriggerRecoveryAgain() {
        for (int i = 0; i < properties.getFailureThreshold() * 2; i++) {
            monitor.handleProbeResult(false);
        }

        verify(lifecycleManager, times(2)).ejectAndResurrect("RAW_SOCKET_UNRESPONSIVE");
    }
}
