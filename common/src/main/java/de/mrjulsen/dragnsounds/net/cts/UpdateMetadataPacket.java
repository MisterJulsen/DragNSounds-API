package de.mrjulsen.dragnsounds.net.cts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;

public class UpdateMetadataPacket extends NetworkPacketData {

    private static final String NBT_ID = "Id";
    private static final String NBT_LOCATION = "Location";
    private static final String NBT_METADATA = "Metadata";

    private String id;
    private SoundLocation location;
    private Map<String, String> metadata;

    private CompoundTag nbt;

    public UpdateMetadataPacket(DLStatus status) {
        super(status);
    }

    public UpdateMetadataPacket(String id, SoundLocation location, Map<String, String> metadata) {
        super(DLStatus.OK);
        this.id = id;
        this.location = location;
        this.metadata = metadata;
    }

    public UpdateMetadataPacket(String id, CompoundTag nbt, Map<String, String> metadata) {
        super(DLStatus.OK);
        this.id = id;
        this.nbt = nbt;
        this.metadata = metadata;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putString(NBT_ID, id);
        nbt.put(NBT_LOCATION, location.serializeNbt());
        CompoundTag meta = new CompoundTag();
        if (metadata != null) {
            for (Map.Entry<String, String> e : metadata.entrySet()) {
                meta.putString(e.getKey(), e.getValue());
            }
        }
        nbt.put(NBT_METADATA, meta);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.id = nbt.getString(NBT_ID);
        this.nbt = nbt.getCompound(NBT_LOCATION);
        CompoundTag meta = nbt.getCompound(NBT_METADATA);
        this.metadata = new HashMap<>();
        for (String key : meta.getAllKeys()) {
            this.metadata.put(key, meta.getString(key));
        }
    }

    public static void handle(UpdateMetadataPacket packet, NetworkPacketContext context) {
        SoundLocation location = SoundLocation.fromNbt(packet.nbt, context.getPlayer().level());
        try {
            SoundFile.updateMetadataInternal(location, packet.id, packet.metadata);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
