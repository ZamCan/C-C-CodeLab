# Optional AWS progress sync: deployment and security contract

Status: **infrastructure template and local serialization only; not a deployed online sync service**.

## Architecture
- Android SQLite is the source of truth for unsynchronized learning progress.
- Only learners who explicitly choose Online and complete Amazon or Google authentication may upload.
- Backend verifies provider token/code, establishes its own short-lived session, then derives the DynamoDB partition key from the verified internal learner ID. Never accept client-provided partition keys.
- Use `pk=LEARNER#<opaque server id>` and `sk=PROFILE` or `sk=PROGRESS#<course>#<lesson>`.
- Record each lesson as completed_at and an incrementing server revision. Merge completed lessons by set union and earliest valid completion time; no automatic deletion.
- Do not store access tokens, passwords, source code, ad identifiers or exam answer keys in DynamoDB progress items.
- Use a dedicated least-privilege AWS execution role for GetItem, Query, PutItem and UpdateItem on this table only. Never ship AWS credentials in the APK.
- Require TLS, authentication, per-user rate limits, payload size limits, schema validation, deletion/export endpoints, and audit events.
- No account linking by email alone. Linking Google and Amazon identities requires verified proof of both.
- Exam eligibility and certificates must remain server-authoritative and must **never** be inferred from client-synced lesson completion.
- Provisioned Standard table is selected to align with DynamoDB's published 25 GB free-tier allowance; throughput, transfer and other charges can still apply. Set AWS Budgets alerts before deployment.
- Do not enable online UI until AWS account, provider client IDs, redirect URIs, credentials, backend IAM role and integration tests are configured.

## Deployment
Review `aws/progress-table.yaml`, then deploy it in an AWS account and Region under the owner's control. No resources have been created by this repository commit.
