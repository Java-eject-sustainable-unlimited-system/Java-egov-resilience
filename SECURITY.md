# Security Policy

## Project status
Jesus_system is **Alpha** software. It is a research project and must not be used to access or control real power equipment without authorisation.

## Supported versions
Only the latest commit on `main` receives security fixes.

## Reporting a vulnerability
Please report privately using GitHub's **Report a vulnerability** (Security → Advisories) on this repository. Do not open public issues for security problems.

Include affected files/versions, reproduction steps, and impact. We aim to acknowledge reports within 7 days and to tell you whether the report is accepted or declined, with a fix or mitigation plan, as it progresses.

## Built-in safeguards
- The watchdog's destructive auto-configuration requires `jesus.watchdog.enabled=true`.
- Manual ejection API requires the `X-Watchdog-Token` header, compared against the configured token; blank tokens are rejected.
- `HttpVitalsProbe` only contacts allowlisted origins (default `http://localhost:8080`).
- CI uses read-only permissions, CodeQL scanning, and a checksum-pinned OpenJML download.
