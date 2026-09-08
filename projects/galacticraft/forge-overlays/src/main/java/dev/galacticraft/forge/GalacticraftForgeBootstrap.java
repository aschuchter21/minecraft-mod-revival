/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.forge;

import com.mojang.logging.LogUtils;
import dev.galacticraft.mod.forge.network.ForgeRocketNetworking;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Forge 47.4.10 bootstrap for the recovered Galacticraft 1.20.1 runtime.
 *
 * <p>This overlay intentionally starts small: it establishes loader-native
 * networking before any player can connect. Registry/event bootstrap is being
 * transplanted subsystem-by-subsystem as the official content classes become
 * Forge-clean.</p>
 */
@Mod(GalacticraftForgeBootstrap.MOD_ID)
public final class GalacticraftForgeBootstrap {
    public static final String MOD_ID = "galacticraft";
    private static final Logger LOGGER = LogUtils.getLogger();

    public GalacticraftForgeBootstrap() {
        ForgeRocketNetworking.register();
        LOGGER.info("Galacticraft Forge 1.20.1 runtime bootstrap loaded");
    }
}
