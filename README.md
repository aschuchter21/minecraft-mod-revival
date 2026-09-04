# Minecraft Mod Revival

A workspace for updating, rebuilding, and testing older Minecraft mods on newer Minecraft/Forge versions.

## Current target

- Minecraft 1.20.1
- Forge 47.4.10
- Java 17

## Projects

### Control Engineering

Status: **Build successful — runtime testing in progress**

Upstream: `malte0811/ControlEngineering`

- Upstream branch: `1.20.1`
- Control Engineering: `0.4.0-dev-1`
- Compile dependency: Immersive Engineering `1.20.1-10.2.0-182`
- Intended runtime test: Immersive Engineering `1.20.1-10.2.0-183`
- Forge target: `47.4.10`

The build workflow checks out the upstream source directly and does not modify the original repository.

## Workflow for future revivals

1. Find the original source repository and license.
2. Identify the newest usable branch/version.
3. Check Minecraft, loader, Java, and dependency versions.
4. Build the source unchanged first.
5. Patch compile errors caused by newer APIs/dependencies.
6. Produce a test jar.
7. Test startup in the target modpack.
8. Fix runtime incompatibilities.
9. Document the final working dependency/version combination.

## Notes

This repository is intentionally separate from Printaverse and other production/business repositories.
