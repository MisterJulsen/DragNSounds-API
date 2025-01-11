package de.mrjulsen.dragnsounds.net.stc;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.callbacks.client.SoundStartUploadCallback;
import de.mrjulsen.mcdragonlib.data.StatusResult;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class StartUploadResponsePacket extends BaseNetworkPacket<StartUploadResponsePacket> {

    private long requestId;
    private StatusResult status;

    public StartUploadResponsePacket() {}

    public StartUploadResponsePacket(long requestId, StatusResult status) {
        this.requestId = requestId;
        this.status = status;
    }

    @Override
    public void encode(StartUploadResponsePacket packet, RegistryFriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeBoolean(packet.status.result());
        buf.writeInt(packet.status.code());
        buf.writeUtf(packet.status.message());
    }

    @Override
    public StartUploadResponsePacket decode(RegistryFriendlyByteBuf buf) {
        return new StartUploadResponsePacket(
            buf.readLong(),
            new StatusResult(buf.readBoolean(), buf.readInt(), buf.readUtf())
        );
    }

    @Override
    public void handle(StartUploadResponsePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                SoundStartUploadCallback.run(packet.requestId, packet.status);
            });
        });
    }
}
