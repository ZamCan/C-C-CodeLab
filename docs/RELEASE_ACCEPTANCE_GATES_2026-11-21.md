# CodeLab release gates — verified 2026-11-21

## Confirmed backend inventory
- Appwrite project: `codelab-zamcan` in region `fra`.
- TablesDB database: `codelab-learning`.
- Existing private table: `lesson-progress`, rowSecurity=true, authenticated-user create permission.
- Existing columns: `course`, `lesson`, `completed_at`, `schema_version`.
- Appwrite currently reports zero Auth users, messaging resources and functions.
- OAuth provider inspection returned 401 `general_unauthorized_scope` (missing `project.oauth2.read`). **Do not claim OAuth is configured.**

## Required functional acceptance checks
1. Google OAuth: confirm Google provider enabled in Appwrite, credentials and allowed callback, Android deep link, Google testing audience, successful Account.get, logout, cold-start restore and cancellation.
2. Compiler: verify all bundled WASM assets exist in APK; run Hello World, stdin, diagnostics, C and C++ headers, multi-file linking, cancellation, timeout and offline airplane-mode execution on supported Android devices. Never substitute simulated output.
3. Progress: check private row permissions and per-user isolation, offline writes and sync conflicts; avoid cross-account data disclosure.
4. Messaging: Appwrite project has no messaging provider/topic. Configure delivery provider and push target registration before claiming remote push; obtain notification consent and test foreground/background/killed-app behaviour. Local StudyReminders are separate.
5. Branding: check single launcher vector, monochrome notification icon, minimal Compile•Learn C/C++ splash, dark/light themes and accessible contrast.
6. Release: run Gradle build and tests, install APK, smoke-test on device, inspect merged manifest and packaged resources, then sign and verify release artifact.

## Manual prerequisites
- Supply test Google accounts only after consent screen is confirmed in Testing mode; never send passwords.
- Enable appropriate Appwrite console permissions for OAuth inspection or verify in Console.
- Configure Google OAuth provider credentials, Google audience, and Android application registration.
- Configure FCM/Appwrite messaging provider credentials and a test Android device token, if remote push is required.
- Provide signing key through a secure local build process, not source control.

Do not mark any gate passed without recorded device/build evidence.
