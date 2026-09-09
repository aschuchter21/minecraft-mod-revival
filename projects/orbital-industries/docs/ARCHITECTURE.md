# Orbital Industries Architecture

## Core rule

The simulation is **network-first, world-backed and physically grounded**. Blocks and vehicles are the player's interface to a persistent interplanetary network; logistics should not become item teleportation with a space skin.

## 1. Mission Control

Mission Control is the shared control plane. It reads and manages persistent `SpaceNetworkSavedData`, eventually presenting tabs for Overview, Stations, Colonies, Satellites, Fleet, Logistics, Power, Resources and Missions.

The initial console registers itself as a persistent network node and reports network counts. The future GUI will operate on the same saved model rather than inventing a separate client-only state.

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

Stations will be controller-driven structures assembled from functional modules. Planned modules include habitation, docking, life support, hydroponics, storage, fuel, research, refining, manufacturing, power generation and artificial gravity.

Station simulation will aggregate module capabilities into budgets for crew capacity, power, oxygen, storage, docking capacity and industrial throughput.

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

Persistent simulation is server-authoritative. Clients receive view models/snapshots through packets for Mission Control screens. This keeps multiplayer state deterministic and prevents UI code from becoming the source of truth.
