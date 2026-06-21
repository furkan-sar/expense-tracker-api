# Synchronization Strategy

## Goal

Allow users to recover their data after:

* Device change
* Reinstall
* Application reset

## Source Of Truth

Backend database is the primary source of truth.

## Mobile Storage

Mobile application may cache data locally.

Local database is not the source of truth.

## Sync Process

1. User logs in.
2. Mobile application downloads latest data.
3. Mobile application stores data locally.
4. User performs operations.
5. Changes are sent to backend.
6. Backend persists data.

## Future Enhancements

* Offline mode
* Conflict resolution
* Background synchronization
