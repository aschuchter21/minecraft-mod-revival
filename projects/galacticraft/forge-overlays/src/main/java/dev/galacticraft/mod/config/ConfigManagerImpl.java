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
package dev.galacticraft.mod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.api.config.Config;
import dev.galacticraft.mod.api.config.ConfigManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Forge-native config file location for the recovered Galacticraft config. */
public class ConfigManagerImpl implements ConfigManager {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final File file = new File(FMLPaths.CONFIGDIR.get().toFile(), "galacticraft/config.json");
    private Config config = new ConfigImpl();

    public ConfigManagerImpl() {
        this.load();
    }

    @Override
    public void save() {
        try {
            org.apache.commons.io.FileUtils.writeStringToFile(
                    this.file, this.gson.toJson(this.config), Charset.defaultCharset());
        } catch (IOException e) {
            Constant.LOGGER.error("Failed to save config.", e);
        }
    }

    @Override
    public void load() {
        try {
            this.file.getParentFile().mkdirs();
            if (!this.file.exists()) {
                Constant.LOGGER.info("Failed to find config file, creating one.");
                this.save();
            } else {
                byte[] bytes = Files.readAllBytes(Paths.get(this.file.getPath()));
                ConfigImpl loaded = this.gson.fromJson(new String(bytes, Charset.defaultCharset()), ConfigImpl.class);
                if (loaded != null) this.config = loaded;
            }
        } catch (IOException e) {
            Constant.LOGGER.error("Failed to load config.", e);
        }
    }

    @Override
    public Config get() {
        return this.config;
    }

    @Override
    public Screen getScreen(Screen parent) {
        // Cloth Config is a Fabric-side integration in this recovered baseline.
        // Preserve the API without pulling that client-only dependency into the
        // Forge server; a native Forge config screen can be restored separately.
        return parent;
    }
}
