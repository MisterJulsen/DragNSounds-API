package de.mrjulsen.dragnsounds.net.cts;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.net.stc.SoundDeleteResponsePacket;
import de.mrjulsen.mcdragonlib.data.StatusResult;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class SoundDeleteRequestPacket implements IPacketBase<SoundDeleteRequestPacket> {

    private long requestId;
    private SoundLocation location;
    private UUID id;

    private CompoundTag nbt;

    public SoundDeleteRequestPacket() {}

    public SoundDeleteRequestPacket(long requestId, SoundLocation location, UUID id) {
        this.requestId = requestId;
        this.location = location;
        this.id = id;
    }

    public SoundDeleteRequestPacket(long requestId, CompoundTag nbt, UUID id) {
        this.requestId = requestId;
        this.nbt = nbt;
        this.id = id;
    }

    @Override
    public void encode(SoundDeleteRequestPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeNbt(packet.location.serializeNbt());
        buf.writeUUID(packet.id);
    }

    @Override
    public SoundDeleteRequestPacket decode(FriendlyByteBuf buf) {
        return new SoundDeleteRequestPacket(
            buf.readLong(), 
            buf.readNbt(), 
            buf.readUUID()
        );        
    }

    @Override
    public void handle(SoundDeleteRequestPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            SoundLocation loc = SoundLocation.fromNbt(packet.nbt, contextSupplier.get().getPlayer().getLevel());
            StatusResult result;
            try {
                result = ServerSoundManager.deleteSound(loc, packet.id);
            } catch (IOException e) {
                DragNSounds.LOGGER.error("Unable to delete sound file: " + packet.id, e);
                result = new StatusResult(false, Integer.MIN_VALUE, e.getLocalizedMessage());
            }
            DragNSounds.net().sendToPlayer((ServerPlayer)contextSupplier.get().getPlayer(), new SoundDeleteResponsePacket(packet.requestId, result));
        });
    }
    
}
