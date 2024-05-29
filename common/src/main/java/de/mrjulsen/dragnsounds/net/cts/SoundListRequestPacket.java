package de.mrjulsen.dragnsounds.net.cts;

import java.util.Optional;
import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.filter.IFilter;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.registry.FilterRegistry;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

public class SoundListRequestPacket implements IPacketBase<SoundListRequestPacket> {

    private long requestId;
    private IFilter<SoundFile>[] filter;

    public SoundListRequestPacket() {}

    public SoundListRequestPacket(long requestId, IFilter<SoundFile>[] filter) {
        this.requestId = requestId;
        this.filter = filter;
    }

    @Override
    public void encode(SoundListRequestPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeInt(packet.filter.length);
        for (IFilter<SoundFile> f : packet.filter) {
            buf.writeResourceLocation(f.getFilterId());
            buf.writeUtf(f.key());
            buf.writeUtf(f.value());
            buf.writeByte(f.compareOperation().getId());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public SoundListRequestPacket decode(FriendlyByteBuf buf) {
        long requestId = buf.readLong();
        int size = buf.readInt();
        IFilter<SoundFile>[] filter = new IFilter[size];
        for (int i = 0; i < size; i++) {
            Optional<IFilter<?>> filt = FilterRegistry.get(buf.readResourceLocation(), buf.readUtf(), buf.readUtf(), ECompareOperation.getById(buf.readByte()));
            filter[i] = filt.isPresent() ? (IFilter<SoundFile>)filt.get() : null;
        }
        return new SoundListRequestPacket(requestId, filter);
    }

    @Override
    public void handle(SoundListRequestPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerSoundManager.sendFileListToPlayer(contextSupplier.get().getPlayer(), packet.requestId, contextSupplier.get().getPlayer().getLevel(), packet.filter);
        });
    }
    
}
