package de.mrjulsen.dragnsounds.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback.ESoundPlaybackStatus;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

public class SoundCreatedResponsePacket implements IPacketBase<SoundCreatedResponsePacket> {

    private long soundId;
    private ESoundPlaybackStatus status;


    public SoundCreatedResponsePacket() {}

    public SoundCreatedResponsePacket(long soundId, ESoundPlaybackStatus status) {
        this.soundId = soundId;
        this.status = status;
    }

    @Override
    public void encode(SoundCreatedResponsePacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.soundId);
        buf.writeInt(packet.status.getId());
    }

    @Override
    public SoundCreatedResponsePacket decode(FriendlyByteBuf buf) {
        return new SoundCreatedResponsePacket(buf.readLong(), ESoundPlaybackStatus.getById(buf.readInt()));
    }

    @Override
    public void handle(SoundCreatedResponsePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            SoundPlayingCallback.run(packet.soundId, contextSupplier.get().getPlayer(), packet.status);
        });
    }
    
}
