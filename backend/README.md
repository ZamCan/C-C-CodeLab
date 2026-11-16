# ZamCan certification service

Prepared implementation, **not deployed or production verified**. Use `wrangler.jsonc`; the older TOML example is historical. Authentication, exams and issuance default to disabled, the existing ZamCan Nexus account and database are explicitly bound, and the public origin remains a placeholder.

Implemented: passkey registration/login with user verification; hashed sessions; cookie CSRF protection; role checks; rate limits; trusted eligibility; server-selected 25-question exams; durable answers, deadline and finalization; three official names captured before starting; a 70% pass threshold; name reconfirmation before issuance; Ed25519 signed credentials; printable PDF with QR and attached signed record; active/revoked/invalid verification; English/Swahili dashboard; PKCE mobile exchange; append-only audit and immutable issued records. Android synchronization is not yet connected.

Run `npm ci`, `npm test`, `npm run check`, `npm run build`, and `npm run dry-run` with Node 22+. Integration tests use actual SQLite migrations, ephemeral test signing/authenticator keys, synthetic exam questions, PDF parsing and independent signature verification. These do not prove live Cloudflare or phone behavior. Tests never provision real students or production keys.

## Deployment and security gates

1. Owner-approved Wrangler OAuth has been verified for the Zamcannexus account. For future sessions use `npx wrangler login` and approve Cloudflare's browser OAuth flow. Never paste tokens into chat or source. Use the owner's account, not a temporary anonymous account.
2. Preserve the existing bound D1 database. Its schema matches 0001 but lacks migration history: do not replay 0001. Run `python3 tools/check_existing_d1.py` from the repository root, take a protected backup, baseline the original migration and apply only reviewed pending migrations. Set the actual HTTPS origin in `wrangler.jsonc`. Run `node tools/preflight.mjs` before deployment. Preserve production backups before future migrations.
3. Provision Ed25519 key material outside the repository. Upload the private JWK using `wrangler secret put ISSUER_PRIVATE_JWK` from protected local input. Publish only its public JWK in `issuer_keys`; set `ISSUER_KEY_ID` to the exact origin key identifier. Verify matching keys. Private keys must never enter build assets, logs or Android.
4. Provision the first admin through a controlled account-owner database operation after passkey enrollment. All public registrations always receive the student role. Review identity with appropriate evidence before approval. There is no automated account recovery: use synced passkeys and retain authenticator access. Document and test reviewed recovery before accepting real students.
5. Publish reviewed separate C/C++ course metadata, immutable versioned criteria and a private exam bank. The test bank is synthetic and must never become course content. Approve eligibility from trusted evidence, not Android progress. Practical grading remains unimplemented and blocks a full programming certification launch.
6. Test the deployed preview end to end: real phone passkey, account isolation, CSRF, name registration/reconfirmation, exam expiry/resume, scoring, concurrent submission, issuance/retry, PDF/QR, independent verification, revocation, key rotation and backups. Enable feature flags only after their relevant gates pass. Record actual URLs/results without secret data.

## Backup and operations

`tools/backup.mjs` encrypts/decrypts exported SQL with scrypt and AES-256-GCM. Supply a strong passphrase via the `ZAMCAN_BACKUP_PASSPHRASE` environment through secure local input, never command history. The automated test restores synthetic SQL and rejects wrong passphrases/tampering. A real encrypted off-provider D1 backup and restore drill is still required. Keep signing-key recovery separately controlled. Retain historical public keys; retire keys for rotation and mark compromised keys compromised. Revoke and reissue corrected certificates rather than editing them.

Before real learners: approve retention/deletion and identity-evidence policies, recovery and appeals procedures, privacy notice/contact, monitoring and capacity limits. No outside accreditation or Open Badges conformance is claimed. Current credential signing uses a VC2-style JWS profile; independent standards conformance testing remains pending.

Font: Noto Sans Regular from notofonts/noto-fonts (hinted/ttf/NotoSans/NotoSans-Regular.ttf), distributed with its bundled license in `public/fonts`. Dashboard assets are local; no external font/CDN requests are required.
