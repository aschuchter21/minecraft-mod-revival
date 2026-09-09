# Orbital Industries Handoff

## Branch

`orbital-industries-foundation`

## Current development checkpoint

Base branch head at the start of this slice: `46c9fb8ebee00a6205aff6f0b3c301d7da74367b`.

This handoff is maintained in the same commit as the code whenever practical. For the exact newest head, read the branch ref; the code checkpoint described below is the commit containing this file.

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

## Architectural decisions

- Simulation remains network-first, world-backed and server-authoritative.
- Station operational status is derived from persisted resource/module state rather than persisted independently, preventing stale health flags.
- Mission Control reads aggregate values directly from `SpaceNetworkSavedData`; later detail screens and alerts should use the same source of truth.
- Galacticraft integration remains isolated behind future adapters; do not couple core saved state directly to unstable port APIs.
- Physical logistics must remain physical; route data coordinates movement rather than teleporting inventories.

## Build / CI status

The branch is configured to run `.github/workflows/orbital-industries-build.yml` on Orbital Industries changes using JDK 17 and Gradle 8.8. Verify the newest workflow run after this checkpoint and fix compile failures before building additional systems.

## Known issues / unfinished work

- No structural ownership rules yet; two nearby controllers could conceptually claim overlapping module structures.
- Station budgets are abstract integer simulation values, not Forge energy/oxygen/inventory capabilities.
- Mission Control shows aggregate health but does not yet provide per-station selectable cards/detail lists.
- Cargo routes have no manifests, scheduler, vehicle, fuel/range or physical shipment state.
- No orbital infrastructure, colony controller or Galacticraft adapter exists yet.

## Next recommended work

1. Add station structural ownership and validation so a module network belongs to exactly one controller.
2. Add Mission Control station list/detail synchronization on top of the new operational health model.
3. Introduce real station cargo inventory capability and then power/oxygen capability adapters.
4. Begin the first orbital infrastructure node (satellite chassis + payload model) once station ownership is stable.
