package de.mrjulsen.dragnsounds.net.cts;

import de.mrjulsen.dragnsounds.core.ServerInstanceManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class FinishUploadSoundPacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_MAX_SIZE = "MaxSize";
    private static final String NBT_FILE = "File";
    private static final String NBT_INITIAL_DURATION = "InitialDuration";
    private static final String NBT_INITIAL_CHANNELS = "InitialChannels";

    private long requestId;
    private int maxSize;
    private SoundFile.Builder file;
    private long initialDuration;
    private int initialChannels;

    private CompoundTag nbt;
    private Level level;

    public FinishUploadSoundPacket(DLStatus status) {
        super(status);
    }

    public FinishUploadSoundPacket(long requestId, int maxSize, SoundFile.Builder file, int initialChannels, long initialDuration) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.maxSize = maxSize;
        this.file = file;
        this.initialChannels = initialChannels;
    }

    private FinishUploadSoundPacket(long requestId, int maxSize, CompoundTag nbt, int initialChannels, long initialDuration) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.maxSize = maxSize;
        this.nbt = nbt;
        this.initialChannels = initialChannels;
    }
    

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putLong(NBT_REQUEST_ID, requestId);
        nbt.putInt(NBT_MAX_SIZE, maxSize);
        nbt.put(NBT_FILE, file.serializeNbt());
        nbt.putLong(NBT_INITIAL_DURATION, initialDuration);
        nbt.putInt(NBT_INITIAL_CHANNELS, initialChannels);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.requestId = nbt.getLong(NBT_REQUEST_ID);
        this.maxSize = nbt.getInt(NBT_MAX_SIZE);
        this.nbt = nbt.getCompound(NBT_FILE);
        this.initialChannels = nbt.getInt(NBT_INITIAL_CHANNELS);
        this.initialDuration = nbt.getLong(NBT_INITIAL_DURATION);
    }   

    
    public static void handle(FinishUploadSoundPacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            packet.level = context.getPlayer().level();
            ServerInstanceManager.getUploadBuffer(packet.requestId, packet.maxSize, (ServerPlayer)context.getPlayer()).setFinalizerPacket(packet);
        });
    }

    public long getRequestId() {
        return requestId;
    }

    public long getInitialDuration() {
        return initialDuration;
    }
    
    public int getInitialChannels() {
        return initialChannels;
    }

    public SoundFile.Builder getFile() {
        return SoundFile.Builder.fromNbt(nbt, level);
    }
}
