package de.mrjulsen.dragnsounds.net.stc;

import de.mrjulsen.dragnsounds.core.ClientInstanceManager;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundErrorCallback;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class UploadFailedPacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_RESULT = "Result";

    private long requestId;
    private DLStatus error;

    public UploadFailedPacket(DLStatus status) { super(status); }

    public UploadFailedPacket(long requestId, DLStatus error) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.error = error;
    }

    @Override
    protected void write(CompoundTag tag) {
        tag.putLong(NBT_REQUEST_ID, requestId);
        tag.put(NBT_RESULT, error.toNbt());
    }

    @Override
    protected void read(CompoundTag tag) {
        this.requestId = tag.getLong(NBT_REQUEST_ID);
        this.error = DLStatus.fromNbt(tag.getCompound(NBT_RESULT));
    }

    public static void handle(UploadFailedPacket packet, NetworkPacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            SoundErrorCallback.run(packet.requestId, packet.error);
            ClientInstanceManager.closeUploadCallbacks(packet.requestId);
        });
    }
}
