package de.mrjulsen.dragnsounds.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.net.stc.StartUploadResponsePacket;
import de.mrjulsen.mcdragonlib.data.StatusResult;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.mcdragonlib.net.DLNetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class StartUploadSoundPacket extends BaseNetworkPacket<StartUploadSoundPacket> {

    private long requestId;
    private int maxSize;    

    public StartUploadSoundPacket() {}

    public StartUploadSoundPacket(long requestId, int maxSize) {
        this.requestId = requestId;
        this.maxSize = maxSize;
    }

    @Override
    public void encode(StartUploadSoundPacket packet, RegistryFriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeInt(packet.maxSize);
    }

    @Override
    public StartUploadSoundPacket decode(RegistryFriendlyByteBuf buf) {
        return new StartUploadSoundPacket(
            buf.readLong(),
            buf.readInt()
        );
    }

    @Override
    public void handle(StartUploadSoundPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            StatusResult result = ServerSoundManager.prepareUploadPacket((ServerPlayer)contextSupplier.get().getPlayer(), packet);
            DLNetworkManager.sendToPlayer((ServerPlayer)contextSupplier.get().getPlayer(), new StartUploadResponsePacket(packet.requestId, result));
        });
    }

    public long getRequestId() {
        return requestId;
    }
      

    public int getMaxSize() {
        return maxSize;
    }  
}
