package de.mrjulsen.dragnsounds.net.stc.modify;

import java.util.Arrays;

import de.mrjulsen.dragnsounds.core.ClientInstanceManager;
import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class SoundVolumePacket extends NetworkPacketData {

    private static final String NBT_HAS_FILE = "HasFile";
    private static final String NBT_FILE = "File";
    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_VOLUME = "Volume";
    private static final String NBT_PITCH = "Pitch";
    private static final String NBT_ATT = "Att";

    private CompoundTag nbt;
    private long requestId;
    private float volume;
    private float pitch;
    private int attenuationDistance;

    public SoundVolumePacket(DLStatus status) { super(status); }

    public SoundVolumePacket(SoundFile file, long requestId, float volume, float pitch, int attenuationDistance) {
        super(DLStatus.OK);
        this.nbt = file != null ? file.serializeNbt() : null;
        this.requestId = requestId;
        this.volume = volume;
        this.pitch = pitch;
        this.attenuationDistance = attenuationDistance;
    }

    @Override protected void write(CompoundTag tag) {
        tag.putBoolean(NBT_HAS_FILE, nbt != null);
        if (nbt != null) tag.put(NBT_FILE, nbt);
        else tag.putLong(NBT_REQUEST_ID, requestId);
        tag.putFloat(NBT_VOLUME, volume);
        tag.putFloat(NBT_PITCH, pitch);
        tag.putInt(NBT_ATT, attenuationDistance);
    }

    @Override protected void read(CompoundTag tag) {
        boolean hasFile = tag.getBoolean(NBT_HAS_FILE);
        this.nbt = hasFile ? tag.getCompound(NBT_FILE) : null;
        this.requestId = hasFile ? 0 : tag.getLong(NBT_REQUEST_ID);
        this.volume = tag.getFloat(NBT_VOLUME);
        this.pitch = tag.getFloat(NBT_PITCH);
        this.attenuationDistance = tag.getInt(NBT_ATT);
    }

    public static void handle(SoundVolumePacket packet, NetworkPacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            if (packet.nbt != null) {
                SoundFile file = SoundFile.fromNbt(packet.nbt, context.getPlayer().level());
                Arrays.stream(ClientInstanceManager.getInstancesOfSound(file)).forEach(x -> apply(x, packet.volume, packet.pitch, packet.attenuationDistance));
            } else {
                apply(packet.requestId, packet.volume, packet.pitch, packet.attenuationDistance);
            }
        });
    }

    private static void apply(long id, float volume, float pitch, int attenuationDistance) {
        if (volume >= 0) ClientSoundManager.setVolume(id, volume);
        if (pitch >= 0) ClientSoundManager.setPitch(id, pitch);
        if (attenuationDistance >= 0) ClientSoundManager.setAttenuationDistance(id, attenuationDistance);
    }
}
