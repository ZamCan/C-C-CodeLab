# CodeLab — physical-device preview and release acceptance checklist

This is an evidence checklist, not a release announcement. Check the [GitHub Actions Android verification](https://github.com/ZamCan/C-C-CodeLab/actions) run on the commit you install. A passing build does **not** prove the offline compiler or cloud authentication works.

## Before downloading a preview
- [ ] GitHub Actions `Android verification` **android** job green on the exact commit; `testDebugUnitTest`, `assembleDebug`, `lintDebug` completed.
- [ ] Download `codelab-debug-apk` artifact from that run, unzip, install `app-debug.apk` on Android 8+; check package `com.zamcan.codelab`. Debug APK is for evaluation, not production distribution.
- [ ] Record commit SHA, APK SHA-256, Android model/version, installer warnings, and any crashes.
- [ ] If the job fails, download `android-diagnostics`, fix compilation/lint/tests, and rebuild before installation.

## Offline and native UI gates
- [ ] Install with Wi-Fi/mobile data disabled. First launch, guest lessons, compiler and projects open without account or internet.
- [ ] Compile/run C `printf("Hello, World!\\n")` and C++ `std::cout << "Hello, World!\\n"` with actual stdout, exit code and real diagnostics for invalid code.
- [ ] Verify no fake compiler outputs, no server execution, no missing toolchain errors; check stdin, timeout and safe sandbox behavior.
- [ ] Save/edit multiple files; close/restart app and verify exact source and local progress.
- [ ] Test light/dark themes, navigation, back button, orientation, font scaling, keyboard, scrolling and accessibility.
- [ ] Test 10 C and 10 C++ lessons and guided projects against curriculum spec; report missing/placeholder content.

## Account-wide identity gates
- [ ] Google OAuth provider **enabled** in Appwrite project `codelab-zamcan` with actual Google Cloud client ID/secret and exact redirect URL.
- [ ] Appwrite Android SDK OAuth callback and `Account.get()` verified on physical device.
- [ ] Cloudflare exchange endpoint exists, validates short-lived Appwrite JWT server-to-server, maps canonical learner, issues revocable Cloudflare session. Existing standalone verification adapter is **not** sufficient.
- [ ] Two separate Google accounts on one phone never see each other's progress, exams or certificates; guest progress merges only after explicit consent.
- [ ] Sign-out, offline transitions, token expiration, account deletion, wrong-project JWT and Appwrite outage fail safely.
- [ ] Cloudflare exams and certificate issuance remain feature-gated until reviewed exam bank, verified legal identity, signing keys, eligibility and backend tests pass.

## Distribution and privacy
- [ ] Consent and privacy notice checked; ads not shown deceptively or during timed exams.
- [ ] Release signing key is private and backed up securely; versionCode increased; release build signed and independently tested.
- [ ] Review package permissions, no credentials or API keys inside APK, data deletion and privacy contact.
- [ ] Only publish after all mandatory gates pass and a human approves real device evidence.

## Manual owner tasks
1. In Google Cloud Console create OAuth consent screen and Web OAuth client; enter the Appwrite-provided callback URL. Keep Google OAuth client secret private.
2. In Appwrite Auth settings enable Google OAuth with credentials. Appwrite project and TablesDB resources are already provisioned.
3. Install the CI-generated debug APK on a real Android phone and return screenshots, exact compiler stdout/stderr and any crashes.
4. Approve branding, privacy policy, release signing and store distribution only after evidence.

## Engineering agents' next queue
1. Implement Appwrite Android SDK + OAuth callback + verified session; never present a nonfunctional sign-in button as complete.
2. Implement Cloudflare D1 external identity migration, Appwrite token exchange endpoint, canonical learner mapping, revocable sessions and end-to-end tests. Keep `AUTH_ENABLED`, `EXAMS_ENABLED`, `ISSUANCE_ENABLED` gated until tested.
3. Implement owner-scoped Appwrite sync with explicit row permissions, local account separation, conflict recovery and deletion.
4. Deliver real offline Android C/C++ compiler and verify on physical devices.
5. Fix any GitHub Actions Android failures, produce evidence-backed debug APK and then prepare a separately signed release candidate.

## Engineering update — 2026-11-18
- Appwrite SDK 31.0.1 dependency and `io.appwrite.views.CallbackActivity` with `appwrite-callback-codelab-zamcan` were committed to `main`. These are configuration scaffolding, not a verified login.
- The Appwrite-provided Google SVG was converted to an Android vector and displayed in the learning-progress entry. The entry does **not** start Google OAuth yet.
- Academic references and truthful technology acknowledgements were added; no third-party sponsorship is claimed.
- Connected Appwrite provider readback reports Google enabled and a nonempty client ID, but returned `clientSecret` is empty. Confirm saved secret privately in console and perform an actual OAuth round-trip.
- Next implementation: Android account/session API, launch browser OAuth, resume callback, verify `Account.get()`, logout and user separation, then CI and physical-device tests. Do not turn on cloud progress sync until backend authorization is enforced.

## Status as of 2026-11-18
Appwrite project, Android platform and progress table exist. The Appwrite JWT verification adapter and tests have been committed but the tests have not been verified passing. Google provider was subsequently observed enabled; its returned client secret was empty, and authentication remains untested. Cloudflare exchange and full Android OAuth are not yet implemented. A production APK and working offline compiler are not verified.
