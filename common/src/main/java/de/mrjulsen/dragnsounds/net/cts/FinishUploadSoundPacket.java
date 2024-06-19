package de.mrjulsen.dragnsounds.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ServerInstanceManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class FinishUploadSoundPacket implements IPacketBase<FinishUploadSoundPacket> {

    private long requestId;
    private int maxSize;
    private SoundFile.Builder file;
    private long initialDuration;
    private int initialChannels;

    private CompoundTag nbt;
    private Level level;

    public FinishUploadSoundPacket() {}

    public FinishUploadSoundPacket(long requestId, int maxSize, SoundFile.Builder file, int initialChannels, long initialDuration) {
        this.requestId = requestId;
        this.maxSize = maxSize;
        this.file = file;
        this.initialChannels = initialChannels;
    }

    private FinishUploadSoundPacket(long requestId, int maxSize, CompoundTag nbt, int initialChannels, long initialDuration) {
        this.requestId = requestId;
        this.maxSize = maxSize;
        this.nbt = nbt;
        this.initialChannels = initialChannels;
    }

    @Override
    public void encode(FinishUploadSoundPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeInt(packet.maxSize);
        buf.writeNbt(packet.file.serializeNbt());
        buf.writeInt(packet.initialChannels);
        buf.writeLong(packet.initialDuration);
    }

    @Override
    public FinishUploadSoundPacket decode(FriendlyByteBuf buf) {
        return new FinishUploadSoundPacket(
            buf.readLong(),
            buf.readInt(),
            buf.readNbt(),
            buf.readInt(),
            buf.readLong()
        );
    }

    @Override
    public void handle(FinishUploadSoundPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            packet.level = contextSupplier.get().getPlayer().level();
            ServerInstanceManager.getOrCreateUploadBuffer(packet.requestId, packet.maxSize, (ServerPlayer)contextSupplier.get().getPlayer()).setFinalizerPacket(packet);
        });
    }

    public long getRequestId() {
        return requestId;
    }

    public long getInitialDuration() {
        return initialDuration;
    }
    
    public int getInitialChannels() {
        return initialChannels;
    }

    public SoundFile.Builder getFile() {
        return SoundFile.Builder.fromNbt(nbt, level);
    }    
}
