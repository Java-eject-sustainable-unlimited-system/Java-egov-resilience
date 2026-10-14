# Accessibility

We want people to be able to read, use, and contribute to this project regardless of how they access information. This project is an early-stage Java watchdog with command-line workflows and documentation; it does not currently provide a graphical user interface.

## Priorities

- Write documentation in clear language with descriptive headings and links.
- Keep instructions and important information available as text, not only as images or color.
- Preserve readable Markdown structure, including heading order and meaningful table headers.
- Avoid adding user-facing interaction that depends only on a mouse, color, or visual cues.

These are project priorities, not a claim of conformance to an accessibility standard. No formal accessibility evaluation has been completed.

## Contributor expectations

When changing user-facing documentation or adding an interface, consider keyboard use, screen-reader reading order, text alternatives, and clear error messages. Describe any accessibility checks performed in the pull request, and report known limitations rather than implying that a change has been fully evaluated.

## Reporting accessibility issues

Please open an issue in this repository describing the barrier and the task you were trying to complete. Include relevant page or file names, observed behavior, and browser, operating system, terminal, or assistive technology details when useful. Screenshots and recordings are optional. Do not include sensitive personal information; report security vulnerabilities privately as described in [SECURITY.md](SECURITY.md).

There are no formal severity labels or response-time commitments for accessibility reports. Maintainers will triage issues based on how much they prevent people from reading, using, or contributing to the project and provide updates in the issue as work progresses.

## Ownership and maintenance

Repository maintainers are responsible for reviewing accessibility reports and considering accessibility in documentation and user-facing changes. This statement should be updated when project interfaces, support, or evaluation practices change.

## Supported environments and known limitations

The project documents Java and Maven command-line workflows, but does not specify or claim support for particular operating systems, terminals, browsers, or assistive technologies. No combinations have been formally evaluated.

Some Markdown tables and decorative emoji are used in the README files; their presentation and announcement can vary between readers and assistive technologies. The project has not completed a systematic review of its documentation or command-line experience, so additional barriers may remain.

## Feedback and improvements

Suggestions for this statement or the project's accessibility practices can be submitted as repository issues. Please link to the relevant section and describe the improvement you recommend.
