package de.mrjulsen.dragnsounds.net.cts;

import java.util.Map;
import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.net.stc.AllMetadataResponsePacket;
import de.mrjulsen.dragnsounds.registry.ModNetworkManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class AllMetadataRequestPacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_LOCATION = "Location";
    private static final String NBT_ID = "Id";

    private long requestId;
    private SoundLocation location;
    private String id;

    private CompoundTag nbt;

    public AllMetadataRequestPacket(DLStatus status) {
        super(status);
    }

    public AllMetadataRequestPacket(long requestId, SoundFile file) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.location = file.getLocation();
        this.id = file.getId();
    }

    private AllMetadataRequestPacket(long requestId, CompoundTag nbt, String id) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.nbt = nbt;
        this.id = id;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putLong(NBT_REQUEST_ID, requestId);
        nbt.put(NBT_LOCATION, location.serializeNbt());
        nbt.putString(NBT_ID, id);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.requestId = nbt.getLong(NBT_REQUEST_ID);
        this.id = nbt.getString(NBT_ID);
        this.nbt = nbt.getCompound(NBT_LOCATION);
    }
    
    public static void handle(AllMetadataRequestPacket packet, NetworkPacketContext context) {        
        context.queue(() -> {
            SoundLocation loc = SoundLocation.fromNbt(packet.nbt, context.getPlayer().level());
            Map<String, String> metadata = ServerSoundManager.getAllSoundFileMetadata(loc, packet.id);
            ModNetworkManager.RESPONSE_ALL_METADATA.send(NetworkDirection.toPlayer((ServerPlayer)context.getPlayer()), new AllMetadataResponsePacket(packet.requestId, metadata));
        });
    }
    
}
