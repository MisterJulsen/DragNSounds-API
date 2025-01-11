package de.mrjulsen.dragnsounds.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback.ESoundPlaybackStatus;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class SoundCreatedResponsePacket extends BaseNetworkPacket<SoundCreatedResponsePacket> {

    private long soundId;
    private ESoundPlaybackStatus status;


    public SoundCreatedResponsePacket() {}

    public SoundCreatedResponsePacket(long soundId, ESoundPlaybackStatus status) {
        this.soundId = soundId;
        this.status = status;
    }

    @Override
    public void encode(SoundCreatedResponsePacket packet, RegistryFriendlyByteBuf buf) {
        buf.writeLong(packet.soundId);
        buf.writeInt(packet.status.getId());
    }

    @Override
    public SoundCreatedResponsePacket decode(RegistryFriendlyByteBuf buf) {
        return new SoundCreatedResponsePacket(buf.readLong(), ESoundPlaybackStatus.getById(buf.readInt()));
    }

    @Override
    public void handle(SoundCreatedResponsePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            SoundPlayingCallback.run(packet.soundId, contextSupplier.get().getPlayer(), packet.status);
        });
    }
    
}
