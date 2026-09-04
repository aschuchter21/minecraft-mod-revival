# MachineLib 0.3 Forge 1.20.1 backport

This project is the native Forge foundation needed by the Galacticraft 5 1.20.1 revival.

Upstream baseline:
- Repository: `TeamGalacticraft/MachineLib`
- Commit: `031a62640ce753e0c6a5eb4e8ee6d495fecb2ecf`
- Upstream version: `0.3.0`
- Minecraft: `1.20.1`
- Original loader: Fabric
- License: MIT

Target:
- Minecraft `1.20.1`
- Forge `47.4.10`
- Java `17`

The backport keeps Galacticraft's machine-facing API and internal long-valued resource units wherever practical, while replacing Fabric Transfer API and Reborn Energy exposure with native Forge capabilities. The newer `ColinVaughn/MachineLib` 1.21 multiloader work is used as a reference for platform separation; 1.21 binaries are not used as 1.20.1 dependencies.
