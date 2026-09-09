# Orbital Industries Handoff

## Branch

`orbital-industries-foundation`

## Current development checkpoint

The branch now contains the station health + structural ownership slices. Read the branch ref for the exact current head; the implementation described here is committed on this branch.

## Completed systems

- Standalone Forge 1.20.1 / Forge 47.4.10 / Java 17 project.
- Mission Control Console block entity, menu and client screen.
- Server-authoritative persistent `SpaceNetworkSavedData`.
- Persistent infrastructure nodes and cargo-route primitive.
- Station Controller with contiguous connected-module discovery.
- Habitation, life support, docking, cargo storage and solar station modules.
- Persistent `StationState` module/resource aggregation.
- Derived station operational health model for incomplete, power-deficit, oxygen-deficit, combined-deficit and operational states.
- Mission Control synchronization of station count, operational station count, module count, aggregate net power and aggregate net oxygen.
- Persistent station-module ownership claims keyed by dimension + block position.
- Controller rescans reject module networks already claimed by another station.
- Claims are replaced on successful rescan and released when a station controller is removed.

## Architectural decisions

- Simulation remains network-first, world-backed and server-authoritative.
- Station operational status is derived from persisted resource/module state rather than persisted independently.
- Station module ownership is persisted in the same world-level saved data as station identities, so overlapping controllers are rejected consistently across reloads.
- Mission Control reads aggregate values directly from `SpaceNetworkSavedData`; later detail screens and alerts should use the same source of truth.
- Galacticraft integration remains isolated behind future adapters; do not couple core saved state directly to unstable port APIs.
- Physical logistics must remain physical; route data coordinates movement rather than teleporting inventories.

## Build / CI status

The Orbital Industries workflow uses JDK 17 and Gradle 8.8. The health slice commit `066dd5def03f5ccc2a2287c8ab9eb3214f66c3a0` triggered the build workflow. Verify the newest workflow run after the structural ownership commit and fix any failures before further expansion.

## Known issues / unfinished work

- Ownership claims prevent overlapping controllers but do not yet enforce richer geometry rules such as minimum module count, required core modules, disconnected hull validation, or controller-to-controller separation.
- Station budgets are abstract integer simulation values, not Forge energy/oxygen/inventory capabilities.
- Mission Control shows aggregate health but does not yet provide per-station selectable cards/detail lists.
- Cargo routes have no manifests, scheduler, vehicle, fuel/range or physical shipment state.
- No orbital infrastructure, colony controller or Galacticraft adapter exists yet.

## Next recommended work

1. Add station validation requirements beyond ownership: required life support/power for crewed stations and explicit validation issues.
2. Add Mission Control station list/detail synchronization on top of the operational health model.
3. Introduce real station cargo inventory capability and then power/oxygen capability adapters.
4. Begin the first orbital infrastructure node (satellite chassis + payload model) once station structure rules are stable.
