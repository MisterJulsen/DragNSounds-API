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

public class SoundPositionPacket extends NetworkPacketData {

    private static final String NBT_HAS_FILE = "HasFile";
    private static final String NBT_FILE = "File";
    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_X = "X";
    private static final String NBT_Y = "Y";
    private static final String NBT_Z = "Z";

    private CompoundTag nbt;
    private long requestId;
    private Vec3 pos;

    public SoundPositionPacket(DLStatus status) { super(status); }

    public SoundPositionPacket(SoundFile file, long requestId, Vec3 pos) {
        super(DLStatus.OK);
        this.nbt = file != null ? file.serializeNbt() : null;
        this.requestId = requestId;
        this.pos = pos;
    }

    @Override protected void write(CompoundTag tag) {
        tag.putBoolean(NBT_HAS_FILE, nbt != null);
        if (nbt != null) tag.put(NBT_FILE, nbt);
        else tag.putLong(NBT_REQUEST_ID, requestId);
        tag.putDouble(NBT_X, pos.x());
        tag.putDouble(NBT_Y, pos.y());
        tag.putDouble(NBT_Z, pos.z());
    }

    @Override protected void read(CompoundTag tag) {
        boolean hasFile = tag.getBoolean(NBT_HAS_FILE);
        this.nbt = hasFile ? tag.getCompound(NBT_FILE) : null;
        this.requestId = hasFile ? 0 : tag.getLong(NBT_REQUEST_ID);
        this.pos = new Vec3(tag.getDouble(NBT_X), tag.getDouble(NBT_Y), tag.getDouble(NBT_Z));
    }

    public static void handle(SoundPositionPacket packet, NetworkPacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            if (packet.nbt != null) {
                SoundFile file = SoundFile.fromNbt(packet.nbt, context.getPlayer().level());
                Arrays.stream(ClientInstanceManager.getInstancesOfSound(file)).forEach(x -> ClientSoundManager.setPosition(x, packet.pos));
            } else {
                ClientSoundManager.setPosition(packet.requestId, packet.pos);
            }
        });
    }
}
