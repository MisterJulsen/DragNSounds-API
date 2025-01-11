package de.mrjulsen.dragnsounds.net.cts;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class RemoveMetadataPacket extends BaseNetworkPacket<RemoveMetadataPacket> {

    private String id;
    private SoundLocation location;
    private Set<String> metadata;

    private CompoundTag nbt;

    public RemoveMetadataPacket() {}

    public RemoveMetadataPacket(String id, SoundLocation location, Set<String> metadata) {
        this.id = id;
        this.location = location;
        this.metadata = metadata;
    }

    public RemoveMetadataPacket(String id, CompoundTag nbt, Set<String> metadata) {
        this.id = id;
        this.nbt = nbt;
        this.metadata = metadata;
    }

    @Override
    public void encode(RemoveMetadataPacket packet, RegistryFriendlyByteBuf buf) {
        buf.writeUtf(packet.id);
        buf.writeNbt(packet.location.serializeNbt());
        buf.writeCollection(packet.metadata, (b, v) -> b.writeUtf(v));
    }

    @Override
    public RemoveMetadataPacket decode(RegistryFriendlyByteBuf buf) {
        return new RemoveMetadataPacket(
            buf.readUtf(), 
            buf.readNbt(), 
            buf.readCollection(LinkedHashSet::new, b -> b.readUtf())
        );
    }

    @Override
    public void handle(RemoveMetadataPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            SoundLocation location = SoundLocation.fromNbt(packet.nbt, contextSupplier.get().getPlayer().level());
            try {
                SoundFile.removeMetadataInternal(location, packet.id, packet.metadata);
            } catch (IOException e) {
                DragNSounds.LOGGER.error("Unable to remove metadata.", e);
            }
        });
    }
    
}
