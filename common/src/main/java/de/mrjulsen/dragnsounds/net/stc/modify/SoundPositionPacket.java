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
import net.minecraft.world.phys.Vec3;

public class SoundPositionPacket implements IPacketBase<SoundPositionPacket> {

    private SoundFile file;
    private long requestId;
    private Vec3 pos;

    private CompoundTag nbt;

    public SoundPositionPacket() {}

    public SoundPositionPacket(SoundFile file, long requestId, Vec3 pos) {
        this.requestId = requestId;
        this.pos = pos;
        this.file = file;
    }
    
    private SoundPositionPacket(CompoundTag nbt, long requestId, Vec3 pos) {
        this.requestId = requestId;
        this.pos = pos;
        this.nbt = nbt;
    }

    @Override
    public void encode(SoundPositionPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.file != null);
        if (packet.file == null) {
            buf.writeLong(packet.requestId);
        } else {
            buf.writeNbt(packet.file.serializeNbt());
        }
        buf.writeDouble(packet.pos.x());
        buf.writeDouble(packet.pos.y());
        buf.writeDouble(packet.pos.z());
    }

    @Override
    public SoundPositionPacket decode(FriendlyByteBuf buf) {
        boolean hasFile = buf.readBoolean();
        return new SoundPositionPacket(
            hasFile ? buf.readNbt() : null,
            !hasFile ? buf.readLong() : 0,
            new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
        );
    }

    @Override
    public void handle(SoundPositionPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                if (packet.nbt != null) {
                    SoundFile file = SoundFile.fromNbt(packet.nbt, contextSupplier.get().getPlayer().level());
                    Arrays.stream(ClientInstanceManager.getInstancesOfSound(file)).forEach(x -> ClientSoundManager.setPosition(x, packet.pos));
                } else {
                    ClientSoundManager.setPosition(packet.requestId, packet.pos);
                }
            });
        });
    }
    
}
