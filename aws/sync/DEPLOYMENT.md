# CodeLab AWS progress sync deployment runbook

**Not deployed.** Do not execute until account subscription is active, budget alarms are configured, and owner approves resource creation. Android offline learning works without this service.

## Prerequisites

- AWS access with DynamoDB, Cognito, Lambda, API Gateway, CloudFormation, and IAM permissions.
- Choose one AWS Region and keep the DynamoDB table, Cognito pool, and Lambda in that Region.
- Create an AWS Budget and billing notifications before deploying. The template can incur AWS charges.
- Configure a Cognito User Pool and a **public** application client without a client secret. Use Authorization Code + PKCE in the Android system browser. Set exact app-owned redirect URIs; avoid implicit flow and embedded WebViews.
- Configure Login with Amazon and Google federation in Cognito only after provider verification and approved redirect domains. Do not commit client secrets, tokens, AWS credentials, or signing keys.

## Review and deploy (after approval)

From the repository root, with AWS SAM CLI installed:

```sh
# 1. Deploy the progress table after reviewing its provisioned capacity.
aws cloudformation deploy \
  --stack-name zamcan-codelab-progress-data \
  --template-file aws/progress-table.yaml \
  --parameter-overrides TableName=zamcan-codelab-progress

# 2. Build and deploy the Lambda API using your real Cognito identifiers.
cd aws/sync
npm install
npm test
sam validate --lint --template-file template.yaml
sam build --template-file template.yaml
sam deploy --guided --capabilities CAPABILITY_IAM
```

In SAM guided parameters, provide `ProgressTableName`, `CognitoUserPoolId`, and `CognitoAppClientId`. The table name must match the previously deployed table. Review the change set, execution role and charges before approval. Never use the AWS root identity for deployment.

## API contract

- `GET /v1/progress`: return authenticated learner snapshot.
- `PUT /v1/progress`: validate and union lesson completions; optimistic revision conflict returns HTTP 409.
- `DELETE /v1/progress`: remove only the authenticated learner's cloud progress. Does **not** delete Cognito account.
- Header: `Authorization: Bearer <Cognito ID token>`. Server verifies Cognito issuer, audience, signature, and token use. Never accept raw Google/Amazon tokens.
- Only HTTPS endpoint is valid. Do not embed bearer tokens in URLs or logs.

## Required integration checks

1. Missing, expired, foreign-pool, wrong-audience, and access-token requests return 401.
2. Learner A cannot read/write/delete learner B's progress.
3. Two devices preserve completed lessons through concurrent updates and retry HTTP 409.
4. Offline edits remain local; failed uploads never remove local progress.
5. Oversized requests and responses fail safely; review 240 KB current snapshot limit.
6. Explicit cloud deletion works, while local progress is unchanged.
7. Configure throttling and monitor costs, errors and latency before enabling public access.
8. Test real Android PKCE sign-in and account logout; do not mark cloud UI active before these pass.

## Release limitations

This template provisions the API and Lambda, but **does not provision** Cognito, federated providers, AWS Budgets, account deletion, Android OAuth screens, or production monitoring. It is a deployment candidate, not a certified production system.
