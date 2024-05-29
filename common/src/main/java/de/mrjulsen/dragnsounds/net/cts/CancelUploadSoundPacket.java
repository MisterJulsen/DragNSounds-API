package de.mrjulsen.dragnsounds.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

public class CancelUploadSoundPacket implements IPacketBase<CancelUploadSoundPacket> {

    private long requestId;

    public CancelUploadSoundPacket() {}

    public CancelUploadSoundPacket(long requestId) {
        this.requestId = requestId;
    }

    @Override
    public void encode(CancelUploadSoundPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
    }

    @Override
    public CancelUploadSoundPacket decode(FriendlyByteBuf buf) {
        return new CancelUploadSoundPacket(
            buf.readLong()
        );
    }

    @Override
    public void handle(CancelUploadSoundPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            DragNSounds.LOGGER.info("Cancel sound file upload...");
            ServerSoundManager.closeUpload(packet.requestId);
        });
    }
    
}
