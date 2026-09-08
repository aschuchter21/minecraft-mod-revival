/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.events;

import dev.galacticraft.api.rocket.LaunchStage;
import dev.galacticraft.api.rocket.entity.Rocket;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Loader-neutral replacement for the Fabric array-backed rocket event.
 *
 * <p>The tiny register/invoker surface intentionally mirrors the part of the
 * Fabric Event API Galacticraft used, so existing Galacticraft callers can be
 * ported without pulling Fabric's event runtime into Forge.</p>
 */
public interface RocketEvents {
    StageChangedEvent STAGE_CHANGED = new StageChangedEvent();

    final class StageChangedEvent {
        private final List<StageChanged> listeners = new CopyOnWriteArrayList<>();
        private final StageChanged invoker = (rocket, oldStage) -> {
            for (StageChanged listener : this.listeners) {
                listener.onStageChanged(rocket, oldStage);
            }
        };

        public void register(@NotNull StageChanged listener) {
            this.listeners.add(listener);
        }

        public void unregister(@NotNull StageChanged listener) {
            this.listeners.remove(listener);
        }

        public @NotNull StageChanged invoker() {
            return this.invoker;
        }
    }

    @FunctionalInterface
    interface StageChanged {
        void onStageChanged(Rocket rocket, LaunchStage oldStage);
    }
}
