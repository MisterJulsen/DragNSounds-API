package de.mrjulsen.dragnsounds.net.stc.modify;

import java.util.Arrays;
import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ClientInstanceManager;
import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class SoundPauseResumePacket implements IPacketBase<SoundPauseResumePacket> {

    private SoundFile file;
    private long requestId;
    private boolean pause;

    private CompoundTag nbt;

    public SoundPauseResumePacket() {}

    public SoundPauseResumePacket(SoundFile file, long requestId, boolean pause) {
        this.requestId = requestId;
        this.pause = pause;
        this.file = file;
    }
    
    private SoundPauseResumePacket(CompoundTag nbt, long requestId, boolean pause) {
        this.requestId = requestId;
        this.pause = pause;
        this.nbt = nbt;
    }

    @Override
    public void encode(SoundPauseResumePacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.file != null);
        if (packet.file == null) {
            buf.writeLong(packet.requestId);
        } else {
            buf.writeNbt(packet.file.serializeNbt());
        }
        buf.writeBoolean(packet.pause);
    }

    @Override
    public SoundPauseResumePacket decode(FriendlyByteBuf buf) {
        boolean hasFile = buf.readBoolean();
        return new SoundPauseResumePacket(
            hasFile ? buf.readNbt() : null,
            !hasFile ? buf.readLong() : 0,
            buf.readBoolean()
        );
    }

    @Override
    public void handle(SoundPauseResumePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                if (packet.nbt != null) {
                    SoundFile file = SoundFile.fromNbt(packet.nbt, contextSupplier.get().getPlayer().getLevel());
                    Arrays.stream(ClientInstanceManager.getInstancesOfSound(file)).forEach(x -> ClientSoundManager.setPaused(x, packet.pause));
                } else {
                    ClientSoundManager.setPaused(packet.requestId, packet.pause);
                }
                
            });
        });
    }
    
}
