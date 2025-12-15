package de.mrjulsen.dragnsounds.net.cts;

import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback.ESoundPlaybackStatus;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class StopSoundNotificationPacket extends NetworkPacketData {

    private static final String NBT_SOUND_ID = "SoundId";

    private long soundId;

    public StopSoundNotificationPacket(DLStatus status) {
        super(status);
    }

    public StopSoundNotificationPacket(long soundId) {
        super(DLStatus.OK);
        this.soundId = soundId;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putLong(NBT_SOUND_ID, soundId);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.soundId = nbt.getLong(NBT_SOUND_ID);
    }

    public static void handle(StopSoundNotificationPacket packet, NetworkPacketContext context) {
        SoundPlayingCallback.runAndClose(packet.soundId, context.getPlayer(), ESoundPlaybackStatus.STOP);
        ServerSoundManager.stopSound((ServerPlayer) context.getPlayer(), packet.soundId);
    }
}
