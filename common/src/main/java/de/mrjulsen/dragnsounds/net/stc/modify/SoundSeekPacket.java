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

public class SoundSeekPacket implements IPacketBase<SoundSeekPacket> {

    private SoundFile file;
    private long requestId;
    private int ticks;

    private CompoundTag nbt;

    public SoundSeekPacket() {}

    public SoundSeekPacket(SoundFile file, long requestId, int ticks) {
        this.requestId = requestId;
        this.ticks = ticks;
        this.file = file;
    }
    
    private SoundSeekPacket(CompoundTag nbt, long requestId, int ticks) {
        this.requestId = requestId;
        this.ticks = ticks;
        this.nbt = nbt;
    }

    @Override
    public void encode(SoundSeekPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.file != null);
        if (packet.file == null) {
            buf.writeLong(packet.requestId);
        } else {
            buf.writeNbt(packet.file.serializeNbt());
        }
        buf.writeInt(packet.ticks);
    }

    @Override
    public SoundSeekPacket decode(FriendlyByteBuf buf) {
        boolean hasFile = buf.readBoolean();
        return new SoundSeekPacket(
            hasFile ? buf.readNbt() : null,
            !hasFile ? buf.readLong() : 0,
            buf.readInt()
        );
    }

    @Override
    public void handle(SoundSeekPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                if (packet.nbt != null) {
                    SoundFile file = SoundFile.fromNbt(packet.nbt, contextSupplier.get().getPlayer().getLevel());
                    Arrays.stream(ClientInstanceManager.getInstancesOfSound(file)).forEach(x -> ClientSoundManager.seek(x, packet.ticks));
                } else {
                    ClientSoundManager.seek(packet.requestId, packet.ticks);
                }
                
            });
        });
    }
    
}
