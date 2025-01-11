package de.mrjulsen.dragnsounds.net.stc;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ClientInstanceManager;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class PrintDebugPacket extends BaseNetworkPacket<PrintDebugPacket> {

    @Override
    public void encode(PrintDebugPacket packet, RegistryFriendlyByteBuf buf) {}

    @Override
    public PrintDebugPacket decode(RegistryFriendlyByteBuf buf) {
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
