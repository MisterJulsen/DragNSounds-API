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

public class SoundPauseResumePacket extends NetworkPacketData {

    private static final String NBT_HAS_FILE = "HasFile";
    private static final String NBT_FILE = "File";
    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_PAUSE = "Pause";

    private CompoundTag nbt;
    private long requestId;
    private boolean pause;

    public SoundPauseResumePacket(DLStatus status) { super(status); }

    public SoundPauseResumePacket(SoundFile file, long requestId, boolean pause) {
        super(DLStatus.OK);
        this.nbt = file != null ? file.serializeNbt() : null;
        this.requestId = requestId;
        this.pause = pause;
    }

    @Override protected void write(CompoundTag tag) {
        tag.putBoolean(NBT_HAS_FILE, nbt != null);
        if (nbt != null) tag.put(NBT_FILE, nbt);
        else tag.putLong(NBT_REQUEST_ID, requestId);
        tag.putBoolean(NBT_PAUSE, pause);
    }

    @Override protected void read(CompoundTag tag) {
        boolean hasFile = tag.getBoolean(NBT_HAS_FILE);
        this.nbt = hasFile ? tag.getCompound(NBT_FILE) : null;
        this.requestId = hasFile ? 0 : tag.getLong(NBT_REQUEST_ID);
        this.pause = tag.getBoolean(NBT_PAUSE);
    }

    public static void handle(SoundPauseResumePacket packet, NetworkPacketContext context) {
        context.queue(() -> {
           EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
               if (packet.nbt != null) {
                   SoundFile file = SoundFile.fromNbt(packet.nbt, context.getPlayer().level());
                   Arrays.stream(ClientInstanceManager.getInstancesOfSound(file)).forEach(x -> ClientSoundManager.setPaused(x, packet.pause));
               } else {
                   ClientSoundManager.setPaused(packet.requestId, packet.pause);
               }
           });
       });
    }
}
