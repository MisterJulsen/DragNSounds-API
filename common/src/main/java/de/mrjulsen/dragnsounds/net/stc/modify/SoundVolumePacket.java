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

public class SoundVolumePacket implements IPacketBase<SoundVolumePacket> {

    private SoundFile file;
    private long requestId;
    private float volume;
    private float pitch;
    private int attenuationDistance;

    private CompoundTag nbt;

    public SoundVolumePacket() {}

    public SoundVolumePacket(SoundFile file, long requestId, float volume, float pitch, int attenuationDistance) {
        this.requestId = requestId;
        this.volume = volume;
        this.pitch = pitch;
        this.attenuationDistance = attenuationDistance;
        this.file = file;
    }
    
    private SoundVolumePacket(CompoundTag nbt, long requestId, float volume, float pitch, int attenuationDistance) {
        this.requestId = requestId;
        this.volume = volume;
        this.pitch = pitch;
        this.attenuationDistance = attenuationDistance;
        this.nbt = nbt;
    }

    @Override
    public void encode(SoundVolumePacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.file != null);
        if (packet.file == null) {
            buf.writeLong(packet.requestId);
        } else {
            buf.writeNbt(packet.file.serializeNbt());
        }
        buf.writeFloat(packet.volume);
        buf.writeFloat(packet.pitch);
        buf.writeInt(packet.attenuationDistance);
    }

    @Override
    public SoundVolumePacket decode(FriendlyByteBuf buf) {
        boolean hasFile = buf.readBoolean();
        return new SoundVolumePacket(
            hasFile ? buf.readNbt() : null,
            !hasFile ? buf.readLong() : 0,
            buf.readFloat(),
            buf.readFloat(),
            buf.readInt()
        );
    }

    @Override
    public void handle(SoundVolumePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                if (packet.nbt != null) {
                    SoundFile file = SoundFile.fromNbt(packet.nbt, contextSupplier.get().getPlayer().level());
                    Arrays.stream(ClientInstanceManager.getInstancesOfSound(file)).forEach(x -> apply(x, packet.volume, packet.pitch, packet.attenuationDistance));
                } else {
                    apply(packet.requestId, packet.volume, packet.pitch, packet.attenuationDistance);
                }
            });
        });
    }

    private void apply(long id, float volume, float pitch, int attenuationDistance) {
        if (volume >= 0) {
            ClientSoundManager.setVolume(id, volume);
        }
        
        if (pitch >= 0) {
            ClientSoundManager.setPitch(id, pitch);
        }
        
        if (attenuationDistance >= 0) {
            ClientSoundManager.setAttenuationDistance(id, attenuationDistance);
        }
    }
    
}
