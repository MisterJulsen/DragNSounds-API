package de.mrjulsen.dragnsounds.net.stc;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.FriendlyByteBuf;

public class StopAllSoundsPacket implements IPacketBase<StopAllSoundsPacket> {

    public StopAllSoundsPacket() {}

    @Override
    public void encode(StopAllSoundsPacket packet, FriendlyByteBuf buf) {}

    @Override
    public StopAllSoundsPacket decode(FriendlyByteBuf buf) {
        return new StopAllSoundsPacket();
    }

    @Override
    public void handle(StopAllSoundsPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                ClientSoundManager.stopAllSounds();
            });
        });
    }
    
}
