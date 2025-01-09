package de.mrjulsen.dragnsounds.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.net.stc.StartUploadResponsePacket;
import de.mrjulsen.mcdragonlib.data.StatusResult;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class StartUploadSoundPacket implements IPacketBase<StartUploadSoundPacket> {

    private long requestId;
    private int maxSize;    

    public StartUploadSoundPacket() {}

    public StartUploadSoundPacket(long requestId, int maxSize) {
        this.requestId = requestId;
        this.maxSize = maxSize;
    }

    @Override
    public void encode(StartUploadSoundPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeInt(packet.maxSize);
    }

    @Override
    public StartUploadSoundPacket decode(FriendlyByteBuf buf) {
        return new StartUploadSoundPacket(
            buf.readLong(),
            buf.readInt()
        );
    }

    @Override
    public void handle(StartUploadSoundPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            StatusResult result = ServerSoundManager.prepareUploadPacket((ServerPlayer)contextSupplier.get().getPlayer(), packet);
            DragNSounds.net().sendToPlayer((ServerPlayer)contextSupplier.get().getPlayer(), new StartUploadResponsePacket(packet.requestId, result));
        });
    }

    public long getRequestId() {
        return requestId;
    }
      

    public int getMaxSize() {
        return maxSize;
    }  
}
