package de.mrjulsen.dragnsounds.net.cts;

import de.mrjulsen.dragnsounds.core.callbacks.server.SoundGetDataCallback;
import de.mrjulsen.dragnsounds.core.data.SoundPlaybackData;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;

public class SoundGetDataResponsePacket extends NetworkPacketData {

    private static final String NBT_SOUND_ID = "SoundId";
    private static final String NBT_HAS_DATA = "HasData";
    private static final String NBT_DATA = "Data";

    private long soundId;
    private SoundPlaybackData data;

    public SoundGetDataResponsePacket(DLStatus status) {
        super(status);
    }

    public SoundGetDataResponsePacket(long soundId, SoundPlaybackData data) {
        super(DLStatus.OK);
        this.soundId = soundId;
        this.data = data;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putLong(NBT_SOUND_ID, soundId);
        nbt.putBoolean(NBT_HAS_DATA, data != null);
        if (data != null) {
            nbt.put(NBT_DATA, data.toNbt());
        }
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.soundId = nbt.getLong(NBT_SOUND_ID);
        this.data = nbt.getBoolean(NBT_HAS_DATA) ? SoundPlaybackData.fromNbt(nbt.getCompound(NBT_DATA)) : null;
    }

    public static void handle(SoundGetDataResponsePacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            SoundGetDataCallback.run(packet.soundId, context.getPlayer(), packet.data);
        });
    }
}
