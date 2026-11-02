/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/** Loader-neutral key definitions; Forge performs registration on its client event. */
public final class GCKeyBinds {
    public static final KeyMapping OPEN_ROCKET_INVENTORY =
            new KeyMapping("key.galacticraft.rocket.inventory", GLFW.GLFW_KEY_F, "key.categories.inventory");

    private GCKeyBinds() {
    }

    /** Compatibility no-op; Forge registration lives in GalacticraftForgeClient. */
    public static void register() {
    }
}
