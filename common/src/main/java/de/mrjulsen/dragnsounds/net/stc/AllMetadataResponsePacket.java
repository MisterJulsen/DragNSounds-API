package de.mrjulsen.dragnsounds.net.stc;

import java.util.Map;
import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.callbacks.client.SoundMetadataCallback;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.FriendlyByteBuf;

public class AllMetadataResponsePacket implements IPacketBase<AllMetadataResponsePacket> {

    private long requestId;
    private Map<String, String> metadata;

    public AllMetadataResponsePacket() {}

    public AllMetadataResponsePacket(long requestId, Map<String, String> metadata) {
        this.requestId = requestId;
        this.metadata = metadata;
    }

    @Override
    public void encode(AllMetadataResponsePacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeMap(packet.metadata, (b, k) -> b.writeUtf(k), (b, v) -> b.writeUtf(v));
    }

    @Override
    public AllMetadataResponsePacket decode(FriendlyByteBuf buf) {
        return new AllMetadataResponsePacket(
            buf.readLong(), 
            buf.readMap(b -> b.readUtf(), b -> b.readUtf())
        );
    }

    @Override
    public void handle(AllMetadataResponsePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                SoundMetadataCallback.run(packet.requestId, packet.metadata);
            });
        });
    }

}
