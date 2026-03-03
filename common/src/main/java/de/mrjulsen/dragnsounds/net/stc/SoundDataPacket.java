package de.mrjulsen.dragnsounds.net.stc;

import de.mrjulsen.dragnsounds.core.ClientInstanceManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class SoundDataPacket extends NetworkPacketData implements Comparable<SoundDataPacket> {

    private static final String NBT_SOUND_ID = "SoundId";
    private static final String NBT_INDEX = "Index";
    private static final String NBT_BUFFER_SIZE = "BufferSize";
    private static final String NBT_HAS_NEXT = "HasNext";
    private static final String NBT_DATA = "Data";

    private long soundId;
    private int index;
    private int bufferSize;
    private boolean hasNext;
    private byte[] data;

    public SoundDataPacket(DLStatus status) { super(status); }

    public SoundDataPacket(long soundId, int index, int bufferSize, boolean hasNext, byte[] data) {
        super(DLStatus.OK);
        this.soundId = soundId;
        this.index = index;
        this.bufferSize = bufferSize;
        this.hasNext = hasNext;
        this.data = data;
    }

    @Override protected void write(CompoundTag tag) {
        tag.putLong(NBT_SOUND_ID, soundId);
        tag.putInt(NBT_INDEX, index);
        tag.putInt(NBT_BUFFER_SIZE, bufferSize);
        tag.putBoolean(NBT_HAS_NEXT, hasNext);
        tag.putByteArray(NBT_DATA, data != null ? data : new byte[0]);
    }

    @Override protected void read(CompoundTag tag) {
        this.soundId = tag.getLong(NBT_SOUND_ID);
        this.index = tag.getInt(NBT_INDEX);
        this.bufferSize = tag.getInt(NBT_BUFFER_SIZE);
        this.hasNext = tag.getBoolean(NBT_HAS_NEXT);
        this.data = tag.getByteArray(NBT_DATA);
    }

    public static void handle(SoundDataPacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () -> {
                ClientInstanceManager.receiveSoundData(packet);
            });
        });
    }

    public long getSoundId() { return soundId; }
    public int getIndex() { return index; }
    public int getBufferSize() { return bufferSize; }
    public boolean isHasNext() { return hasNext; }
    public byte[] getData() { return data; }

    @Override
    public int compareTo(SoundDataPacket o) {
        return o == null ? 0 : this.index - o.index;
    }
}
