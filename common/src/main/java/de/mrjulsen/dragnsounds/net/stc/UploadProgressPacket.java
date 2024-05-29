package de.mrjulsen.dragnsounds.net.stc;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.api.ClientApi.UploadProgress;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundUploadProgressCallback;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.FriendlyByteBuf;

public class UploadProgressPacket implements IPacketBase<UploadProgressPacket> {

    private long requestId;
    private UploadProgress progress;

    public UploadProgressPacket() {}

    public UploadProgressPacket(long requestId, UploadProgress progress) {
        this.requestId = requestId;
        this.progress = progress;
    }

    @Override
    public void encode(UploadProgressPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeNbt(packet.progress.toNbt());
    }

    @Override
    public UploadProgressPacket decode(FriendlyByteBuf buf) {
        return new UploadProgressPacket(
            buf.readLong(),
            UploadProgress.fromNbt(buf.readNbt())
        );
    }

    @Override
    public void handle(UploadProgressPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                SoundUploadProgressCallback.run(packet.requestId, packet.progress);
            });
        });
    }
    
}
