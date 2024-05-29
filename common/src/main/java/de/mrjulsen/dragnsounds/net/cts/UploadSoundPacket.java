package de.mrjulsen.dragnsounds.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class UploadSoundPacket implements IPacketBase<UploadSoundPacket>, Comparable<UploadSoundPacket> {

    private long requestId;
    private int index;
    private boolean hasMore;
    private byte[] data;
    private int maxSize;    

    public UploadSoundPacket() {}

    public UploadSoundPacket(long requestId, int index, boolean hasMore, int maxSize, byte[] data) {
        this.requestId = requestId;
        this.index = index;
        this.hasMore = hasMore;
        this.data = data;
        this.maxSize = maxSize;
    }

    @Override
    public void encode(UploadSoundPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeInt(packet.index);
        buf.writeBoolean(packet.hasMore);
        buf.writeInt(packet.maxSize);
        buf.writeByteArray(packet.data);
    }

    @Override
    public UploadSoundPacket decode(FriendlyByteBuf buf) {
        return new UploadSoundPacket(
            buf.readLong(),
            buf.readInt(),
            buf.readBoolean(),
            buf.readInt(),
            buf.readByteArray()
        );
    }

    @Override
    public void handle(UploadSoundPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerSoundManager.receiveUploadPacket((ServerPlayer)contextSupplier.get().getPlayer(), packet);
        });
    }

    public byte[] getData() {
        return data;
    }

    public long getRequestId() {
        return requestId;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public int getIndex() {
        return index;
    }

    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public int compareTo(UploadSoundPacket o) {
        return o == null ? 0 : index - o.index;
    }    
}
