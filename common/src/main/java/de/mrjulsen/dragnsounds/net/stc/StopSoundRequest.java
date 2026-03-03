package de.mrjulsen.dragnsounds.net.stc;

import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class StopSoundRequest extends NetworkPacketData {

    private static final String NBT_SOUND_ID = "SoundId";
    private long soundId;

    public StopSoundRequest(DLStatus status) { super(status); }

    public StopSoundRequest(long soundId) {
        super(DLStatus.OK);
        this.soundId = soundId;
    }

    @Override
    protected void write(CompoundTag tag) {
        tag.putLong(NBT_SOUND_ID, soundId);
    }

    @Override
    protected void read(CompoundTag tag) {
        this.soundId = tag.getLong(NBT_SOUND_ID);
    }

    public static void handle(StopSoundRequest packet, NetworkPacketContext context) {
        context.queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                ClientSoundManager.stopSound(packet.soundId);
            });
        });
    }
}
