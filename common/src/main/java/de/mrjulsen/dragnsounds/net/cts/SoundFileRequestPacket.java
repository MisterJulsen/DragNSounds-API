package de.mrjulsen.dragnsounds.net.cts;

import java.io.IOException;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.net.stc.SoundFileResponsePacket;
import de.mrjulsen.dragnsounds.registry.ModNetworkManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class SoundFileRequestPacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_ID = "Id";
    private static final String NBT_LOCATION = "Location";

    private long requestId;
    private String id;
    private SoundLocation location;

    private CompoundTag nbt;

    public SoundFileRequestPacket(DLStatus status) {
        super(status);
    }

    public SoundFileRequestPacket(long requestId, String id, SoundLocation location) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.id = id;
        this.location = location;
    }

    public SoundFileRequestPacket(long requestId, String id, CompoundTag nbt) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.id = id;
        this.nbt = nbt;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putLong(NBT_REQUEST_ID, requestId);
        nbt.putString(NBT_ID, id);
        nbt.put(NBT_LOCATION, location.serializeNbt());
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.requestId = nbt.getLong(NBT_REQUEST_ID);
        this.id = nbt.getString(NBT_ID);
        this.nbt = nbt.getCompound(NBT_LOCATION);
    }

    public static void handle(SoundFileRequestPacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            try {
                SoundFile file = ServerSoundManager.getSoundFile(SoundLocation.fromNbt(packet.nbt, context.getPlayer().level()), packet.id);
                ModNetworkManager.SOUND_FILE_RESPONSE.send(NetworkDirection.toPlayer((ServerPlayer)context.getPlayer()), new SoundFileResponsePacket(packet.requestId, file));
            } catch (IOException e) {
                DragNSounds.LOGGER.warn("Could not find sound file.", e);
                ModNetworkManager.SOUND_FILE_RESPONSE.send(NetworkDirection.toPlayer((ServerPlayer)context.getPlayer()), new SoundFileResponsePacket(packet.requestId, (SoundFile) null));
            }
        });
    }
}
