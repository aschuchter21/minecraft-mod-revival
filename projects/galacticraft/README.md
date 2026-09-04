# Galacticraft 5 — Forge 1.20.1 Revival

## Target

- Minecraft: 1.20.1
- Loader: Forge 47.4.10
- Java: 17
- Target pack: HBM MODERNIZED
- Mod line: Galacticraft 5

## Source references

This project is based on the MIT-licensed Galacticraft 5 rewrite, not the legacy Galacticraft 4 codebase.

Primary references:

1. TeamGalacticraft/Galacticraft — `minecraft/1.20.1`
   - Native Minecraft 1.20.1 implementation
   - Fabric Loader 0.14.21
   - Fabric API 0.85.0+1.20.1
   - Galacticraft 5.0.0-prealpha
   - Java 17

2. ColinVaughn/Galacticraft-1.21.1 — `main`
   - Community multi-loader conversion
   - Architectury common/fabric/neoforge modules
   - NeoForge implementation provides a reference for replacing Fabric-specific systems
   - Minecraft 1.21.1 / Java 21

## Port strategy

We are not attempting a direct Galacticraft 4 (1.12.2) port.

The working strategy is:

1. Preserve the official Galacticraft 5 Minecraft 1.20.1 game logic and content wherever possible.
2. Use the newer NeoForge platform implementation as a map for loader-specific conversions.
3. Build a Forge 47.4.10 platform layer for Minecraft 1.20.1.
4. Replace Fabric-only dependencies and APIs one subsystem at a time.
5. Keep the first milestone intentionally small: compile and reach the Minecraft title screen.
6. After startup works, restore gameplay systems incrementally: registries, networking, dimensions, oxygen, machines, rockets, rendering, GUIs, multiplayer, and integrations.

## Milestones

- [x] Identify official Minecraft 1.20.1 Galacticraft 5 source
- [x] Identify existing Forge-family / NeoForge conversion work
- [x] Create isolated port branch
- [ ] Confirm both upstream baselines compile in CI
- [ ] Establish Forge 47.4.10 project skeleton
- [ ] Compile shared/API layer against Minecraft 1.20.1
- [ ] Register Galacticraft mod and reach title screen
- [ ] Register blocks/items/entities
- [ ] Restore networking
- [ ] Restore dimensions/worldgen
- [ ] Restore oxygen system
- [ ] Restore machines/energy
- [ ] Restore rockets and launch flow
- [ ] Restore client rendering and GUIs
- [ ] Pack compatibility testing

## License

Galacticraft 5 source is MIT licensed. Any copied or modified source must retain the required copyright and license notices.
