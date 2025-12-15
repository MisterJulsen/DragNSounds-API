package de.mrjulsen.dragnsounds.net.stc;

import java.util.Optional;

import de.mrjulsen.dragnsounds.core.callbacks.client.SoundFileCallback;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class SoundFileResponsePacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_HAS = "Has";
    private static final String NBT_FILE = "File";

    private long requestId;
    private CompoundTag nbt;

    public SoundFileResponsePacket(DLStatus status) { super(status); }
    public SoundFileResponsePacket(long requestId, SoundFile file) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.nbt = file != null ? file.serializeNbt() : null;
    }

    @Override protected void write(CompoundTag tag) {
        tag.putLong(NBT_REQUEST_ID, requestId);
        tag.putBoolean(NBT_HAS, nbt != null);
        if (nbt != null) tag.put(NBT_FILE, nbt);
    }

    @Override protected void read(CompoundTag tag) {
        this.requestId = tag.getLong(NBT_REQUEST_ID);
        this.nbt = tag.getBoolean(NBT_HAS) ? tag.getCompound(NBT_FILE) : null;
    }

    public static void handle(SoundFileResponsePacket packet, NetworkPacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            Optional<SoundFile> file = packet.nbt == null ? Optional.empty() : Optional.ofNullable(SoundFile.fromNbt(packet.nbt, context.getPlayer().level()));
            SoundFileCallback.run(packet.requestId, file);
        });
    }
}
