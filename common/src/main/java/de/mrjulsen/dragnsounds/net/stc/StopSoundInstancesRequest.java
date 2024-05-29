package de.mrjulsen.dragnsounds.net.stc;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class StopSoundInstancesRequest implements IPacketBase<StopSoundInstancesRequest> {

    private SoundFile file;
    private CompoundTag nbt;

    public StopSoundInstancesRequest() {}

    public StopSoundInstancesRequest(SoundFile file) {
        this.file = file;
    }

    private StopSoundInstancesRequest(CompoundTag nbt) {
        this.nbt = nbt;
    }

    @Override
    public void encode(StopSoundInstancesRequest packet, FriendlyByteBuf buf) {
        buf.writeNbt(packet.file.serializeNbt());
    }

    @Override
    public StopSoundInstancesRequest decode(FriendlyByteBuf buf) {
        return new StopSoundInstancesRequest(
            buf.readNbt()
        );
    }

    @Override
    public void handle(StopSoundInstancesRequest packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                ClientSoundManager.stopAllSoundInstances(SoundFile.fromNbt(packet.nbt, contextSupplier.get().getPlayer().getLevel()));
            });
        });
    }
    
}
