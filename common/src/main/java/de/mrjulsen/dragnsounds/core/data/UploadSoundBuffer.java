package de.mrjulsen.dragnsounds.core.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.api.ClientApi.UploadProgress;
import de.mrjulsen.dragnsounds.api.ClientApi.UploadState;
import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.net.cts.FinishUploadSoundPacket;
import de.mrjulsen.dragnsounds.net.cts.UploadSoundPacket;
import de.mrjulsen.dragnsounds.net.stc.UploadFailedPacket;
import de.mrjulsen.dragnsounds.net.stc.UploadProgressPacket;
import de.mrjulsen.dragnsounds.net.stc.UploadSuccessPacket;
import de.mrjulsen.mcdragonlib.data.StatusResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class UploadSoundBuffer implements AutoCloseable {

    private final long requestId;
    private final ServerPlayer player;

    private final Queue<UploadSoundPacket> queue;
    private final ByteArrayOutputStream output;
    private final int maxSize;

    private FinishUploadSoundPacket finalizerPacket;
    private int indexNeeded = 0;
    private boolean hasMore = true;

    private boolean isWorking = true;

    public UploadSoundBuffer(long requestId, int maxSize, ServerPlayer player) {
        this.requestId = requestId;
        this.player = player;
        this.maxSize = maxSize;
        this.output = new ByteArrayOutputStream();
        this.queue = new ConcurrentLinkedQueue<>();

        new Thread(this::queueWorker, "Upload Buffer Listener").start();
    }

    private void queueWorker() {
        while (isWorking) {
            while (!queue.isEmpty() && isWorking) {
                UploadSoundPacket packet = queue.peek();
                if (packet.getIndex() == indexNeeded) {
                    try {
                        output.write(packet.getData());
                        queue.remove(packet);
                        hasMore = packet.hasMore();
                        indexNeeded++;
                        double progress = 100D / maxSize * output.size();
                        DragNSounds.net().sendToPlayer(player, new UploadProgressPacket(requestId, new UploadProgress(progress, UploadState.UPLOAD)));
                    } catch (IOException e) {
                        DragNSounds.LOGGER.error("Error while writing upload file content.", e);
                        DragNSounds.net().sendToPlayer(player, new StatusResult(false, -102, e.getLocalizedMessage()));
                        isWorking = false;
                        break;
                    }
                } else {
                    try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException e) { }
                }
            }

            if (!hasMore) {
                while (finalizerPacket == null) { // Waiting for the finalizer packet to arrive.
                    try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException e) { }
                }
                isWorking = false;
                break;
            }

            try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException e) { }
        }

        if (finalizerPacket != null) {
            try {
                SoundFile file = finalizerPacket.getFile().save(player.getUUID(), output, finalizerPacket.getInitialChannels(), finalizerPacket.getInitialDuration());
                DragNSounds.net().sendToPlayer(player, new UploadSuccessPacket(requestId, file));
            } catch (Throwable e) {
                DragNSounds.LOGGER.error("Unable to save uploaded file.", e);
                DragNSounds.net().sendToPlayer(player, new UploadFailedPacket(requestId, new StatusResult(false, -100, e.getLocalizedMessage())));
            }
        } else {            
            DragNSounds.net().sendToPlayer(player, new UploadFailedPacket(requestId, new StatusResult(false, -101, "Upload canceled unexpectedly.")));
        }

        ServerSoundManager.closeUpload(requestId);
    }

    public long getId() {
        return requestId;
    }

    public Player getPlayer() {
        return player;
    }

    public void queue(UploadSoundPacket packet) {
        queue.add(packet);
    }

    public void setFinalizerPacket(FinishUploadSoundPacket packet) {
        this.finalizerPacket = packet;
    }

    @Override
    public void close() {
        DragNSounds.LOGGER.info("Clean up upload buffer. " + requestId);
        isWorking = false;
        try {
            output.close();
        } catch (IOException e) {
            DragNSounds.LOGGER.error("Unable to close file stream.", e);
        }
        queue.clear();
    }
}
