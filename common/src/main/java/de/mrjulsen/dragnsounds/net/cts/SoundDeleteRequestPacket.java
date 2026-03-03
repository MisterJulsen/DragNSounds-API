package de.mrjulsen.dragnsounds.net.cts;

import java.io.IOException;
import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.net.stc.SoundDeleteResponsePacket;
import de.mrjulsen.dragnsounds.registry.ModNetworkManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class SoundDeleteRequestPacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_LOCATION = "Location";
    private static final String NBT_ID = "Id";

    private long requestId;
    private SoundLocation location;
    private String id;

    private CompoundTag nbt;

    public SoundDeleteRequestPacket(DLStatus status) {
        super(status);
    }

    public SoundDeleteRequestPacket(long requestId, SoundLocation location, String id) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.location = location;
        this.id = id;
    }

    public SoundDeleteRequestPacket(long requestId, CompoundTag nbt, String id) {
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
        this.nbt = nbt.getCompound(NBT_LOCATION);
        this.id = nbt.getString(NBT_ID);
    }

    
    public static void handle(SoundDeleteRequestPacket packet, NetworkPacketContext context) {        
        SoundLocation loc = SoundLocation.fromNbt(packet.nbt, context.getPlayer().level());
        DLStatus result;
        try {
            result = ServerSoundManager.deleteSound(loc, packet.id);
        } catch (IOException e) {
            DragNSounds.LOGGER.error("Unable to delete sound file: " + packet.id, e);
            result = new DLStatus(DLStatus.FLAG_ERROR, Integer.MIN_VALUE, e.getLocalizedMessage());
        }
        ModNetworkManager.SOUND_DELETE_RESPONSE.send(NetworkDirection.toPlayer((ServerPlayer)context.getPlayer()), new SoundDeleteResponsePacket(packet.requestId, result));
    }
    
}
