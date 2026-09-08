/*
 * Copyright (c) 2021-2023 Team Galacticraft
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
package dev.galacticraft.machinelib.api.menu.sync;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Loader-neutral menu synchronization contract used by Galacticraft's machine
 * menus. Forge packet transport is wired separately; the handlers preserve the
 * MachineLib 0.3 serialization and change-detection behavior.
 */
public interface MenuSyncHandler {
    static @NotNull MenuSyncHandler simple(LongSupplier supplier, LongConsumer consumer) {
        return new LongHandler(supplier, consumer);
    }

    static @NotNull MenuSyncHandler simple(IntSupplier supplier, IntConsumer consumer) {
        return new IntHandler(supplier, consumer);
    }

    static <E extends Enum<E>> @NotNull MenuSyncHandler simple(
            Supplier<E> supplier, Consumer<E> consumer, E[] allowedValues) {
        return new EnumHandler<>(supplier, consumer, allowedValues);
    }

    static @NotNull MenuSyncHandler booleans(boolean[] input, boolean[] output) {
        return new BooleanArrayHandler(input, output);
    }

    boolean needsSyncing();
    void sync(@NotNull FriendlyByteBuf buf);
    void read(@NotNull FriendlyByteBuf buf);

    final class IntHandler implements MenuSyncHandler {
        private final IntSupplier supplier;
        private final IntConsumer consumer;
        private int value;

        IntHandler(IntSupplier supplier, IntConsumer consumer) {
            this.supplier = supplier;
            this.consumer = consumer;
        }

        @Override public boolean needsSyncing() { return this.value != this.supplier.getAsInt(); }
        @Override public void sync(@NotNull FriendlyByteBuf buf) {
            this.value = this.supplier.getAsInt();
            buf.writeInt(this.value);
        }
        @Override public void read(@NotNull FriendlyByteBuf buf) {
            this.value = buf.readInt();
            this.consumer.accept(this.value);
        }
    }

    final class LongHandler implements MenuSyncHandler {
        private final LongSupplier supplier;
        private final LongConsumer consumer;
        private long value;

        LongHandler(LongSupplier supplier, LongConsumer consumer) {
            this.supplier = supplier;
            this.consumer = consumer;
        }

        @Override public boolean needsSyncing() { return this.value != this.supplier.getAsLong(); }
        @Override public void sync(@NotNull FriendlyByteBuf buf) {
            this.value = this.supplier.getAsLong();
            buf.writeLong(this.value);
        }
        @Override public void read(@NotNull FriendlyByteBuf buf) {
            this.value = buf.readLong();
            this.consumer.accept(this.value);
        }
    }

    final class EnumHandler<E extends Enum<E>> implements MenuSyncHandler {
        private final Supplier<E> supplier;
        private final Consumer<E> consumer;
        private final E[] allowedValues;
        private E value;

        EnumHandler(Supplier<E> supplier, Consumer<E> consumer, E[] allowedValues) {
            this.supplier = supplier;
            this.consumer = consumer;
            this.allowedValues = allowedValues;
        }

        @Override public boolean needsSyncing() { return this.value != this.supplier.get(); }
        @Override public void sync(@NotNull FriendlyByteBuf buf) {
            this.value = this.supplier.get();
            buf.writeVarInt(this.value.ordinal());
        }
        @Override public void read(@NotNull FriendlyByteBuf buf) {
            this.value = this.allowedValues[buf.readVarInt()];
            this.consumer.accept(this.value);
        }
    }

    final class BooleanArrayHandler implements MenuSyncHandler {
        private final boolean[] input;
        private final boolean[] output;

        BooleanArrayHandler(boolean[] input, boolean[] output) {
            if (input.length != output.length) {
                throw new IllegalArgumentException("input and output boolean arrays must have equal length");
            }
            this.input = input;
            this.output = output;
        }

        @Override public boolean needsSyncing() {
            return !Arrays.equals(this.input, this.output);
        }

        @Override public void sync(@NotNull FriendlyByteBuf buf) {
            int len = this.input.length;
            int byteLength = (len - len % 8) / 8 + 1;
            byte[] bytes = new byte[byteLength];
            int byteIndex = 0;
            int bitIndex = 0;
            for (int i = 0; i < len; i++) {
                bytes[byteIndex] |= (byte) ((this.input[byteIndex * 8 + bitIndex] ? 1 : 0) << bitIndex++);
                if (bitIndex == 8) {
                    bitIndex = 0;
                    byteIndex++;
                }
            }
            for (byte value : bytes) buf.writeByte(value);
        }

        @Override public void read(@NotNull FriendlyByteBuf buf) {
            int len = this.output.length;
            int byteLength = (len - len % 8) / 8 + 1;
            byte[] bytes = new byte[byteLength];
            for (int i = 0; i < bytes.length; i++) bytes[i] = buf.readByte();

            int byteIndex = 0;
            int bitIndex = 0;
            for (int i = 0; i < len; i++) {
                this.output[i] = ((bytes[byteIndex] >> bitIndex++) & 1) != 0;
                if (bitIndex == 8) {
                    bitIndex = 0;
                    byteIndex++;
                }
            }
        }
    }
}
