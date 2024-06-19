package de.mrjulsen.dragnsounds.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.data.PlaybackConfig;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PlaySoundRequestPacket implements IPacketBase<PlaySoundRequestPacket> {

    private long requestId;
    private SoundFile file;
    private PlaybackConfig playback;

    private CompoundTag nbt;

    public PlaySoundRequestPacket() {}

    public PlaySoundRequestPacket(long requestId, SoundFile file, PlaybackConfig playback) {
        this.requestId = requestId;
        this.file = file;
        this.playback = playback;
    }
    
    public PlaySoundRequestPacket(long requestId, CompoundTag nbt, PlaybackConfig playback) {
        this.requestId = requestId;
        this.nbt = nbt;
        this.playback = playback;
    }

    @Override
    public void encode(PlaySoundRequestPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeNbt(packet.file.serializeNbt());
        buf.writeNbt(packet.playback.serializeNbt());
    }

    @Override
    public PlaySoundRequestPacket decode(FriendlyByteBuf buf) {
        return new PlaySoundRequestPacket(
            buf.readLong(),
            buf.readNbt(),
            PlaybackConfig.deserializeNbt(buf.readNbt())
        );
    }

    @Override
    public void handle(PlaySoundRequestPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerSoundManager.playSound(SoundFile.fromNbt(packet.nbt, contextSupplier.get().getPlayer().level()), packet.playback, new ServerPlayer[] { (ServerPlayer)contextSupplier.get().getPlayer() }, packet.requestId);
        });
    }
}
