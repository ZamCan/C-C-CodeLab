# Final public branding decision — 2026-11-14

**User-selected product name:** **CodeLab**.

## Public presentation
- Main app identity: **C/C++ CodeLab**. The word **CodeLab** is the distinctive product name; the **C/C++** prefix is immediately visible on the launcher/store identity, splash/wordmark, home, and marketing.
- Supporting line, when useful: **Learn • Code • Compile • Build**.
- Subtle secondary attribution: **by ZamCan Academy**. ZamCan Academy is the developer/umbrella education brand, not the prominent learner-facing app title.
- Preserve existing repository `ZamCan/C-C-CodeLab` and Android application ID unless a separately reviewed migration is needed. Do not rename repository or package solely for cosmetic reasons.
- Existing specs calling the app 'ZamCan Academy' describe the umbrella project; interpret learner-facing labels according to this decision. Do not remove the academy's extensible learning architecture.

## Logo and interface
- Design an original premium icon with distinctive C/C++ coding imagery, not a simple letter Z or generic clip-art. It must remain readable at small launcher sizes, with adaptive, monochrome, dark/light and store variants.
- Maintain premium native Android design, coherent navy/violet/blue with restrained gold, accessible light mode and readable syntax colors. Avoid overglowing, overcrowding, and tiny text.
- Home retains **Code Studio** and **Learning Academy** as two primary destinations; the public app title is **C/C++ CodeLab**.
- Show the subtle developer credit in About and optional splash footer, never competing with C/C++ branding.
- User approved the name, **not** a particular generated logo concept. Logo artwork still requires implementation and review.

## Advertising
- User describes an always-attached small ad: that is an anchored adaptive **banner**, not an interstitial. Respect content, exam and terminal exclusions and privacy/consent rules described in VISUAL_BRAND.md.

## Implementation checklist
- [ ] Launcher label / app_name = C/C++ CodeLab
- [ ] Splash/home wordmark uses C/C++ CodeLab
- [ ] Premium original adaptive icon assets
- [ ] About credit: by ZamCan Academy
- [ ] Screenshot tests for light/dark, small devices
- [ ] Update documentation/metadata without rewriting established project history
