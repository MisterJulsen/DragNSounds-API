package de.mrjulsen.dragnsounds.net.stc;

import de.mrjulsen.dragnsounds.api.ClientApi.UploadProgress;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundUploadProgressCallback;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class UploadProgressPacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_PROGRESS = "Progress";

    private long requestId;
    private UploadProgress progress;

    public UploadProgressPacket(DLStatus status) { super(status); }

    public UploadProgressPacket(long requestId, UploadProgress progress) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.progress = progress;
    }

    @Override
    protected void write(CompoundTag tag) {
        tag.putLong(NBT_REQUEST_ID, requestId);
        tag.put(NBT_PROGRESS, progress.toNbt());
    }

    @Override
    protected void read(CompoundTag tag) {
        this.requestId = tag.getLong(NBT_REQUEST_ID);
        this.progress = UploadProgress.fromNbt(tag.getCompound(NBT_PROGRESS));
    }

    public static void handle(UploadProgressPacket packet, NetworkPacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            SoundUploadProgressCallback.run(packet.requestId, packet.progress);
        });
    }
}
