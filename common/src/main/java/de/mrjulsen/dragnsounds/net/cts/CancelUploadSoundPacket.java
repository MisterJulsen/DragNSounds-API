package de.mrjulsen.dragnsounds.net.cts;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;

public class CancelUploadSoundPacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";

    private long requestId;

    public CancelUploadSoundPacket(DLStatus status) {
        super(status);
    }

    public CancelUploadSoundPacket(long requestId) {
        super(DLStatus.OK);
        this.requestId = requestId;
    }
    
    @Override
    protected void write(CompoundTag nbt) {
        nbt.putLong(NBT_REQUEST_ID, requestId);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.requestId = nbt.getLong(NBT_REQUEST_ID);
    }

    
    public static void handle(CancelUploadSoundPacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            DragNSounds.LOGGER.info("Cancel sound file upload...");
            ServerSoundManager.closeUpload(packet.requestId);
        });
    }
    
}
