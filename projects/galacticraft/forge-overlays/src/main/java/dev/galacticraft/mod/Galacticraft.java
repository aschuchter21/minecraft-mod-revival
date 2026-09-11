/*
 * Copyright (c) 2019-2023 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */
package dev.galacticraft.mod;

import dev.galacticraft.mod.api.config.ConfigManager;
import dev.galacticraft.mod.config.ConfigManagerImpl;

/**
 * Forge runtime holder for Galacticraft's shared configuration state.
 *
 * <p>Forge initialization lives in {@code GalacticraftForgeBootstrap}; this
 * class deliberately does not implement Fabric's ModInitializer. Keeping the
 * historical CONFIG_MANAGER field preserves the ABI used throughout the
 * recovered 1.20.1 gameplay code.</p>
 */
public final class Galacticraft {
    public static final ConfigManager CONFIG_MANAGER = new ConfigManagerImpl();

    private Galacticraft() {
    }
}
