package de.mrjulsen.dragnsounds.net.cts;

import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class UploadSoundPacket extends NetworkPacketData implements Comparable<UploadSoundPacket> {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_INDEX = "Index";
    private static final String NBT_HAS_MORE = "HasMore";
    private static final String NBT_DATA = "Data";
    private static final String NBT_MAX_SIZE = "MaxSize";

    private long requestId;
    private int index;
    private boolean hasMore;
    private byte[] data;
    private int maxSize;

    public UploadSoundPacket(DLStatus status) {
        super(status);
    }

    public UploadSoundPacket(long requestId, int index, boolean hasMore, int maxSize, byte[] data) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.index = index;
        this.hasMore = hasMore;
        this.data = data;
        this.maxSize = maxSize;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putLong(NBT_REQUEST_ID, requestId);
        nbt.putInt(NBT_INDEX, index);
        nbt.putBoolean(NBT_HAS_MORE, hasMore);
        nbt.putInt(NBT_MAX_SIZE, maxSize);
        nbt.putByteArray(NBT_DATA, data != null ? data : new byte[0]);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.requestId = nbt.getLong(NBT_REQUEST_ID);
        this.index = nbt.getInt(NBT_INDEX);
        this.hasMore = nbt.getBoolean(NBT_HAS_MORE);
        this.maxSize = nbt.getInt(NBT_MAX_SIZE);
        this.data = nbt.getByteArray(NBT_DATA);
    }

    public static void handle(UploadSoundPacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            ServerSoundManager.receiveUploadPacket((ServerPlayer) context.getPlayer(), packet);
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
