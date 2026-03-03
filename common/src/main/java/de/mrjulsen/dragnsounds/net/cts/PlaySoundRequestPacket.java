package de.mrjulsen.dragnsounds.net.cts;

import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.data.PlaybackConfig;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PlaySoundRequestPacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_FILE = "File";
    private static final String NBT_CONFIG = "Config";

    private long requestId;
    private SoundFile file;
    private PlaybackConfig playback;

    private CompoundTag nbt;

    public PlaySoundRequestPacket(DLStatus status) {
        super(status);
    }

    public PlaySoundRequestPacket(long requestId, SoundFile file, PlaybackConfig playback) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.file = file;
        this.playback = playback;
    }
    
    public PlaySoundRequestPacket(long requestId, CompoundTag nbt, PlaybackConfig playback) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.nbt = nbt;
        this.playback = playback;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putLong(NBT_REQUEST_ID, requestId);
        nbt.put(NBT_FILE, file.serializeNbt());
        nbt.put(NBT_CONFIG, playback.serializeNbt());
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.requestId = nbt.getLong(NBT_REQUEST_ID);
        this.nbt = nbt.getCompound(NBT_FILE);
        this.playback = PlaybackConfig.deserializeNbt(nbt.getCompound(NBT_CONFIG));
    }

    
    public static void handle(PlaySoundRequestPacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            ServerSoundManager.playSound(SoundFile.fromNbt(packet.nbt, context.getPlayer().level()), packet.playback, new ServerPlayer[] { (ServerPlayer)context.getPlayer() }, packet.requestId);
        });
    }
}
