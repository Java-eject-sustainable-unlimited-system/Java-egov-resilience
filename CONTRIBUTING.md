# Contributing

Thanks for helping make Jesus_system more trustworthy. The project is **Alpha**: we prioritise safety and verifiability over new features.

## Ground rules
- Follow the [Code of Conduct](CODE_OF_CONDUCT.md). Report vulnerabilities privately per [SECURITY.md](SECURITY.md), not in public issues.
- Keep destructive actions (eject/resurrect) opt-in and guarded; never weaken the allowlist, token check, or `jesus.watchdog.enabled` default.
- Do not commit secrets or tokens.

## Development setup
- JDK 17 and Maven.
- Build and test: `mvn clean verify`

## Formal verification (OpenJML)
`FailureCounter` carries JML contracts that CI checks with a pinned OpenJML:

```
openjml --esc --source 17 src/main/java/com/jesus/system/monitor/FailureCounter.java
```

If you change a contract, update the matching JUnit test and [docs/TRACEABILITY.md](docs/TRACEABILITY.md).

## Pull requests
1. Fork and branch from `main`.
2. Keep changes small and focused; add or update JUnit tests.
3. Make sure `mvn clean verify` passes; CI also runs OpenJML and CodeQL.
4. Describe which requirement or safety property your change affects.
