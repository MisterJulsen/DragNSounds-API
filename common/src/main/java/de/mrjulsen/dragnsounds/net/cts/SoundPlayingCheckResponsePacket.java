package de.mrjulsen.dragnsounds.net.cts;

import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCheckCallback;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;

public class SoundPlayingCheckResponsePacket extends NetworkPacketData {

    private static final String NBT_REQUEST_ID = "RequestId";
    private static final String NBT_VALUE = "Value";

    private long requestId;
    private boolean value;

    public SoundPlayingCheckResponsePacket(DLStatus status) {
        super(status);
    }

    public SoundPlayingCheckResponsePacket(long requestId, boolean value) {
        super(DLStatus.OK);
        this.requestId = requestId;
        this.value = value;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putLong(NBT_REQUEST_ID, requestId);
        nbt.putBoolean(NBT_VALUE, value);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.requestId = nbt.getLong(NBT_REQUEST_ID);
        this.value = nbt.getBoolean(NBT_VALUE);
    }

    public static void handle(SoundPlayingCheckResponsePacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            SoundPlayingCheckCallback.run(packet.requestId, packet.value);
        });
    }
}
