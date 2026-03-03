package de.mrjulsen.dragnsounds.net.stc;

import de.mrjulsen.dragnsounds.core.callbacks.client.SoundChannelsHolder;
import de.mrjulsen.dragnsounds.net.cts.SoundPlayingCheckResponsePacket;
import de.mrjulsen.dragnsounds.registry.ModNetworkManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class SoundPlayingCheckPacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_SOUND_ID = "SoundId";

    private long requestId;
    private long soundId;

    public SoundPlayingCheckPacket(DLStatus status) { super(status); }
    public SoundPlayingCheckPacket(long requestId, long soundId) { super(DLStatus.OK); this.requestId = requestId; this.soundId = soundId; }

    @Override protected void write(CompoundTag tag) { tag.putLong(NBT_REQUEST_ID, requestId); tag.putLong(NBT_SOUND_ID, soundId); }
    @Override protected void read(CompoundTag tag) { this.requestId = tag.getLong(NBT_REQUEST_ID); this.soundId = tag.getLong(NBT_SOUND_ID); }

    public static void handle(SoundPlayingCheckPacket packet, NetworkPacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            boolean isPlaying = SoundChannelsHolder.has(packet.soundId);
            ModNetworkManager.SOUND_PLAYING_CHECK_RESPONSE.send(NetworkDirection.toServer(), new SoundPlayingCheckResponsePacket(packet.requestId, isPlaying));
        });
    }
}
