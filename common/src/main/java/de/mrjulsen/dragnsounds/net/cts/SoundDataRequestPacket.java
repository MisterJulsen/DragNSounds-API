package de.mrjulsen.dragnsounds.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

public class SoundDataRequestPacket implements IPacketBase<SoundDataRequestPacket> {

    private long soundId;
    private int size;
    private int index;

    public SoundDataRequestPacket() {}

    public SoundDataRequestPacket(long soundId, int size, int index) {
        this.soundId = soundId;
        this.size = size;
        this.index = index;
    }

    @Override
    public void encode(SoundDataRequestPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.soundId);
        buf.writeInt(packet.size);
        buf.writeInt(packet.index);
    }

    @Override
    public SoundDataRequestPacket decode(FriendlyByteBuf buf) {
        return new SoundDataRequestPacket(
            buf.readLong(),
            buf.readInt(),
            buf.readInt()
        );
    }

    @Override
    public void handle(SoundDataRequestPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerSoundManager.sendSoundData(contextSupplier.get().getPlayer(), packet.soundId, packet.size, packet.index);
        });
    }
    
}
