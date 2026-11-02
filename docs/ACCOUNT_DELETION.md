# Account deletion

App Store Review Guideline 5.1.1(v) and Google Play's account-deletion policy
require apps that let users create an account to offer in-app deletion. BiciRadar
implements it from **Profile → Cuenta → Eliminar cuenta**, available only while
a session is active.

## Flow

1. The user confirms the deletion in a dialog that states what is removed and
   that the action cannot be undone.
2. `DeleteAccountUseCase` calls `DELETE /v1/me` with the current access token.
3. The backend removes every row tied to the account (`routines`,
   `routine_steps`, `routine_windows`, `subscriptions`, `devices`,
   `event_outbox` and the `users` row).
4. The app clears the local routine copy and resets the routine sync
   bookkeeping (`routine_dirty_ids`, `routine_pending_deletes`,
   `routine_sync_state`), so deleted data is not uploaded again on the next
   login.
5. The OIDC session is closed and the stored credentials are discarded.

If the backend call fails, nothing is deleted locally and the session is kept so
the user can retry.

## Backend requirement: delete the Auth0 identity

The client cannot delete an Auth0 user. `DELETE /v1/me` must also remove the
identity with the Auth0 Management API (`DELETE /api/v2/users/{id}`) using a
machine-to-machine token for that tenant. If only the SQLite rows are deleted,
the same credentials can sign in again (and the user row is recreated on the
next request), which an App Store reviewer can detect: the account would not
actually be gone.

The app calls the endpoint unconditionally, so the backend change is the only
pending piece outside this repository.

## Local data that intentionally remains

Favorites, home/work stations and saved-place alert rules are device-local and
are not account data: they survive account deletion and are re-uploaded only if
the user signs into a (new) account. The confirmation dialog and this document
describe the account data that is deleted.

## Review notes

Add this to `APP_REVIEW_NOTES`:

> Account deletion: sign in, open the Profile tab and tap «Eliminar cuenta».
> The app calls DELETE /v1/me, removes the account data and signs out. The app
> also works without signing in; no account is required to browse stations.
