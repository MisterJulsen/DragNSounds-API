package de.mrjulsen.dragnsounds.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCheckCallback;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

public class SoundPlayingCheckResponsePacket implements IPacketBase<SoundPlayingCheckResponsePacket> {

    private long requestId;
    private boolean value;

    public SoundPlayingCheckResponsePacket() {}

    public SoundPlayingCheckResponsePacket(long requestId, boolean value) {
        this.requestId = requestId;
        this.value = value;
    }

    @Override
    public void encode(SoundPlayingCheckResponsePacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeBoolean(packet.value);
    }

    @Override
    public SoundPlayingCheckResponsePacket decode(FriendlyByteBuf buf) {
        return new SoundPlayingCheckResponsePacket(
            buf.readLong(), 
            buf.readBoolean()
        );
    }

    @Override
    public void handle(SoundPlayingCheckResponsePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            SoundPlayingCheckCallback.run(packet.requestId, packet.value);
        });
    }

}
