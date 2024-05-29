package de.mrjulsen.dragnsounds.net.cts;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class UpdateMetadataPacket implements IPacketBase<UpdateMetadataPacket> {

    private UUID id;
    private SoundLocation location;
    private Map<String, String> metadata;

    private CompoundTag nbt;

    public UpdateMetadataPacket() {}

    public UpdateMetadataPacket(UUID id, SoundLocation location, Map<String, String> metadata) {
        this.id = id;
        this.location = location;
        this.metadata = metadata;
    }

    public UpdateMetadataPacket(UUID id, CompoundTag nbt, Map<String, String> metadata) {
        this.id = id;
        this.nbt = nbt;
        this.metadata = metadata;
    }

    @Override
    public void encode(UpdateMetadataPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.id);
        buf.writeNbt(packet.location.serializeNbt());
        buf.writeMap(packet.metadata, (b, k) -> b.writeUtf(k), (b, v) -> b.writeUtf(v));
    }

    @Override
    public UpdateMetadataPacket decode(FriendlyByteBuf buf) {
        return new UpdateMetadataPacket(
            buf.readUUID(), 
            buf.readNbt(), 
            buf.readMap(b -> b.readUtf(), b -> b.readUtf())
        );
    }

    @Override
    public void handle(UpdateMetadataPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            SoundLocation location = SoundLocation.fromNbt(packet.nbt, contextSupplier.get().getPlayer().getLevel());
            try {
                SoundFile.updateMetadataInternal(location, packet.id, packet.metadata);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    
}
