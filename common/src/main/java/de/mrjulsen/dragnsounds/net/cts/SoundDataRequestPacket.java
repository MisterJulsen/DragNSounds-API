package de.mrjulsen.dragnsounds.net.cts;

import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;

public class SoundDataRequestPacket extends NetworkPacketData {

    private static final String NBT_SOUND_ID = "SoundId";
    private static final String NBT_SIZE = "Size";
    private static final String NBT_INDEX = "Index";

    private long soundId;
    private int size;
    private int index;

    public SoundDataRequestPacket(DLStatus status) {
        super(status);
    }

    public SoundDataRequestPacket(long soundId, int size, int index) {
        super(DLStatus.OK);
        this.soundId = soundId;
        this.size = size;
        this.index = index;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putLong(NBT_SOUND_ID, soundId);
        nbt.putInt(NBT_SIZE, size);
        nbt.putInt(NBT_INDEX, index);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.soundId = nbt.getLong(NBT_SOUND_ID);
        this.size = nbt.getInt(NBT_SIZE);
        this.index = nbt.getInt(NBT_INDEX);
    }

    
    public static void handle(SoundDataRequestPacket packet, NetworkPacketContext context) {
        ServerSoundManager.sendSoundData(context.getPlayer(), packet.soundId, packet.size, packet.index);
    }
    
}
