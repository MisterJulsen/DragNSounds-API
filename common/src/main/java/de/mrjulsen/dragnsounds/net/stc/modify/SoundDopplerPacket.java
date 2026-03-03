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
import net.minecraft.world.phys.Vec3;

public class SoundDopplerPacket extends NetworkPacketData {

    private static final String NBT_HAS_FILE = "HasFile";
    private static final String NBT_FILE = "File";
    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_DOPPLER = "Doppler";
    private static final String NBT_VX = "Vx";
    private static final String NBT_VY = "Vy";
    private static final String NBT_VZ = "Vz";

    private CompoundTag nbt;
    private long requestId;
    private float doppler;
    private Vec3 velocity;

    public SoundDopplerPacket(DLStatus status) { super(status); }

    public SoundDopplerPacket(SoundFile file, long requestId, float doppler, Vec3 velocity) {
        super(DLStatus.OK);
        this.nbt = file != null ? file.serializeNbt() : null;
        this.requestId = requestId;
        this.doppler = doppler;
        this.velocity = velocity;
    }

    @Override protected void write(CompoundTag tag) {
        tag.putBoolean(NBT_HAS_FILE, nbt != null);
        if (nbt != null) tag.put(NBT_FILE, nbt);
        else tag.putLong(NBT_REQUEST_ID, requestId);
        tag.putFloat(NBT_DOPPLER, doppler);
        tag.putDouble(NBT_VX, velocity.x());
        tag.putDouble(NBT_VY, velocity.y());
        tag.putDouble(NBT_VZ, velocity.z());
    }

    @Override protected void read(CompoundTag tag) {
        boolean hasFile = tag.getBoolean(NBT_HAS_FILE);
        this.nbt = hasFile ? tag.getCompound(NBT_FILE) : null;
        this.requestId = hasFile ? 0 : tag.getLong(NBT_REQUEST_ID);
        this.doppler = tag.getFloat(NBT_DOPPLER);
        this.velocity = new Vec3(tag.getDouble(NBT_VX), tag.getDouble(NBT_VY), tag.getDouble(NBT_VZ));
    }

    public static void handle(SoundDopplerPacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                if (packet.nbt != null) {
                    SoundFile file = SoundFile.fromNbt(packet.nbt, context.getPlayer().level());
                    Arrays.stream(ClientInstanceManager.getInstancesOfSound(file)).forEach(x -> ClientSoundManager.setDoppler(x, packet.doppler, packet.velocity));
                } else {
                    ClientSoundManager.setDoppler(packet.requestId, packet.doppler, packet.velocity);
                }
            });
        });
    }
}
