package de.mrjulsen.dragnsounds.net.stc;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ClientInstanceManager;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.FriendlyByteBuf;

public class PrintDebugPacket implements IPacketBase<PrintDebugPacket> {

    @Override
    public void encode(PrintDebugPacket packet, FriendlyByteBuf buf) {}

    @Override
    public PrintDebugPacket decode(FriendlyByteBuf buf) {
        return new PrintDebugPacket();
    }

    @Override
    public void handle(PrintDebugPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                contextSupplier.get().getPlayer().sendSystemMessage(ClientInstanceManager.debugComponent());
            });
        });
    }
    
}
