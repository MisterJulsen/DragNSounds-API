package de.mrjulsen.dragnsounds.core.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

public record SoundPlaybackData(float volume, float pitch, int attenuationDistance, Vec3 position, Vec3 direction, Vec3 velocity, float doppler, float coneAngleA, float coneAngleB, float coneOuterGain, boolean paused, int playbackPositionTicks) {
    private static final String NBT_VOLUME = "Volume";
    private static final String NBT_PITCH = "Pitch";
    private static final String NBT_ATTENUATION_DISTANCE = "AttenuationDistance";
    private static final String NBT_POSITION = "Position";
    private static final String NBT_DIRECTION = "Direction";
    private static final String NBT_VELOCITY = "Velocity";
    private static final String NBT_DOPPLER = "Doppler";
    private static final String NBT_CONE_ANGLE_A = "AngleA";
    private static final String NBT_CONE_ANGLE_B = "AngleB";
    private static final String NBT_CONE_OUTER_GAIN = "OuterGain";
    private static final String NBT_PAUSED = "IsPaused";
    private static final String NBT_POSITION_TICKS = "PositionTicks";

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();

        ListTag posTag = new ListTag();
        posTag.add(DoubleTag.valueOf(position().x));
        posTag.add(DoubleTag.valueOf(position().y));
        posTag.add(DoubleTag.valueOf(position().z));

        ListTag dirTag = new ListTag();
        posTag.add(DoubleTag.valueOf(direction().x));
        posTag.add(DoubleTag.valueOf(direction().y));
        posTag.add(DoubleTag.valueOf(direction().z));

        ListTag velTag = new ListTag();
        posTag.add(DoubleTag.valueOf(velocity().x));
        posTag.add(DoubleTag.valueOf(velocity().y));
        posTag.add(DoubleTag.valueOf(velocity().z));

        nbt.putFloat(NBT_VOLUME, volume());
        nbt.putFloat(NBT_PITCH, pitch());
        nbt.putInt(NBT_ATTENUATION_DISTANCE, attenuationDistance());
        nbt.put(NBT_POSITION, posTag);
        nbt.put(NBT_DIRECTION, dirTag);
        nbt.put(NBT_VELOCITY, velTag);
        nbt.putFloat(NBT_DOPPLER, doppler());
        nbt.putFloat(NBT_CONE_ANGLE_A, coneAngleA());
        nbt.putFloat(NBT_CONE_ANGLE_B, coneAngleB());
        nbt.putFloat(NBT_CONE_OUTER_GAIN, coneOuterGain());
        nbt.putBoolean(NBT_PAUSED, paused());
        nbt.putInt(NBT_POSITION_TICKS, playbackPositionTicks());
        return nbt;
    }

    public static SoundPlaybackData fromNbt(CompoundTag nbt) {
        return new SoundPlaybackData(
            nbt.getFloat(NBT_VOLUME),
            nbt.getFloat(NBT_PITCH),
            nbt.getInt(NBT_ATTENUATION_DISTANCE),
            new Vec3(nbt.getList(NBT_POSITION, Tag.TAG_DOUBLE).getDouble(0), nbt.getList(NBT_POSITION, Tag.TAG_DOUBLE).getDouble(1), nbt.getList(NBT_POSITION, Tag.TAG_DOUBLE).getDouble(2)),
            new Vec3(nbt.getList(NBT_DIRECTION, Tag.TAG_DOUBLE).getDouble(0), nbt.getList(NBT_DIRECTION, Tag.TAG_DOUBLE).getDouble(1), nbt.getList(NBT_DIRECTION, Tag.TAG_DOUBLE).getDouble(2)),
            new Vec3(nbt.getList(NBT_VELOCITY, Tag.TAG_DOUBLE).getDouble(0), nbt.getList(NBT_VELOCITY, Tag.TAG_DOUBLE).getDouble(1), nbt.getList(NBT_VELOCITY, Tag.TAG_DOUBLE).getDouble(2)),
            nbt.getFloat(NBT_DOPPLER),
            nbt.getFloat(NBT_CONE_ANGLE_A),
            nbt.getFloat(NBT_CONE_ANGLE_B),
            nbt.getFloat(NBT_CONE_OUTER_GAIN),
            nbt.getBoolean(NBT_PAUSED),
            nbt.getInt(NBT_POSITION_TICKS)
        );
    }
}
