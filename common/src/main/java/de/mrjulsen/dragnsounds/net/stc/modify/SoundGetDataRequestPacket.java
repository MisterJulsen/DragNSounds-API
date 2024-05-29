package de.mrjulsen.dragnsounds.net.stc.modify;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.net.cts.SoundGetDataResponsePacket;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.FriendlyByteBuf;

public class SoundGetDataRequestPacket implements IPacketBase<SoundGetDataRequestPacket> {
    
    private long soundId;

    public SoundGetDataRequestPacket() {}

    public SoundGetDataRequestPacket(long soundId) {
        this.soundId = soundId;
    }

    @Override
    public void encode(SoundGetDataRequestPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.soundId);
    }

    @Override
    public SoundGetDataRequestPacket decode(FriendlyByteBuf buf) {
        return new SoundGetDataRequestPacket(buf.readLong());
    }

    @Override
    public void handle(SoundGetDataRequestPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                DragNSounds.net().sendToServer(new SoundGetDataResponsePacket(packet.soundId, ClientSoundManager.getData(packet.soundId)));
            });
        });
    }

    
}
