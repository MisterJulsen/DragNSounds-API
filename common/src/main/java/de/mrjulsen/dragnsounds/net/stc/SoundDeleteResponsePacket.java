package de.mrjulsen.dragnsounds.net.stc;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.callbacks.client.SoundDeleteCallback;
import de.mrjulsen.mcdragonlib.data.StatusResult;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.FriendlyByteBuf;

public class SoundDeleteResponsePacket implements IPacketBase<SoundDeleteResponsePacket> {

    private long requestId;
    private StatusResult result;

    public SoundDeleteResponsePacket() {}   

    public SoundDeleteResponsePacket(long requestId, StatusResult result) {
        this.requestId = requestId;
        this.result = result;
    }

    @Override
    public void encode(SoundDeleteResponsePacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeBoolean(packet.result.result());
        buf.writeInt(packet.result.code());
        buf.writeUtf(packet.result.message());
    }

    @Override
    public SoundDeleteResponsePacket decode(FriendlyByteBuf buf) {
        return new SoundDeleteResponsePacket(
            buf.readLong(), 
            new StatusResult(buf.readBoolean(), buf.readInt(), buf.readUtf())
        );
    }

    @Override
    public void handle(SoundDeleteResponsePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                SoundDeleteCallback.run(packet.requestId, packet.result);
            });
        });
    }

}
