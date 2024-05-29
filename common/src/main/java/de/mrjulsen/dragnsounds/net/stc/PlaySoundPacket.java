package de.mrjulsen.dragnsounds.net.stc;

import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.core.data.PlaybackConfig;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class PlaySoundPacket implements IPacketBase<PlaySoundPacket> {

    private long soundId;
    private int triggerIndex;
    private SoundFile file;
    private PlaybackConfig playback;
    private long clientCallbackRequestId;

    private CompoundTag nbt;

    public PlaySoundPacket() {}

    public PlaySoundPacket(long soundId, int triggerIndex, SoundFile file, PlaybackConfig config, long clientCallbackRequestId) {
        this.soundId = soundId;
        this.triggerIndex = triggerIndex;
        this.file = file;
        this.playback = config;
        this.clientCallbackRequestId = clientCallbackRequestId;
    }

    private PlaySoundPacket(long soundId, int triggerIndex, CompoundTag nbt, PlaybackConfig config, long clientCallbackRequestId) {
        this.soundId = soundId;
        this.triggerIndex = triggerIndex;
        this.nbt = nbt;
        this.playback = config;
        this.clientCallbackRequestId = clientCallbackRequestId;
    }

    @Override
    public void encode(PlaySoundPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.soundId);
        buf.writeInt(packet.triggerIndex);
        buf.writeNbt(packet.file.serializeNbt());
        buf.writeNbt(packet.playback.serializeNbt());
        buf.writeLong(packet.clientCallbackRequestId);
    }

    @Override
    public PlaySoundPacket decode(FriendlyByteBuf buf) {
        return new PlaySoundPacket(
            buf.readLong(),
            buf.readInt(),
            buf.readNbt(),
            PlaybackConfig.deserializeNbt(buf.readNbt()),
            buf.readLong()
        );
    }

    @Override
    public void handle(PlaySoundPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                ClientSoundManager.playSoundQueue(packet.soundId, packet.triggerIndex, SoundFile.fromNbt(packet.nbt, contextSupplier.get().getPlayer().getLevel()), packet.playback, packet.clientCallbackRequestId);
            });
        });
    }
    
}
