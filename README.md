Electrical eGov Resilience

Free and Open-Source Research for Resilient Electrical Infrastructure

Free. Open. Verifiable. Resilient.

Our Mission

Electrical eGov Resilience is an open-source initiative dedicated to improving the security, reliability, and recoverability of critical electrical infrastructure.

Built upon the concepts of J.E.S.U.S (Java Ejectable Survival Unlimited Sustainable System), the project aims to combine Java-based system monitoring, eGovFrame integration, and electrical-grid resilience simulations.

Our mission is to make defensive infrastructure research freely accessible to developers, researchers, students, and institutions worldwide.

Core Principles

- Free and Open: Public source code and transparent research.
- Defense First: Prevention, monitoring, and recovery.
- Safety by Design: No direct control of operational power-grid equipment.
- Open Verification: Reproducible tests and independently reviewable results.
- Human Accountability: Safe recovery decisions with appropriate oversight.
- Global Collaboration: Contributions welcomed from around the world.

Technology Direction

- Java 17
- Spring and eGovFrame integration
- J.E.S.U.S-inspired system monitoring
- Microgrid outage simulation
- Backup-power endurance analysis
- Auditable recovery planning
- Automated testing and security review

Formal Verification

OpenJML specifications and static checking are used for the failure counter's safety properties. The CI workflow runs OpenJML ESC on `FailureCounter.java`, checking its non-negative invariant, saturating failure increment, recovery, threshold, and reset contracts. This currently verifies the failure counter only; it does not formally verify the Spring framework or the complete application.

Project Status

Early-stage development.

Development branch: "Dev"

Default branch: "main"

The software is not currently certified or ready for operational deployment. Compatibility and integration testing remain development objectives.

Proposed Electrical Resilience Foundation

Our long-term vision is to establish an independent, international, nonprofit open-source organization supporting research, education, verification, and collaboration in electrical infrastructure resilience.

The foundation has not yet been established.

Licensing

This project uses the MIT License. Commercial and noncommercial use are permitted under its terms. Copyright and license notices from upstream projects must be preserved.

한국어 소개

Electrical eGov Resilience는 전력망 사이버보안과 국가 핵심 인프라의 복원력 향상을 목표로 하는 무료 오픈소스 연구 프로젝트입니다.

Java, 전자정부 표준프레임워크, Jesus_system 기반 상태 감시 기술을 활용하여 가상 전력망의 장애 분석과 안전한 복구 지원 기술을 연구합니다.

실제 전력설비에 대한 무단 접근이나 자동 제어가 아닌 예방, 감시, 검증, 복원력 향상에 집중합니다.

장기적으로 국제 개발자와 연구자가 공동 운영하는 독립적인 비영리 오픈소스 재단으로 발전하는 것을 목표로 합니다.

함께 검증하고, 함께 복원력을 높입니다.

See [CONTRIBUTING.md](CONTRIBUTING.md), [SECURITY.md](SECURITY.md) and [docs/TRACEABILITY.md](docs/TRACEABILITY.md).
