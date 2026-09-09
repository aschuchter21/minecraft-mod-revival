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

## Current development state

The mod now has a compiling persistent network foundation plus the first modular station implementation:

- Mission Control Console block and block entity.
- Server-wide `SpaceNetworkSavedData` shared across dimensions.
- Persistent infrastructure nodes and cargo routes.
- Station Controller with face-connected module discovery.
- Habitation, Life Support, Docking Port, Solar Array and Cargo Storage modules.
- Derived station budgets for crew capacity, power, oxygen, docking and cargo.
- Persistent station state registered into the same network Mission Control uses.
- Automatic cleanup when Mission Control or a Station Controller is removed.

All block art is intentionally temporary developer art while the systems are being built.

See `docs/ARCHITECTURE.md` and `docs/ROADMAP.md` for the planned systems.

## Building

This project is intentionally self-contained under `projects/orbital-industries`.

```bash
gradle build
```

The long-term intent is for Orbital Industries to remain its own mod ID and distributable jar even while it is developed beside the Galacticraft Forge port.
