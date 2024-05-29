package de.mrjulsen.dragnsounds.net.stc;

import java.util.Arrays;
import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.callbacks.client.SoundListCallback;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class SoundListChunkResponsePacket implements IPacketBase<SoundListChunkResponsePacket> {

    private long requestId;
    private boolean hasMore;
    private SoundFile[] files;

    private CompoundTag[] nbt;

    public SoundListChunkResponsePacket() {}

    public SoundListChunkResponsePacket(long requestId, boolean hasMore, SoundFile[] files) {
        this.requestId = requestId;
        this.hasMore = hasMore;
        this.files = files;
    }

    private SoundListChunkResponsePacket(long requestId, boolean hasMore, CompoundTag[] nbt) {
        this.requestId = requestId;
        this.hasMore = hasMore;
        this.nbt = nbt;
    }

    @Override
    public void encode(SoundListChunkResponsePacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeBoolean(packet.hasMore);
        buf.writeInt(packet.files.length);
        for (int i = 0; i < packet.files.length; i++) {
            buf.writeNbt(packet.files[i].serializeNbt());
        }
    }

    @Override
    public SoundListChunkResponsePacket decode(FriendlyByteBuf buf) {
        long requestId = buf.readLong();
        boolean hasMore = buf.readBoolean();
        int size = buf.readInt();
        CompoundTag[] nbt = new CompoundTag[size];
        for (int i = 0; i < size; i++) {
            nbt[i] = buf.readNbt();
        }
        return new SoundListChunkResponsePacket(requestId, hasMore, nbt);
    }

    @Override
    public void handle(SoundListChunkResponsePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                Arrays.stream(packet.nbt).map(x -> SoundFile.fromNbt(x, contextSupplier.get().getPlayer().getLevel())).forEach(x -> SoundListCallback.get(packet.requestId).add(x));

                if (!packet.hasMore) {
                    SoundListCallback.runIfPresent(packet.requestId);
                }
            });
        });
    }
    
}
