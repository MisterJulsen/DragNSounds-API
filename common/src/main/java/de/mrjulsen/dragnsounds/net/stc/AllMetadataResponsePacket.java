package de.mrjulsen.dragnsounds.net.stc;

import java.util.HashMap;
import java.util.Map;

import de.mrjulsen.dragnsounds.core.callbacks.client.SoundMetadataCallback;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class AllMetadataResponsePacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_METADATA = "Metadata";

    private long requestId;
    private Map<String, String> metadata;

    public AllMetadataResponsePacket(DLStatus status) { super(status); }
    public AllMetadataResponsePacket(long requestId, Map<String, String> metadata) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.metadata = metadata;
    }

    @Override protected void write(CompoundTag tag) {
        tag.putLong(NBT_REQUEST_ID, requestId);
        CompoundTag m = new CompoundTag();
        if (metadata != null) for (Map.Entry<String,String> e : metadata.entrySet()) m.putString(e.getKey(), e.getValue());
        tag.put(NBT_METADATA, m);
    }

    @Override protected void read(CompoundTag tag) {
        this.requestId = tag.getLong(NBT_REQUEST_ID);
        CompoundTag m = tag.getCompound(NBT_METADATA);
        this.metadata = new HashMap<>();
        for (String k : m.getAllKeys()) this.metadata.put(k, m.getString(k));
    }

    public static void handle(AllMetadataResponsePacket packet, NetworkPacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            SoundMetadataCallback.run(packet.requestId, packet.metadata);
        });
    }
}
