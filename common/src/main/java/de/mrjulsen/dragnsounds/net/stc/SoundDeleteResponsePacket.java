package de.mrjulsen.dragnsounds.net.stc;

import de.mrjulsen.dragnsounds.core.callbacks.client.SoundDeleteCallback;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class SoundDeleteResponsePacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_RESULT = "Result";

    private long requestId;
    private DLStatus result;

    public SoundDeleteResponsePacket(DLStatus status) {
        super(status);
    }

    public SoundDeleteResponsePacket(long requestId, DLStatus result) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.result = result;
    }

    @Override protected void write(CompoundTag tag) {
        tag.putLong(NBT_REQUEST_ID, requestId);
        tag.put(NBT_RESULT, result.toNbt());
    }

    @Override protected void read(CompoundTag tag) {
        this.requestId = tag.getLong(NBT_REQUEST_ID);
        this.result = DLStatus.fromNbt(tag.getCompound(NBT_RESULT));
    }

    public static void handle(SoundDeleteResponsePacket packet, NetworkPacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            SoundDeleteCallback.run(packet.requestId, packet.result);
        });
    }
}
