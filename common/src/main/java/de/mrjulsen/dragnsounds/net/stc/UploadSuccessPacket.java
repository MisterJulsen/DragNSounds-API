package de.mrjulsen.dragnsounds.net.stc;

import java.util.Optional;

import de.mrjulsen.dragnsounds.core.ClientInstanceManager;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundUploadCallback;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class UploadSuccessPacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_FILE = "File";

    private long requestId;
    private CompoundTag nbt;

    public UploadSuccessPacket(DLStatus status) { super(status); }

    public UploadSuccessPacket(long requestId, SoundFile file) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.nbt = file != null ? file.serializeNbt() : null;
    }

    @Override
    protected void write(CompoundTag tag) {
        tag.putLong(NBT_REQUEST_ID, requestId);
        if (nbt != null) tag.put(NBT_FILE, nbt);
    }

    @Override
    protected void read(CompoundTag tag) {
        this.requestId = tag.getLong(NBT_REQUEST_ID);
        this.nbt = tag.contains(NBT_FILE) ? tag.getCompound(NBT_FILE) : null;
    }

    public static void handle(UploadSuccessPacket packet, NetworkPacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            SoundUploadCallback.run(packet.requestId, Optional.ofNullable(packet.nbt == null ? null : SoundFile.fromNbt(packet.nbt, context.getPlayer().level())));
            ClientInstanceManager.closeUploadCallbacks(packet.requestId);
        });
    }
}
