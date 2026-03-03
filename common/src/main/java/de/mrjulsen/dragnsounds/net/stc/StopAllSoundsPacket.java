package de.mrjulsen.dragnsounds.net.stc;

import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;

public class StopAllSoundsPacket extends NetworkPacketData {

    public StopAllSoundsPacket() { super(DLStatus.OK); }
    public StopAllSoundsPacket(de.mrjulsen.mcdragonlib.data.DLStatus status) { super(status); }

    @Override protected void write(net.minecraft.nbt.CompoundTag tag) {}
    @Override protected void read(net.minecraft.nbt.CompoundTag tag) {}

    public static void handle(StopAllSoundsPacket packet, NetworkPacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            ClientSoundManager.stopAllSounds();
        });
    }
}
