package de.mrjulsen.dragnsounds.net.cts;

import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.net.stc.StartUploadResponsePacket;
import de.mrjulsen.dragnsounds.registry.ModNetworkManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class StartUploadSoundPacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_MAX_SIZE = "MaxSize";

    private long requestId;
    private int maxSize;

    public StartUploadSoundPacket(DLStatus status) {
        super(status);
    }

    public StartUploadSoundPacket(long requestId, int maxSize) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.maxSize = maxSize;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putLong(NBT_REQUEST_ID, requestId);
        nbt.putInt(NBT_MAX_SIZE, maxSize);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.requestId = nbt.getLong(NBT_REQUEST_ID);
        this.maxSize = nbt.getInt(NBT_MAX_SIZE);
    }

    public static void handle(StartUploadSoundPacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            DLStatus result = ServerSoundManager.prepareUploadPacket((ServerPlayer) context.getPlayer(), packet);
            ModNetworkManager.START_UPLOAD_RESPONSE.send(NetworkDirection.toPlayer((ServerPlayer)context.getPlayer()), new StartUploadResponsePacket(packet.requestId, result));
        });
    }

    public long getRequestId() {
        return requestId;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
