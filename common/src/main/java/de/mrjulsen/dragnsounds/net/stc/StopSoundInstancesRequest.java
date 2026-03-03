package de.mrjulsen.dragnsounds.net.stc;

import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class StopSoundInstancesRequest extends NetworkPacketData {

    private static final String NBT_FILE = "File";
    private CompoundTag nbt;

    public StopSoundInstancesRequest(DLStatus status) { super(status); }

    public StopSoundInstancesRequest(SoundFile file) {
        super(DLStatus.OK);
        this.nbt = file.serializeNbt();
    }

    @Override
    protected void write(CompoundTag tag) {
        tag.put(NBT_FILE, nbt);
    }

    @Override
    protected void read(CompoundTag tag) {
        this.nbt = tag.getCompound(NBT_FILE);
    }

    public static void handle(StopSoundInstancesRequest packet, NetworkPacketContext context) {
        context.queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                ClientSoundManager.stopAllSoundInstances(SoundFile.fromNbt(packet.nbt, context.getPlayer().level()));
            });
        });
    }
}
