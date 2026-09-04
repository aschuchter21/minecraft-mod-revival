# Control Engineering — 1.20.1 Revival

## Upstream

- Repository: `malte0811/ControlEngineering`
- Upstream branch: `1.20.1`
- License: MIT

## Target environment

- Minecraft: `1.20.1`
- Forge: `47.4.10`
- Java: `17`
- Immersive Engineering runtime target: `1.20.1-10.2.0-183`

## Build details

The upstream 1.20.1 branch already contains the major port from Minecraft 1.19.2.

The project compiles successfully against:

- Forge `47.4.10`
- Immersive Engineering API `1.20.1-10.2.0-182`

IE build 183 was not available through the Maven repositories used by the project during testing, so the mod is compiled against 182. Its declared runtime dependency is 182 or newer, making IE 183 a valid runtime test target.

## Current status

**Build successful. Runtime testing in HBM MODERNIZED is pending/in progress.**

If startup fails, preserve `debug.log` and identify the first true Control Engineering/Immersive Engineering runtime exception before making source changes.
