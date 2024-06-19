package de.mrjulsen.dragnsounds.net.cts;

import java.io.IOException;
import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.net.stc.SoundFileResponsePacket;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class SoundFileRequestPacket implements IPacketBase<SoundFileRequestPacket> {

    private long requestId;
    private String id;
    private SoundLocation location;

    private CompoundTag nbt;

    public SoundFileRequestPacket() {}

    public SoundFileRequestPacket(long requestId, String id, SoundLocation location) {
        this.requestId = requestId;
        this.id = id;
        this.location = location;
    }

    public SoundFileRequestPacket(long requestId, String id, CompoundTag nbt) {
        this.requestId = requestId;
        this.id = id;
        this.nbt = nbt;
    }

    @Override
    public void encode(SoundFileRequestPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeUtf(packet.id);
        buf.writeNbt(packet.location.serializeNbt());
    }

    @Override
    public SoundFileRequestPacket decode(FriendlyByteBuf buf) {
        return new SoundFileRequestPacket(
            buf.readLong(), 
            buf.readUtf(), 
            buf.readNbt()
        );
    }

    @Override
    public void handle(SoundFileRequestPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            try {
                SoundFile file = ServerSoundManager.getSoundFile(SoundLocation.fromNbt(packet.nbt, contextSupplier.get().getPlayer().getLevel()), packet.id);                
                DragNSounds.net().sendToPlayer((ServerPlayer)contextSupplier.get().getPlayer(), new SoundFileResponsePacket(packet.requestId, file));
            } catch (IOException e) {
                DragNSounds.LOGGER.warn("Could not find sound file.", e);
                DragNSounds.net().sendToPlayer((ServerPlayer)contextSupplier.get().getPlayer(), new SoundFileResponsePacket(packet.requestId, (SoundFile)null));
            }
        });
    }

}
