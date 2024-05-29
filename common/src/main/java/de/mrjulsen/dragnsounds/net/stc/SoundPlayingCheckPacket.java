package de.mrjulsen.dragnsounds.net.stc;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundChannelsHolder;
import de.mrjulsen.dragnsounds.net.cts.SoundPlayingCheckResponsePacket;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.FriendlyByteBuf;

public class SoundPlayingCheckPacket implements IPacketBase<SoundPlayingCheckPacket> {

    private long requestId;
    private long soundId;

    public SoundPlayingCheckPacket() {}

    public SoundPlayingCheckPacket(long requestId, long soundId) {
        this.requestId = requestId;
        this.soundId = soundId;
    }

    @Override
    public void encode(SoundPlayingCheckPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeLong(packet.soundId);
    }

    @Override
    public SoundPlayingCheckPacket decode(FriendlyByteBuf buf) {
        return new SoundPlayingCheckPacket(
            buf.readLong(), 
            buf.readLong()
        );
    }

    @Override
    public void handle(SoundPlayingCheckPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                boolean isPlaying = SoundChannelsHolder.has(packet.soundId);
                DragNSounds.net().sendToServer(new SoundPlayingCheckResponsePacket(packet.requestId, isPlaying));
            });
        });
    }
    
}
