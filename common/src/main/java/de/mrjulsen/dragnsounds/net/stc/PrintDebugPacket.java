package de.mrjulsen.dragnsounds.net.stc;

import de.mrjulsen.dragnsounds.core.ClientInstanceManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class PrintDebugPacket extends NetworkPacketData {

    public PrintDebugPacket() { super(DLStatus.OK); }
    public PrintDebugPacket(DLStatus status) { super(status); }

    @Override protected void write(CompoundTag tag) {}
    @Override protected void read(CompoundTag tag) {}

    public static void handle(PrintDebugPacket packet, NetworkPacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            context.getPlayer().sendSystemMessage(ClientInstanceManager.debugComponent());
        });
    }
}
