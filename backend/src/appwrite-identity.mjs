import {ApiError} from './policy.mjs';

/**
 * Validate a short-lived Appwrite JWT by asking the configured Appwrite project
 * for the authenticated account. No client-supplied email or user ID is trusted.
 * This module never logs tokens and intentionally fails closed.
 */
export async function verifyAppwriteIdentity(jwt, options = {}) {
  const endpoint = options.endpoint || 'https://fra.cloud.appwrite.io/v1';
  const projectId = options.projectId || 'codelab-zamcan';
  const fetcher = options.fetcher || fetch;
  if (typeof jwt !== 'string' || jwt.length < 20 || jwt.length > 8192 ||
      !/^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$/.test(jwt)) {
    throw new ApiError(401, 'invalid_identity_token');
  }
  if (endpoint !== 'https://fra.cloud.appwrite.io/v1' || projectId !== 'codelab-zamcan') {
    throw new ApiError(503, 'identity_provider_misconfigured');
  }
  let response;
  try {
    response = await fetcher(endpoint + '/account', {
      method: 'GET',
      headers: {
        'X-Appwrite-Project': projectId,
        'X-Appwrite-JWT': jwt,
        'Accept': 'application/json'
      },
      signal: AbortSignal.timeout(5000),
      redirect: 'error'
    });
  } catch {
    throw new ApiError(503, 'identity_provider_unavailable');
  }
  if (response.status === 401 || response.status === 403) {
    throw new ApiError(401, 'invalid_identity_token');
  }
  if (!response.ok) throw new ApiError(503, 'identity_provider_unavailable');
  let account;
  try { account = await response.json(); }
  catch { throw new ApiError(503, 'invalid_identity_response'); }
  if (!account || typeof account.$id !== 'string' ||
      !/^[A-Za-z0-9._-]{1,36}$/.test(account.$id) || account.status !== true) {
    throw new ApiError(401, 'invalid_identity_account');
  }
  return Object.freeze({
    provider: 'appwrite',
    subject: account.$id,
    externalRef: 'appwrite:' + account.$id,
    displayName: typeof account.name === 'string' ? account.name.slice(0,100) : ''
  });
}
