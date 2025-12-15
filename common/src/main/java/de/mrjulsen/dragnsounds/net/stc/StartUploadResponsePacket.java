package de.mrjulsen.dragnsounds.net.stc;

import de.mrjulsen.dragnsounds.core.callbacks.client.SoundStartUploadCallback;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class StartUploadResponsePacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_RESULT = "Result";

    private long requestId;
    private DLStatus status;

    public StartUploadResponsePacket(DLStatus status) {
        super(status);
    }

    public StartUploadResponsePacket(long requestId, DLStatus status) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.status = status;
    }

    @Override
    protected void write(CompoundTag tag) {
        tag.putLong(NBT_REQUEST_ID, requestId);
        tag.put(NBT_RESULT, status.toNbt());
    }

    @Override
    protected void read(CompoundTag tag) {
        this.requestId = tag.getLong(NBT_REQUEST_ID);
        this.status = DLStatus.fromNbt(tag.getCompound(NBT_RESULT));
    }

    public static void handle(StartUploadResponsePacket packet, NetworkPacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            SoundStartUploadCallback.run(packet.requestId, packet.status);
        });
    }
}
