package de.mrjulsen.dragnsounds.net.cts;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class RemoveMetadataPacket extends NetworkPacketData {

    private static final String NBT_ID = "Id";
    private static final String NBT_LOCATION = "Location";
    private static final String NBT_METADATA = "Metadata";

    private String id;
    private SoundLocation location;
    private Set<String> metadata;

    private CompoundTag nbt;

    public RemoveMetadataPacket(DLStatus status) {
        super(status);
    }

    public RemoveMetadataPacket(String id, SoundLocation location, Set<String> metadata) {
        super(DLStatus.OK);
        this.id = id;
        this.location = location;
        this.metadata = metadata;
    }

    public RemoveMetadataPacket(String id, CompoundTag nbt, Set<String> metadata) {
        super(DLStatus.OK);
        this.id = id;
        this.nbt = nbt;
        this.metadata = metadata;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putString(NBT_LOCATION, id);
        nbt.put(NBT_LOCATION, location.serializeNbt());
        ListTag list = new ListTag();
        for (String meta : metadata) {
            list.add(StringTag.valueOf(meta));
        }
        nbt.put(NBT_METADATA, list);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.id = nbt.getString(NBT_ID);
        this.nbt = nbt.getCompound(NBT_LOCATION);
        this.metadata = nbt.getList(NBT_METADATA, Tag.TAG_STRING).stream().map(x -> ((StringTag)x).getAsString()).collect(Collectors.toSet());
    }

    
    public static void handle(RemoveMetadataPacket packet, NetworkPacketContext context) {
        SoundLocation location = SoundLocation.fromNbt(packet.nbt, context.getPlayer().level());
        try {
            SoundFile.removeMetadataInternal(location, packet.id, packet.metadata);
        } catch (IOException e) {
            DragNSounds.LOGGER.error("Unable to remove metadata.", e);
        }
    }
    
}
