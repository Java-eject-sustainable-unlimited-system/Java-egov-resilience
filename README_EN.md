📄 README_EN.md (Global Edition)


# ✝️ J.E.S.U.S (Java Ejectable Survival Unlimited Sustainable System)

> A Java 17 / Spring Boot watchdog prototype for legacy eGovFrame and enterprise systems.

---

## 🛡️ Overview
**J.E.S.U.S** is an early-stage Java 17/Spring Boot watchdog prototype for legacy enterprise services. It combines HTTP and TCP probes, optional JDBC checks, watchdog-JVM diagnostics, and disk/I/O checks. After configured failure conditions, it can forcibly terminate matching same-user Java processes and ask Nix to start the configured flake application.

This is not a production-certified recovery system. Recovery is destructive, depends on process matching and Nix being available, and should only be evaluated in a controlled environment. The watchdog's JVM and local filesystem checks describe the process and working directory where the watchdog itself runs; they do not inspect a remote target's JVM or disk.

---

## ⚡ Key Features

* **Layered probes**: HTTP(S) health endpoint, raw TCP connectivity, optional JDBC connection validation, watchdog-JVM heap/deadlock diagnostics, and local disk/write checks.
* **Threshold-based recovery**: Most probes require repeated failures before requesting ejection; a detected JVM deadlock and critically low disk space trigger immediately.
* **Actuator health**: `/actuator/health` reports the watchdog's aggregate monitor vitals. The HTTP endpoint it probes is the monitored target's endpoint.
* **Optional manual control**: `POST /api/jesus/eject-and-resurrect` is authorized by the `X-Watchdog-Token` header and is disabled unless a non-empty token is configured.

---

## 📂 Directory Structure

```text
Jesus_system/
├── .github/
│   └── workflows/
│       └── ci.yml                     # GitHub Actions CI/CD Pipeline
├── flake.nix                          # Multi-platform Nix shell & build architecture
├── pom.xml                            # Maven standard build config (Spring Boot 3.3.0)
└── src/
    └── main/
        ├── java/com/jesus/system/
        │   ├── JesusApplication.java              # Bootstrap Main
        │   ├── JesusWatchdogAutoConfiguration.java # Spring Boot Starter Auto-Configuration
        │   ├── config/
        │   │   └── WatchdogProperties.java        # Externalized jesus.watchdog.* settings
        │   ├── core/
        │   │   ├── ProcessLifecycleManager.java   # Single Eject & Nix Resurrection Core
        │   │   └── VitalsRegistry.java            # Shared Monitor Vitals Registry
        │   ├── monitor/
        │   │   ├── WatchdogAgent.java             # 0.5s Actuator Ping & Ejection Loop
        │   │   ├── AbyssProbeMonitor.java         # Raw TCP Socket Low-Level Probe
        │   │   ├── EgovWatchdogMonitor.java       # eGovFrame DB & WAS Dual Verification
        │   │   ├── JvmDiagnosticsMonitor.java     # JVM Heap & Deadlock Preemption Guard
        │   │   ├── DiskIoGuardMonitor.java        # Disk I/O & Read-Only Lock Guard
        │   │   └── HttpVitalsProbe.java           # Shared HTTP Vitals Probe
        │   ├── api/
        │   │   └── WatchdogCommandController.java # Emergency Manual Control REST API
        │   └── health/
        │       └── WatchdogHealthIndicator.java   # Health Indicator Backed by Real Vitals
        └── resources/
            ├── application.yml                    # Server port (9090) & watchdog thresholds
            └── META-INF/spring/                   # Auto-configuration registration (starter mode)



🚀 Quick Start & Build


1. Enter Nix Environment (Recommended)


nix develop



2. Local Maven Build & Packaging


mvn clean verify



3. Run Application


mvn spring-boot:run



📦 Adopting into an Existing Spring App (Starter Mode)


Any Spring Boot 3 application (e.g. a modernized eGovFrame service) can embed the full watchdog into its core with a single dependency:


<dependency>
    <groupId>com.jesus.system</groupId>
    <artifactId>jesus-watchdog-agent</artifactId>
    <version>1.0.0</version>
</dependency>


Just having it on the classpath auto-registers every monitor, the health indicator, and the REST API. Disable anytime with jesus.watchdog.enabled=false. Configure `JESUS_WATCHDOG_MANUAL_CONTROL_TOKEN` to enable the emergency endpoint; leave it unset to keep manual ejection disabled.


⚠️ javax-era eGovFrame (Spring Boot 2.x) hosts cannot embed it directly due to the Jakarta namespace split — keep running the watchdog in standalone (sidecar) mode for those systems.



🧪 Test & Formal Verification (Nix)


1. Run the full test suite inside the reproducible Nix environment


nix develop -c mvn clean verify



2. Build the deployment artifact as a pure Nix derivation (offline, hash-pinned dependencies)


nix build .#jesus-server-17



ℹ️ The first nix build fails with an mvnHash mismatch error — paste the printed hash into flake.nix (mvnHash = "...") and re-run. From then on the build is fully reproducible.



📊 CI/CD Status


J.E.S.U.S CI/CD Pipeline


---


