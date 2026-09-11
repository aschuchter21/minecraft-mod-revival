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
package dev.galacticraft.mod.api.config;

import dev.galacticraft.mod.Galacticraft;
import net.minecraft.client.gui.screens.Screen;

/** Loader-neutral configuration manager API for the Forge runtime. */
public interface ConfigManager {
    static ConfigManager getInstance() {
        return Galacticraft.CONFIG_MANAGER;
    }

    void save();

    void load();

    Config get();

    Screen getScreen(Screen parent);
}
