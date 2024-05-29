package de.mrjulsen.dragnsounds.net.stc;

import java.util.Optional;
import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.callbacks.client.SoundFileCallback;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class SoundFileResponsePacket implements IPacketBase<SoundFileResponsePacket> {

    private long requestId;
    private SoundFile file;

    private CompoundTag nbt;

    public SoundFileResponsePacket() {}

    public SoundFileResponsePacket(long requestId, SoundFile file) {
        this.requestId = requestId;
        this.file = file;
    }

    private SoundFileResponsePacket(long requestId, CompoundTag nbt) {
        this.requestId = requestId;
        this.nbt = nbt;
    }

    @Override
    public void encode(SoundFileResponsePacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        boolean b = packet.file != null;
        buf.writeBoolean(b);
        if (b) {
            buf.writeNbt(packet.file.serializeNbt());
        }
    }

    @Override
    public SoundFileResponsePacket decode(FriendlyByteBuf buf) {
        return new SoundFileResponsePacket(
            buf.readLong(),
            buf.readBoolean() ? buf.readNbt() : null
        );
    }

    @Override
    public void handle(SoundFileResponsePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                Optional<SoundFile> file = packet.nbt == null ? Optional.empty() : Optional.of(SoundFile.fromNbt(nbt, contextSupplier.get().getPlayer().getLevel()));
                SoundFileCallback.run(packet.requestId, file);
            });
        });
    }

}
