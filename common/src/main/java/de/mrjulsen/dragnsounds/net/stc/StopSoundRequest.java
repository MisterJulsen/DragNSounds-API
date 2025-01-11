package de.mrjulsen.dragnsounds.net.stc;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class StopSoundRequest extends BaseNetworkPacket<StopSoundRequest> {

    private long soundId;

    public StopSoundRequest() {}

    public StopSoundRequest(long soundId) {
        this.soundId = soundId;
    }

    @Override
    public void encode(StopSoundRequest packet, RegistryFriendlyByteBuf buf) {
        buf.writeLong(packet.soundId);
    }

    @Override
    public StopSoundRequest decode(RegistryFriendlyByteBuf buf) {
        return new StopSoundRequest(
            buf.readLong()
        );
    }

    @Override
    public void handle(StopSoundRequest packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                ClientSoundManager.stopSound(packet.soundId);
            });
        });
    }
    
}
