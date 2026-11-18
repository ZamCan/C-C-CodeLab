# CodeLab Android AdMob release integration

## Confirmed identity
- AdMob registered app display name: **Learn C/C++ - CodeLab** (confirmed by developer).
- Android application ID: `com.zamcan.codelab`.
- Existing Android dependencies: Google Mobile Ads SDK `24.7.0`, Google UMP `3.2.0`.
- Existing `CodeLabAds` only requests Google's demonstration banner in **debug** builds after explicit local opt-in and a UMP consent check. This is not a production ad implementation.
- Current AndroidManifest.xml uses Google's **sample** application ID. Do not ship a monetized release with this value.

## Release gates (do not mark complete without evidence)
1. Developer supplies the actual AdMob **App ID** (`ca-app-pub-...~...`) and separately creates banner/interstitial/rewarded **ad unit IDs** (`ca-app-pub-.../...`). Do not confuse these IDs.
2. Move App ID and ad unit IDs into validated build configuration; release must fail closed when missing, never silently substitute demo IDs.
3. Implement UMP update on each launch, consent form, privacy-options entry point where required, and ads only after `canRequestAds()`. Test relevant privacy regions.
4. Use banner only on appropriate screens, and interstitial only at natural transitions with frequency caps; rewarded ads must be explicitly user-initiated, with reward granted only from verified SDK callback. Keep learning and compiler functional offline.
5. Test demo ads and Ad Inspector on physical Android device, then test developer-owned ad units on registered test devices/app-level test mode. Never click live ads to test.
6. Verify actual offline C/C++ compilation, safe execution, diagnostics, persistence, Google OAuth and cloud learner authorization. These remain independent release gates.
7. Verify Cloudflare certificate issuance/verification backend deployed with secrets stored server-side, and run positive/negative verification tests before enabling.
8. CI: pass unit tests, lint, debug assembly and release assembly. Build signed production APK with privately held signing credentials; install and test on actual Android devices. Record APK hash, version and test evidence.
9. Publish publicly to a supported Android app store (Amazon Appstore is supported); then link the published listing in the **existing** AdMob app and complete readiness review and publisher/payment verification. Do not claim production ad serving until AdMob shows **Ready**.

## Account owner action
In https://apps.admob.com/ open **Apps → Learn C/C++ - CodeLab → App settings** to copy the **App ID**. Then **Ad units → Add ad unit** for banner, interstitial and rewarded placements. The IDs are identifiers, not login passwords; never share account credentials, OAuth client secrets or signing keys.

Official references:
- https://developers.google.com/admob/android/test-ads
- https://developers.google.com/admob/android/privacy
- https://support.google.com/admob/answer/10564477
- https://support.google.com/admob/answer/9989980
