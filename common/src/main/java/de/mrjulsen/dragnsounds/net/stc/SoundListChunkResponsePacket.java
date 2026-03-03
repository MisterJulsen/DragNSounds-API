package de.mrjulsen.dragnsounds.net.stc;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.dragnsounds.core.callbacks.client.SoundListCallback;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class SoundListChunkResponsePacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_HAS_MORE = "HasMore";
    private static final String NBT_FILES = "Files";

    private long requestId;
    private boolean hasMore;
    private CompoundTag[] nbt;

    public SoundListChunkResponsePacket(DLStatus status) { super(status); }
    public SoundListChunkResponsePacket(long requestId, boolean hasMore, SoundFile[] files) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.hasMore = hasMore;
        if (files != null) {
            List<CompoundTag> list = new ArrayList<>();
            for (SoundFile f : files) list.add(f.serializeNbt());
            this.nbt = list.toArray(new CompoundTag[0]);
        }
    }

    @Override protected void write(CompoundTag tag) {
        tag.putLong(NBT_REQUEST_ID, requestId);
        tag.putBoolean(NBT_HAS_MORE, hasMore);
        ListTag list = new ListTag();
        if (nbt != null) for (CompoundTag c : nbt) list.add(c);
        tag.put(NBT_FILES, list);
    }

    @Override protected void read(CompoundTag tag) {
        this.requestId = tag.getLong(NBT_REQUEST_ID);
        this.hasMore = tag.getBoolean(NBT_HAS_MORE);
        ListTag list = tag.getList(NBT_FILES, Tag.TAG_COMPOUND);
        this.nbt = new CompoundTag[list.size()];
        for (int i = 0; i < list.size(); i++) this.nbt[i] = list.getCompound(i);
    }

    public static void handle(SoundListChunkResponsePacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                for (CompoundTag t : packet.nbt) SoundListCallback.get(packet.requestId).add(SoundFile.fromNbt(t, context.getPlayer().level()));
                if (!packet.hasMore) SoundListCallback.runIfPresent(packet.requestId);
            });
        });
    }
}
