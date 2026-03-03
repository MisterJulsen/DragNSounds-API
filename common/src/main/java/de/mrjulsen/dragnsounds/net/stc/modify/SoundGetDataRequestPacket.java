package de.mrjulsen.dragnsounds.net.stc.modify;

import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.net.cts.SoundGetDataResponsePacket;
import de.mrjulsen.dragnsounds.registry.ModNetworkManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class SoundGetDataRequestPacket extends NetworkPacketData {

    private static final String NBT_SOUND_ID = "SoundId";
    private long soundId;

    public SoundGetDataRequestPacket(DLStatus status) { super(status); }
    public SoundGetDataRequestPacket(long soundId) { super(DLStatus.OK); this.soundId = soundId; }

    @Override protected void write(CompoundTag tag) { tag.putLong(NBT_SOUND_ID, soundId); }
    @Override protected void read(CompoundTag tag) { this.soundId = tag.getLong(NBT_SOUND_ID); }

    public static void handle(SoundGetDataRequestPacket packet, NetworkPacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            ModNetworkManager.SOUND_GET_DATA_RESPONSE.send(NetworkDirection.toServer(), new SoundGetDataResponsePacket(packet.soundId, ClientSoundManager.getData(packet.soundId)));
        });
    }
}
