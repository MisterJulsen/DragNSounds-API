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

public class SoundDopplerPacket implements IPacketBase<SoundDopplerPacket> {

    private SoundFile file;
    private long requestId;
    private float doppler;
    private Vec3 velocity;

    private CompoundTag nbt;

    public SoundDopplerPacket() {}

    public SoundDopplerPacket(SoundFile file, long requestId, float doppler, Vec3 velocity) {
        this.requestId = requestId;
        this.doppler = doppler;
        this.velocity = velocity;
        this.file = file;
    }
    
    private SoundDopplerPacket(CompoundTag nbt, long requestId, float doppler, Vec3 velocity) {
        this.requestId = requestId;
        this.doppler = doppler;
        this.velocity = velocity;
        this.nbt = nbt;
    }

    @Override
    public void encode(SoundDopplerPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.file != null);
        if (packet.file == null) {
            buf.writeLong(packet.requestId);
        } else {
            buf.writeNbt(packet.file.serializeNbt());
        }
        buf.writeFloat(packet.doppler);
        buf.writeDouble(packet.velocity.x());
        buf.writeDouble(packet.velocity.y());
        buf.writeDouble(packet.velocity.z());
    }

    @Override
    public SoundDopplerPacket decode(FriendlyByteBuf buf) {
        boolean hasFile = buf.readBoolean();
        return new SoundDopplerPacket(
            hasFile ? buf.readNbt() : null,
            !hasFile ? buf.readLong() : 0,
            buf.readFloat(),
            new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat())
        );
    }

    @Override
    public void handle(SoundDopplerPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                if (packet.nbt != null) {
                    SoundFile file = SoundFile.fromNbt(packet.nbt, contextSupplier.get().getPlayer().level());
                    Arrays.stream(ClientInstanceManager.getInstancesOfSound(file)).forEach(x -> ClientSoundManager.setDoppler(x, packet.doppler, packet.velocity));
                } else {
                    ClientSoundManager.setDoppler(packet.requestId, packet.doppler, packet.velocity);
                }
            });
        });
    }
    
}
