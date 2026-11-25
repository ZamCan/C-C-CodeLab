# Boot and landing — mandatory design reference

Reference: two user-supplied IBKR Mobile screenshots dated 2026-11-25. Place original screenshots in design/screenshots/ as boot-reference.jpg and landing-reference.jpg. They are inspiration only; do not reuse IBKR logo, artwork or trade dress verbatim.

## Implementation
- Replace existing CodeLab boot and account-choice screens with a unified native Android visual system: near-black midnight navy, original low-contrast flowing technical mesh in lower half, crisp white CodeLab wordmark and original logo.
- Splash: centered brand lockup, brief subtle motion, Android system splash coordinated with first activity; no artificial delay. Transition seamlessly to landing with same background.
- Landing: brand and concise value proposition in upper third; generous negative space; bottom-aligned full-width Google button with official multicolor G and outlined Continue as Guest button. Respect system insets, accessibility, font scaling, 48dp touch targets. Overflow for settings and help. No giant cards, redundant warnings, or webpage styling.
- Guest enters immediately with local progress; disclose local-only storage inline. Preserve actual OAuth flow and diagnose redirect_uri_mismatch separately; never label provider rejection as user cancellation.
- Create original vector/Canvas artwork, support reduced motion and low-end Android devices. Keep light theme coherent. Remove stale CompileLearn branding.
- Production agents must implement in main, not merely document; test cold/warm launch, back navigation, guest persistence, Google success/error/cancel, small screens and both themes. Capture real APK screenshots for comparison. Do not claim completion without device evidence.
