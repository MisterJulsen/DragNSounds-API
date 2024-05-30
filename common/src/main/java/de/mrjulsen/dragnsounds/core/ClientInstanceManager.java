package de.mrjulsen.dragnsounds.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.api.Api;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundChannelsHolder;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundDeleteCallback;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundErrorCallback;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundFileCallback;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundListCallback;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundMetadataCallback;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundStreamHolder;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundUploadCallback;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundUploadCancelCallback;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundUploadProgressCallback;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback.ESoundPlaybackStatus;
import de.mrjulsen.dragnsounds.core.data.SoundCommandListener;
import de.mrjulsen.dragnsounds.core.data.SoundDataStream;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.net.cts.SoundCreatedResponsePacket;
import de.mrjulsen.dragnsounds.net.stc.SoundDataPacket;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

public final class ClientInstanceManager {

    // Callbacks
    private static final Map<Long, Consumer<Long>> pendingSoundRequests = new HashMap<>();
    private static final Map<Long, Runnable> runnableCallbacks = new HashMap<>();

    // Queues
    private static final Map<Long, SoundCommandListener> soundCommandListeners = new HashMap<>();

    // 
    private static final Set<Long> blockedSoundIds = new HashSet<>();

    private static int gcCount = 0;


    public static final void receiveSoundData(SoundDataPacket packet) {
        if (blockedSoundIds.contains(packet.getSoundId())) {
            return;
        }
        
        SoundDataStream stream = SoundStreamHolder.getOrCreate(packet.getSoundId(), () -> {
            DragNSounds.LOGGER.debug("Local sound buffer with a size of " + packet.getBufferSize() + " bytes has been created. (ID " + packet.getSoundId() + ")");
            createSoundCommandListener(packet.getSoundId());
            DragNSounds.net().sendToServer(new SoundCreatedResponsePacket(packet.getSoundId(), ESoundPlaybackStatus.PREPARE));
            return new SoundDataStream(packet.getBufferSize(), packet.getSoundId());
        });

        if (stream.shouldReject(packet.getIndex())) {
            DragNSounds.LOGGER.warn(String.format("Sound data packet rejected! (ID %s, Index: %s, Size: %s bytes, IsLast: %s)", packet.getSoundId(), packet.getIndex(), packet.getBufferSize(), !packet.isHasNext()));
            return;
        }
        stream.queue(packet);
    }

    public static long addSoundRequestCallback(Consumer<Long> callback) {
        long requestId = Api.id();
        pendingSoundRequests.put(requestId, callback);
        return requestId;
    }

    public static void runSoundRequestCallback(long requestId, long soundId) {
        if (pendingSoundRequests.containsKey(requestId)) {
            pendingSoundRequests.remove(requestId).accept(soundId);
        } else {
            DragNSounds.LOGGER.error("Unable to run sound request callback (ID " + requestId + ") for custom sound with ID " + soundId);
        }
    }

    public static void addRunnableCallback(long id, Runnable task) {
        runnableCallbacks.put(id, task);
    }

    public static void runRunnableCallback(long id) {
        if (runnableCallbacks.containsKey(id)) {
            runnableCallbacks.remove(id).run();
        } else {
            DragNSounds.LOGGER.error("Unable to run callback with ID " + id);
        }
    }

    public static SoundCommandListener createSoundCommandListener(long soundId) {
        return soundCommandListeners.computeIfAbsent(soundId, x -> new SoundCommandListener(x));
    }

    public static SoundCommandListener getSoundCommandListener(long soundId) {
        return soundCommandListeners.get(soundId);
    }

    public static long[] getInstancesOfSound(SoundFile file) {
        return soundCommandListeners.entrySet().stream().filter(x -> x.getValue().getSoundFile().equals(file)).mapToLong(x -> x.getKey()).toArray();
    }

    public static long[] getAllSoundIds() {
        return soundCommandListeners.keySet().stream().mapToLong(x -> x).toArray();
    }

    public static boolean isAnyInstanceOfSoundPlaying(SoundFile file) {
        return soundCommandListeners.entrySet().stream().anyMatch(x -> x.getValue().getSoundFile().equals(file));
    }

    public static SoundCommandListener removeSoundCommandListener(long soundId) {
        return soundCommandListeners.remove(soundId);
    }

    
    public static boolean isSoundIdBlocked(long soundId) {
        return blockedSoundIds.contains(soundId);
    }

    public static void closeUploadCallbacks(long id) {
        runnableCallbacks.remove(id);
        SoundErrorCallback.close(id);
        SoundUploadProgressCallback.close(id);
        SoundUploadCallback.close(id);
    }

    public static void delayedSoundGC(long soundId, int delay) {
        if (blockedSoundIds.add(soundId)) {
            new Thread(() -> {
                gcCount++;
                try {
                    Thread.sleep(delay);
    
                    SoundChannelsHolder.close(soundId);
                    SoundStreamHolder.close(soundId);
                    runnableCallbacks.remove(soundId);
                    pendingSoundRequests.remove(soundId);
                    blockedSoundIds.remove(soundId);
                    soundCommandListeners.remove(soundId);
                } catch (Exception e) {
                    DragNSounds.LOGGER.warn("Error executing the sound GC.", e);
                } finally {
                    gcCount--;
                }
            }, "Sound GC " + soundId).start();
        }
    }

    public static void clearCallbacks() {
        SoundChannelsHolder.clear();
        SoundDeleteCallback.clear();
        SoundErrorCallback.clear();
        SoundFileCallback.clear();
        SoundListCallback.clear();
        SoundMetadataCallback.clear();
        SoundStreamHolder.clear();
        SoundUploadCallback.clear();
        SoundUploadProgressCallback.clear();
        pendingSoundRequests.clear();
        runnableCallbacks.clear();
        //soundCommandListeners.clear();
    }





    /* DEBUG AREA */

    public static String debugString() {
        return String.format("DS-API[C] B: %s, Ch: %s, L: %s, R: %s, RC: %s, C: [%s,%s,%s,%s,%s,%s,%s,%s], IdL: %s, GC: %s",
            SoundStreamHolder.getCount(),
            SoundChannelsHolder.getCount(),
            soundCommandListeners.size(),
            pendingSoundRequests.size(),
            runnableCallbacks.size(),

            SoundDeleteCallback.getCount(),
            SoundFileCallback.getCount(),
            SoundListCallback.getCount(),
            SoundMetadataCallback.getCount(),
            SoundUploadCallback.getCount(),
            SoundUploadProgressCallback.getCount(),
            SoundErrorCallback.getCount(),
            SoundUploadCancelCallback.getCount(),

            blockedSoundIds.size(),
            gcCount
        );
    }

    

    public static MutableComponent debugComponent() {
        return TextUtils.empty().append(TextUtils.text("DragNSounds API Status (Client):").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD))
            .append("\n").append(TextUtils.text("SoundStreamHolder").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(SoundStreamHolder.getCount())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("SoundChannelsHolder").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(SoundChannelsHolder.getCount())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("SoundCommandListeners").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(soundCommandListeners.size())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("PendingSoundRequests").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(pendingSoundRequests.size())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("RunnableCallbacks").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(runnableCallbacks.size())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("SoundDeleteCallback").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(SoundDeleteCallback.getCount())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("SoundFileCallback").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(SoundFileCallback.getCount())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("SoundListCallback").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(SoundListCallback.getCount())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("SoundMetadataCallback").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(SoundMetadataCallback.getCount())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("SoundUploadCallback").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(SoundUploadCallback.getCount())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("SoundUploadProgressCallback").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(SoundUploadProgressCallback.getCount())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("SoundErrorCallback").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(SoundErrorCallback.getCount())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("SoundUploadCancelCallback").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(SoundUploadCancelCallback.getCount())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("blockedSoundIds").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(blockedSoundIds.size())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("GC Count").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(gcCount)).withStyle(ChatFormatting.RED))
        ;
    }
}
