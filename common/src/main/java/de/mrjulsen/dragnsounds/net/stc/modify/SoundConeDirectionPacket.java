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

public class SoundConeDirectionPacket implements IPacketBase<SoundConeDirectionPacket> {

    private SoundFile file;
    private long requestId;
    private float angleA;
    private float angleB;
    private float outerGain;
    private Vec3 direction;

    private CompoundTag nbt;

    public SoundConeDirectionPacket() {}

    public SoundConeDirectionPacket(SoundFile file, long requestId, float angleA, float angleB, float outerGain, Vec3 direction) {
        this.file = file;
        this.requestId = requestId;
        this.angleA = angleA;
        this.angleB = angleB;
        this.outerGain = outerGain;
        this.direction = direction;
    }

    private SoundConeDirectionPacket(CompoundTag nbt, long requestId, float angleA, float angleB, float outerGain, Vec3 direction) {
        this.nbt = nbt;
        this.angleA = angleA;
        this.angleB = angleB;
        this.outerGain = outerGain;
        this.direction = direction;
    }

    @Override
    public void encode(SoundConeDirectionPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.file != null);
        if (packet.file == null) {
            buf.writeLong(packet.requestId);
        } else {
            buf.writeNbt(packet.file.serializeNbt());
        }
        buf.writeFloat(packet.angleA);
        buf.writeFloat(packet.angleB);
        buf.writeFloat(packet.outerGain);
        buf.writeDouble(packet.direction.x());
        buf.writeDouble(packet.direction.y());
        buf.writeDouble(packet.direction.z());
    }

    @Override
    public SoundConeDirectionPacket decode(FriendlyByteBuf buf) {
        boolean hasFile = buf.readBoolean();
        return new SoundConeDirectionPacket(
            hasFile ? buf.readNbt() : null,
            !hasFile ? buf.readLong() : 0,
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat())
        );
    }

    @Override
    public void handle(SoundConeDirectionPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                if (packet.nbt != null) {
                    SoundFile file = SoundFile.fromNbt(packet.nbt, contextSupplier.get().getPlayer().getLevel());
                    Arrays.stream(ClientInstanceManager.getInstancesOfSound(file)).forEach(x -> ClientSoundManager.setCone(x, packet.direction, packet.angleA, packet.angleB, packet.outerGain));
                } else {
                    ClientSoundManager.setCone(packet.requestId, packet.direction, packet.angleA, packet.angleB, packet.outerGain);
                }
            });
        });
    }
    
}
