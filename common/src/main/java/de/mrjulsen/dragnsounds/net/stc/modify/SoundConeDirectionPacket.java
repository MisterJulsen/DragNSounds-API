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

public class SoundConeDirectionPacket extends NetworkPacketData {

    private static final String NBT_HAS_FILE = "HasFile";
    private static final String NBT_FILE = "File";
    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_ANGLE_A = "AngleA";
    private static final String NBT_ANGLE_B = "AngleB";
    private static final String NBT_OUTER_GAIN = "OuterGain";
    private static final String NBT_DIR_X = "DirX";
    private static final String NBT_DIR_Y = "DirY";
    private static final String NBT_DIR_Z = "DirZ";

    private SoundFile file;
    private long requestId;
    private float angleA;
    private float angleB;
    private float outerGain;
    private Vec3 direction;

    private CompoundTag nbt;

    public SoundConeDirectionPacket(DLStatus status) { super(status); }

    public SoundConeDirectionPacket(SoundFile file, long requestId, float angleA, float angleB, float outerGain, Vec3 direction) {
        super(DLStatus.OK);
        this.file = file;
        this.requestId = requestId;
        this.angleA = angleA;
        this.angleB = angleB;
        this.outerGain = outerGain;
        this.direction = direction;
    }

    @Override
    protected void write(CompoundTag tag) {
        tag.putBoolean(NBT_HAS_FILE, file != null);
        if (file != null) tag.put(NBT_FILE, file.serializeNbt());
        else tag.putLong(NBT_REQUEST_ID, requestId);
        tag.putFloat(NBT_ANGLE_A, angleA);
        tag.putFloat(NBT_ANGLE_B, angleB);
        tag.putFloat(NBT_OUTER_GAIN, outerGain);
        tag.putDouble(NBT_DIR_X, direction.x());
        tag.putDouble(NBT_DIR_Y, direction.y());
        tag.putDouble(NBT_DIR_Z, direction.z());
    }

    @Override
    protected void read(CompoundTag tag) {
        boolean hasFile = tag.getBoolean(NBT_HAS_FILE);
        if (hasFile) this.nbt = tag.getCompound(NBT_FILE);
        else this.requestId = tag.getLong(NBT_REQUEST_ID);
        this.angleA = tag.getFloat(NBT_ANGLE_A);
        this.angleB = tag.getFloat(NBT_ANGLE_B);
        this.outerGain = tag.getFloat(NBT_OUTER_GAIN);
        this.direction = new Vec3(tag.getDouble(NBT_DIR_X), tag.getDouble(NBT_DIR_Y), tag.getDouble(NBT_DIR_Z));
    }

    public static void handle(SoundConeDirectionPacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                if (packet.nbt != null) {
                    SoundFile file = SoundFile.fromNbt(packet.nbt, context.getPlayer().level());
                    Arrays.stream(ClientInstanceManager.getInstancesOfSound(file)).forEach(x -> ClientSoundManager.setCone(x, packet.direction, packet.angleA, packet.angleB, packet.outerGain));
                } else {
                    ClientSoundManager.setCone(packet.requestId, packet.direction, packet.angleA, packet.angleB, packet.outerGain);
                }
            });
        });
    }
}
