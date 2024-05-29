package de.mrjulsen.dragnsounds.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback.ESoundPlaybackStatus;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

public class StopSoundNotificationPacket implements IPacketBase<StopSoundNotificationPacket> {

    private long soundId;

    public StopSoundNotificationPacket() {}

    public StopSoundNotificationPacket(long soundId) {
        this.soundId = soundId;
    }

    @Override
    public void encode(StopSoundNotificationPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.soundId);
    }

    @Override
    public StopSoundNotificationPacket decode(FriendlyByteBuf buf) {
        return new StopSoundNotificationPacket(buf.readLong());
    }

    @Override
    public void handle(StopSoundNotificationPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            SoundPlayingCallback.runAndClose(packet.soundId, contextSupplier.get().getPlayer(), ESoundPlaybackStatus.STOP);
            ServerSoundManager.stopSound(contextSupplier.get().getPlayer(), packet.soundId);
        });
    }
    
}
