package de.mrjulsen.dragnsounds.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class CancelUploadSoundPacket extends BaseNetworkPacket<CancelUploadSoundPacket> {

    private long requestId;

    public CancelUploadSoundPacket() {}

    public CancelUploadSoundPacket(long requestId) {
        this.requestId = requestId;
    }

    @Override
    public void encode(CancelUploadSoundPacket packet, RegistryFriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
    }

    @Override
    public CancelUploadSoundPacket decode(RegistryFriendlyByteBuf buf) {
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
