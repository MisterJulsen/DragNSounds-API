package de.mrjulsen.dragnsounds.net.cts;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.filter.IFilter;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.registry.FilterRegistry;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public class SoundListRequestPacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_FILTERS = "Filters";

    private long requestId;
    private IFilter<SoundFile>[] filter;

    public SoundListRequestPacket(DLStatus status) {
        super(status);
    }

    public SoundListRequestPacket(long requestId, IFilter<SoundFile>[] filter) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.filter = filter;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putLong(NBT_REQUEST_ID, requestId);
        ListTag list = new ListTag();
        if (filter != null) {
            for (IFilter<SoundFile> f : filter) {
                if (f == null) {
                    list.add(StringTag.valueOf("null"));
                } else {
                    CompoundTag c = new CompoundTag();
                    c.putString("Id", f.getFilterId().toString());
                    c.putString("Key", f.key());
                    c.putString("Value", f.value());
                    c.putByte("Op", (byte) f.compareOperation().getId());
                    list.add(c);
                }
            }
        }
        nbt.put(NBT_FILTERS, list);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void read(CompoundTag nbt) {
        this.requestId = nbt.getLong(NBT_REQUEST_ID);
        ListTag list = nbt.getList(NBT_FILTERS, 10); // compounds or strings
        List<IFilter<SoundFile>> filters = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == Tag.TAG_STRING) {
                filters.add(null);
            } else {
                CompoundTag c = (CompoundTag) list.get(i);
                ResourceLocation id = DLUtils.resourceLocation(c.getString("Id"));
                String key = c.getString("Key");
                String value = c.getString("Value");
                byte op = c.getByte("Op");
                Optional<IFilter<?>> filt = FilterRegistry.get(id, key, value, ECompareOperation.getById(op));
                filters.add(filt.isPresent() ? (IFilter<SoundFile>) filt.get() : null);
            }
        }
        this.filter = filters.toArray(new IFilter[0]);
    }

    public static void handle(SoundListRequestPacket packet, NetworkPacketContext context) {
        ServerSoundManager.sendFileListToPlayer(context.getPlayer(), packet.requestId, context.getPlayer().level(), packet.filter);
    }
}
