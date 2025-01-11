package de.mrjulsen.dragnsounds.net.stc;

import java.util.Optional;
import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ClientInstanceManager;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundUploadCallback;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class UploadSuccessPacket extends BaseNetworkPacket<UploadSuccessPacket> {

    private long requestId;
    private SoundFile file;

    private CompoundTag nbt;

    public UploadSuccessPacket() {}

    public UploadSuccessPacket(long requestId, SoundFile file) {
        this.requestId = requestId;
        this.file = file;
    }

    private UploadSuccessPacket(long requestId, CompoundTag nbt) {
        this.requestId = requestId;
        this.nbt = nbt;
    }

    @Override
    public void encode(UploadSuccessPacket packet, RegistryFriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeNbt(packet.file.serializeNbt());
    }

    @Override
    public UploadSuccessPacket decode(RegistryFriendlyByteBuf buf) {
        return new UploadSuccessPacket(
            buf.readLong(),
            buf.readNbt()
        );
    }

    @Override
    public void handle(UploadSuccessPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                SoundUploadCallback.run(packet.requestId, Optional.ofNullable(SoundFile.fromNbt(packet.nbt, contextSupplier.get().getPlayer().level())));
                ClientInstanceManager.closeUploadCallbacks(packet.requestId);
            });
        });
    }
    
}
