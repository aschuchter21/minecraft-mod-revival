package dev.galacticraft.forge;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(GalacticraftForgeBootstrap.MOD_ID)
public final class GalacticraftForgeBootstrap {
    public static final String MOD_ID = "galacticraft";
    private static final Logger LOGGER = LogUtils.getLogger();

    public GalacticraftForgeBootstrap() {
        LOGGER.info("Galacticraft Forge 1.20.1 port bootstrap loaded");
    }
}
