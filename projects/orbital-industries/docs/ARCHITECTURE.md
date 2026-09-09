# Orbital Industries Architecture

## Core rule

The simulation is **network-first, world-backed and physically grounded**. Blocks and vehicles are the player's interface to a persistent interplanetary network; logistics should not become item teleportation with a space skin.

## 1. Mission Control

Mission Control is the shared control plane. It reads and manages persistent `SpaceNetworkSavedData`, eventually presenting tabs for Overview, Stations, Colonies, Satellites, Fleet, Logistics, Power, Resources and Missions.

The console registers itself as a persistent network node and reports server-authoritative network counts. The first dashboard now also exposes aggregate station health: operational station count, connected module count, net station power and net station oxygen. Future screens should consume this same saved model rather than inventing separate client-only state.

## 2. Persistent network nodes

`SpaceNetworkNode` is the common identity/location layer for infrastructure. Initial node types are:

- Mission Control
- Station
- Colony
- Orbital Depot
- Satellite
- Industrial Facility

Specialized station, colony, satellite and industry state will reference these node IDs so cross-system routing does not depend on block positions alone.

## 3. Space stations

Stations are controller-driven structures assembled from functional modules. Initial modules include habitation, docking, life support, cargo storage and solar generation.

`StationState` aggregates module capabilities into crew capacity, power generation/demand, oxygen production/demand, docking capacity and cargo storage. It also derives a server-authoritative `StationOperationalStatus` from those budgets:

- `INCOMPLETE` — controller has no connected station modules.
- `POWER_DEFICIT` — station demand exceeds generation.
- `OXYGEN_DEFICIT` — occupied capacity exceeds oxygen production.
- `POWER_AND_OXYGEN_DEFICIT` — both critical budgets are negative.
- `OPERATIONAL` — the station has modules and neither critical budget is negative.

Operational status is deliberately derived instead of separately persisted, avoiding stale state after module rescans or save migration.

Next station work should introduce structural ownership/validation, then replace abstract budgets with Forge energy, oxygen and inventory capabilities.

## 4. Colonies

Colonies will use the same network identity model but add settlement state: population, housing, food, water, oxygen, power, morale/comfort, medical support, research and industrial specialization.

Shortages should create operational pressure without turning the mod into a spreadsheet. Automation is optional; manual supply remains possible.

## 5. Logistics

`CargoRoute` is the first persistent routing primitive. Routes connect network nodes and will later carry schedules, vehicle class, mass limits, fuel cost, manifests, launch windows and route status.

Cargo is intended to remain physical: containers are loaded, transported and unloaded. Route state coordinates that movement; it does not teleport inventories.

## 6. Orbital infrastructure

Satellites and orbital facilities are network nodes with orbital metadata. Planned roles include communications, navigation, resource scanning, weather observation, solar power, relay coverage, fuel storage and deep-space communications.

## 7. Galacticraft integration

The loader dependency is optional during the foundation stage. Galacticraft-specific adapters will live behind an integration package so Orbital Industries owns its simulation model and can tolerate Galacticraft API/port changes while development continues.

Expected integration points include celestial bodies, rocket launch/orbit handoff, fuel capability, dimensions, oxygen/life-support concepts and future docking/fleet hooks.

## 8. Server authority

Persistent simulation is server-authoritative. Clients receive view models/snapshots through menu data and later dedicated packets for Mission Control screens. This keeps multiplayer state deterministic and prevents UI code from becoming the source of truth.
