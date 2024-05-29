package de.mrjulsen.dragnsounds.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.callbacks.server.SoundGetDataCallback;
import de.mrjulsen.dragnsounds.core.data.SoundPlaybackData;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

public class SoundGetDataResponsePacket implements IPacketBase<SoundGetDataResponsePacket> {
    
    private long soundId;
    private SoundPlaybackData data;

    public SoundGetDataResponsePacket() {}

    public SoundGetDataResponsePacket(long soundId, SoundPlaybackData data) {
        this.soundId = soundId;
        this.data = data;
    }

    @Override
    public void encode(SoundGetDataResponsePacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.soundId);
        buf.writeBoolean(packet.data != null);
        if (packet.data != null) {
            buf.writeNbt(packet.data.toNbt());
        }
    }

    @Override
    public SoundGetDataResponsePacket decode(FriendlyByteBuf buf) {
        return new SoundGetDataResponsePacket(buf.readLong(), buf.readBoolean() ? SoundPlaybackData.fromNbt(buf.readNbt()) : null);
    }

    @Override
    public void handle(SoundGetDataResponsePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            SoundGetDataCallback.run(packet.soundId, contextSupplier.get().getPlayer(), packet.data);
        });
    }

    
}
