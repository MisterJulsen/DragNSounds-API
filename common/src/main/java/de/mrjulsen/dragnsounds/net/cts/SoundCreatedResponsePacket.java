package de.mrjulsen.dragnsounds.net.cts;

import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback.ESoundPlaybackStatus;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;

public class SoundCreatedResponsePacket extends NetworkPacketData {

    private static final String NBT_SOUND_ID = "SoundId";
    private static final String NBT_STATUS = "Status";

    private long soundId;
    private ESoundPlaybackStatus status;


    public SoundCreatedResponsePacket(DLStatus status) {
        super(status);
    }

    public SoundCreatedResponsePacket(long soundId, ESoundPlaybackStatus status) {
        super(DLStatus.OK);
        this.soundId = soundId;
        this.status = status;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putLong(NBT_SOUND_ID, soundId);
        nbt.putInt(NBT_STATUS, status.getId());
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.soundId = nbt.getLong(NBT_SOUND_ID);
        this.status = ESoundPlaybackStatus.getById(nbt.getInt(NBT_STATUS));
    }

    
    public static void handle(SoundCreatedResponsePacket packet, NetworkPacketContext context) {
        SoundPlayingCallback.run(packet.soundId, context.getPlayer(), packet.status);
    }
    
}
