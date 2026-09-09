# Orbital Industries

Orbital Industries is a Forge 1.20.1 companion mod for Galacticraft focused on building a persistent space civilization after the player reaches orbit.

## Design pillars

- **Expanded space stations** — functional station controllers, habitation, life support, docking, power, industry, artificial gravity and orbital construction.
- **Orbital infrastructure** — satellites, relays, navigation constellations, scanners, solar arrays, fuel depots and communications networks.
- **Interplanetary logistics** — physical cargo, manifests, depots, automated routes, freighters, launch windows and supply chains.
- **Colonies** — population, food, water, oxygen, power, habitation, morale, research and industrial specialization.
- **Space industry** — refining, gas processing, zero-g manufacturing, mining support, fuel production and orbital assembly.
- **Mission Control** — one persistent network tying stations, colonies, spacecraft, satellites and logistics together.

## Target

- Minecraft 1.20.1
- Forge 47.4.10
- Java 17
- Galacticraft integration is optional at the loader level during early development so the project can be built and tested independently.

## Current milestone: Foundation

The first implementation provides:

- Forge project skeleton and metadata.
- Mission Control Console block and block entity.
- Persistent `SpaceNetworkSavedData` stored per server world.
- Generic persistent space-network nodes for mission control, stations, colonies, orbital depots, satellites and industrial facilities.
- Persistent cargo-route records.
- A right-click Mission Control network summary so the foundation is testable in-game before the full GUI lands.

See `docs/ARCHITECTURE.md` and `docs/ROADMAP.md` for the planned systems.

## Building

This project is intentionally self-contained under `projects/orbital-industries`.

```bash
gradle build
```

The long-term intent is for Orbital Industries to remain its own mod ID and distributable jar even while it is developed beside the Galacticraft Forge port.
