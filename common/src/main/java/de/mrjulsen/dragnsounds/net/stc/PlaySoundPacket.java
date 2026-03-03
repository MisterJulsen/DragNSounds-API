package de.mrjulsen.dragnsounds.net.stc;

import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.core.data.PlaybackConfig;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class PlaySoundPacket extends NetworkPacketData {

    private static final String NBT_SOUND_ID = "SoundId";
    private static final String NBT_TRIGGER = "Trigger";
    private static final String NBT_FILE = "File";
    private static final String NBT_CONFIG = "Config";
    private static final String NBT_CLIENT_REQ = "ClientReq";

    private long soundId;
    private int triggerIndex;
    private CompoundTag nbt;
    private PlaybackConfig playback;
    private long clientCallbackRequestId;

    public PlaySoundPacket(DLStatus status) { super(status); }

    public PlaySoundPacket(long soundId, int triggerIndex, SoundFile file, PlaybackConfig config, long clientCallbackRequestId) {
        super(DLStatus.OK);
        this.soundId = soundId;
        this.triggerIndex = triggerIndex;
        this.nbt = file.serializeNbt();
        this.playback = config;
        this.clientCallbackRequestId = clientCallbackRequestId;
    }

    @Override protected void write(CompoundTag tag) {
        tag.putLong(NBT_SOUND_ID, soundId);
        tag.putInt(NBT_TRIGGER, triggerIndex);
        tag.put(NBT_FILE, nbt);
        tag.put(NBT_CONFIG, playback.serializeNbt());
        tag.putLong(NBT_CLIENT_REQ, clientCallbackRequestId);
    }

    @Override protected void read(CompoundTag tag) {
        this.soundId = tag.getLong(NBT_SOUND_ID);
        this.triggerIndex = tag.getInt(NBT_TRIGGER);
        this.nbt = tag.getCompound(NBT_FILE);
        this.playback = PlaybackConfig.deserializeNbt(tag.getCompound(NBT_CONFIG));
        this.clientCallbackRequestId = tag.getLong(NBT_CLIENT_REQ);
    }

    public static void handle(PlaySoundPacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                ClientSoundManager.playSoundQueue(packet.soundId, packet.triggerIndex, SoundFile.fromNbt(packet.nbt, context.getPlayer().level()), packet.playback, packet.clientCallbackRequestId);
            });
        });
    }
}
