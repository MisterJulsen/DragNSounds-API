package de.mrjulsen.dragnsounds.core.data;

import de.mrjulsen.dragnsounds.core.ext.CustomSoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

public record PlaybackConfig(ESoundType type, String soundSourceName, float volume, float pitch, Vec3 pos, int attenuationDistance, boolean relative, int offsetTicks, boolean showNowPlayingText) {

    private static final String NBT_TYPE = "Type";
    private static final String NBT_VOLUME = "Volume";
    private static final String NBT_PITCH = "Pitch";
    private static final String NBT_SOURCE_NAME = "Source";
    private static final String NBT_POS = "Pos";
    private static final String NBT_ATTENUATION_DISTANCE = "AttenuationDistance";
    private static final String NBT_RELATIVE = "Relative";
    private static final String NBT_OFFSET = "Offset";
    private static final String NBT_ACTIONBAR = "ActionbarMessage";

    public static PlaybackConfig defaultUI(float volume, float pitch, int offsetTicks) {
        return new PlaybackConfig(ESoundType.UI, CustomSoundSource.CUSTOM.getName(), volume, pitch, Vec3.ZERO, 16, false, offsetTicks, false);
    }

    public static PlaybackConfig defaultWorld(float volume, float pitch, Vec3 pos, int attenuationDistance, boolean relative, int offsetTicks) {
        return new PlaybackConfig(ESoundType.WORLD, CustomSoundSource.CUSTOM.getName(), volume, pitch, pos, attenuationDistance, relative, offsetTicks, false);
    }

    public static PlaybackConfig defaultUIMusic(float volume, float pitch, int offsetTicks) {
        return new PlaybackConfig(ESoundType.UI, CustomSoundSource.CUSTOM.getName(), volume, pitch, Vec3.ZERO, 16, false, offsetTicks, true);
    }

    public static PlaybackConfig defaultWorldMusic(float volume, float pitch, Vec3 pos, int attenuationDistance, boolean relative, int offsetTicks) {
        return new PlaybackConfig(ESoundType.WORLD, CustomSoundSource.CUSTOM.getName(), volume, pitch, pos, attenuationDistance, relative, offsetTicks, true);
    }

    public CompoundTag serializeNbt() {        
        CompoundTag nbt = new CompoundTag();

        ListTag posTag = new ListTag();
        if (type() == ESoundType.WORLD) {
            posTag.add(DoubleTag.valueOf(pos().x));
            posTag.add(DoubleTag.valueOf(pos().y));
            posTag.add(DoubleTag.valueOf(pos().z));
        }

        nbt.putInt(NBT_TYPE, type().getId());
        nbt.putFloat(NBT_VOLUME, volume());
        nbt.putFloat(NBT_PITCH, pitch());
        nbt.put(NBT_POS, posTag);
        nbt.putString(NBT_SOURCE_NAME, soundSourceName());
        nbt.putInt(NBT_ATTENUATION_DISTANCE, attenuationDistance());
        nbt.putBoolean(NBT_RELATIVE, relative());
        nbt.putInt(NBT_OFFSET, offsetTicks());
        nbt.putBoolean(NBT_ACTIONBAR, showNowPlayingText());

        return nbt;
    }

    public static PlaybackConfig deserializeNbt(CompoundTag nbt) {
        ListTag posTag = nbt.getList(NBT_POS, Tag.TAG_DOUBLE);
        return new PlaybackConfig(
            ESoundType.getById(nbt.getInt(NBT_TYPE)),
            nbt.getString(NBT_SOURCE_NAME),
            nbt.getFloat(NBT_VOLUME),
            nbt.getFloat(NBT_PITCH),
            new Vec3(posTag.getDouble(0), posTag.getDouble(1), posTag.getDouble(2)),
            nbt.getInt(NBT_ATTENUATION_DISTANCE),
            nbt.getBoolean(NBT_RELATIVE),
            nbt.getInt(NBT_OFFSET),
            nbt.getBoolean(NBT_ACTIONBAR)
        );
    }
}
