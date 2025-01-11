package de.mrjulsen.dragnsounds.net.cts;

import java.util.Map;
import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.net.stc.AllMetadataResponsePacket;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.mcdragonlib.net.DLNetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class AllMetadataRequestPacket extends BaseNetworkPacket<AllMetadataRequestPacket> {

    private long requestId;
    private SoundLocation location;
    private String id;

    private CompoundTag nbt;

    public AllMetadataRequestPacket() {}

    public AllMetadataRequestPacket(long requestId, SoundFile file) {
        this.requestId = requestId;
        this.location = file.getLocation();
        this.id = file.getId();
    }

    private AllMetadataRequestPacket(long requestId, CompoundTag nbt, String id) {
        this.requestId = requestId;
        this.nbt = nbt;
        this.id = id;
    }

    @Override
    public void encode(AllMetadataRequestPacket packet, RegistryFriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeNbt(packet.location.serializeNbt());
        buf.writeUtf(packet.id);
    }

    @Override
    public AllMetadataRequestPacket decode(RegistryFriendlyByteBuf buf) {
        return new AllMetadataRequestPacket(
            buf.readLong(),
            buf.readNbt(),
            buf.readUtf()
        );
    }

    @Override
    public void handle(AllMetadataRequestPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            SoundLocation loc = SoundLocation.fromNbt(packet.nbt, contextSupplier.get().getPlayer().level());
            Map<String, String> metadata = ServerSoundManager.getAllSoundFileMetadata(loc, packet.id);
            DLNetworkManager.sendToPlayer((ServerPlayer)contextSupplier.get().getPlayer(), new AllMetadataResponsePacket(packet.requestId, metadata));
        });
    }
    
}
