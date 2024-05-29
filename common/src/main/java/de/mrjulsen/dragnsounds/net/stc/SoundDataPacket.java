package de.mrjulsen.dragnsounds.net.stc;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ClientInstanceManager;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.FriendlyByteBuf;

public class SoundDataPacket implements IPacketBase<SoundDataPacket>, Comparable<SoundDataPacket> {

    private long soundId;
    private int index;
    private int bufferSize;
    private boolean hasNext;
    private byte[] data;

    public SoundDataPacket() {}

    public SoundDataPacket(long soundId, int index, int bufferSize, boolean hasNext, byte[] data) {
        this.soundId = soundId;
        this.index = index;
        this.bufferSize = bufferSize;
        this.hasNext = hasNext;
        this.data = data;
    }

    @Override
    public void encode(SoundDataPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.soundId);
        buf.writeInt(packet.index);
        buf.writeInt(packet.bufferSize);
        buf.writeBoolean(packet.hasNext);
        buf.writeByteArray(packet.data);
    }

    @Override
    public SoundDataPacket decode(FriendlyByteBuf buf) {
        return new SoundDataPacket(
            buf.readLong(),
            buf.readInt(),
            buf.readInt(),
            buf.readBoolean(),
            buf.readByteArray()
        );
    }

    @Override
    public void handle(SoundDataPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                ClientInstanceManager.receiveSoundData(packet);
            });
        });
    }

    public long getSoundId() {
        return soundId;
    }

    public int getIndex() {
        return index;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public int compareTo(SoundDataPacket o) {
        return o == null ? 0 : this.index - o.index;
    }
    
}
