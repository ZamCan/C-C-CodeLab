# Live Appwrite setup — 2026-11-17
The connected owner Appwrite account was used to create the following actual resources (not placeholders):

- Endpoint: `https://fra.cloud.appwrite.io/v1`
- Project: `codelab-zamcan` (CodeLab C & C++), region `fra`
- Android platform: `android-codelab`, application ID `com.zamcan.codelab`
- TablesDB database: `codelab-learning`
- Table: `lesson-progress` with `rowSecurity=true`, table permission `create("users")` only; columns `course` varchar(40), `lesson` varchar(100), `completed_at` bigint, `schema_version` integer.

**Critical:** table-level create for signed-in users does NOT automatically guarantee owner-only row permissions. Android code MUST create each row with explicit `read("user:<current-account-id>")`, `update("user:<current-account-id>")`, `delete("user:<current-account-id>")` permissions. Never allow any/guests read. Do not trust owner ID provided by client for access decisions. Test two distinct accounts, including guessing another row ID. The schema does not contain an owner column by design; authorization is enforced by Appwrite row permissions.

## Still required before working Google login
- Enable Google OAuth provider in Appwrite and configure Google Cloud OAuth client and exact Appwrite redirect URL. Google OAuth client secret is private; do not commit or paste it into GitHub.
- Wire the Appwrite Android SDK, OAuth callback activity, real session verification, sign-out, account-isolated local SQLite namespaces and opt-in sync.
- Add explicit row permission enforcement and tests. Ensure guest records never silently upload into another account.
- Test real sign-in and private sync on Android; no production readiness claims yet.
- Cloudflare credential issuance remains separately gated. AWS remains optional and disabled.

Do not confuse provisioned database infrastructure with completed authentication or synchronization.
