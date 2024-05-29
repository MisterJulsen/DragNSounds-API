package de.mrjulsen.dragnsounds.net.stc;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ClientInstanceManager;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundErrorCallback;
import de.mrjulsen.mcdragonlib.data.StatusResult;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.FriendlyByteBuf;

public class UploadFailedPacket implements IPacketBase<UploadFailedPacket> {

    private long requestId;
    private StatusResult error;

    public UploadFailedPacket() {}

    public UploadFailedPacket(long requestId, StatusResult error) {
        this.requestId = requestId;
        this.error = error;
    }

    @Override
    public void encode(UploadFailedPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeBoolean(packet.error.result());
        buf.writeInt(packet.error.code());
        buf.writeUtf(packet.error.message());
    }

    @Override
    public UploadFailedPacket decode(FriendlyByteBuf buf) {
        return new UploadFailedPacket(
            buf.readLong(),
            new StatusResult(buf.readBoolean(), buf.readInt(), buf.readUtf())
        );
    }

    @Override
    public void handle(UploadFailedPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                SoundErrorCallback.run(packet.requestId, packet.error);
                ClientInstanceManager.closeUploadCallbacks(packet.requestId);
            });
        });
    }
    
}
