# Requirements Traceability

Maps each requirement to its implementation, formal contract, and tests. Keep this table updated when behaviour changes.

| ID | Requirement | Implementation | Contract / verification | Tests |
|----|-------------|----------------|-------------------------|-------|
| R1 | Failure counter never goes negative | `FailureCounter` | JML `invariant consecutiveFailures >= 0` (OpenJML ESC in CI) | `FailureCounterTest.gradualRecoveryNeverDropsBelowZero` |
| R2 | Threshold is reached after consecutive failures; reset clears the count | `FailureCounter` | JML `ensures` on increment/threshold/reset | `FailureCounterTest.reachesConfiguredThresholdAndCanBeReset` |
| R3 | Eject/resurrect only after consecutive TCP probe failures | `AbyssProbeMonitor` | Relies on R1–R2 | `AbyssProbeMonitorTest` |
| R4 | Health probe contacts only allowlisted origins | `HttpVitalsProbe`, `WatchdogProperties` | Review | `HttpVitalsProbeTest`, `WatchdogPropertiesTest` |
| R5 | Destructive watchdog is opt-in | `JesusWatchdogAutoConfiguration` | Review | `JesusWatchdogAutoConfigurationTest` |
| R6 | Manual ejection requires a valid token | `WatchdogCommandController` | Review | `WatchdogCommandControllerTest` |
| R7 | Duplicate resurrection triggers are skipped | `ProcessLifecycleManager` | Review | `ProcessLifecycleManagerTest` |
| R8 | Disk space is monitored | `DiskIoGuardMonitor` | Review | `DiskIoGuardMonitorTest` |
| R9 | Component health is aggregated and exposed | `VitalsRegistry`, `WatchdogHealthIndicator` | Review | `VitalsRegistryTest`, `WatchdogHealthIndicatorTest` |

Only R1–R2 are formally verified today; the rest are covered by tests and review.
