package de.mrjulsen.dragnsounds.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundGetDataCallback;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCheckCallback;
import de.mrjulsen.dragnsounds.core.data.PlayerboundDataBuffer;
import de.mrjulsen.dragnsounds.core.data.UploadSoundBuffer;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.util.IOUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class ServerInstanceManager {
    private static final Map<String, PlayerboundDataBuffer> activeBuffers = new HashMap<>();
    private static final Map<Long, PlayerboundDataBuffer> activeBuffersBySoundId = new HashMap<>();

    private static final Map<Long, UploadSoundBuffer> uploadFileCache = new HashMap<>();
    private static final Set<Long> uploadIdLocker = new HashSet<>();

    public static PlayerboundDataBuffer loadFile(SoundFile file, long soundId) {
        String hash = IOUtils.getFileHash(file.getPath().get().toString());
        PlayerboundDataBuffer buffer = activeBuffers.computeIfAbsent(hash, x -> {
            try {
                return new PlayerboundDataBuffer(new FileInputStream(file.getPath().get().toFile()));
            } catch (IOException e) {
                DragNSounds.LOGGER.error("Unable to open sound file.", e);
                return null;
            }
        });

        if (buffer == null) {
            DragNSounds.LOGGER.error("Sound file could not be loaded! " + file.toString());
            return null;
        }

        activeBuffersBySoundId.computeIfAbsent(soundId, x -> buffer);

        return buffer;
    }

    public static PlayerboundDataBuffer getBySoundId(long soundId) {
        return activeBuffersBySoundId.get(soundId);
    }

    public static void closeSound(Player player, long soundId) {
        List<PlayerboundDataBuffer> idsToRemove = new LinkedList<>();
        activeBuffersBySoundId.entrySet().removeIf(x -> {
            boolean b = x.getKey() == soundId && x.getValue().remove(player.getUUID(), soundId, PlayerboundDataBuffer.NO_SOUND_WITH_ID).result();
            idsToRemove.add(x.getValue());
            return b;
        });
        removeBufferIfEmpty(idsToRemove);
    }

    public static void closeAll(Player player) {
        List<PlayerboundDataBuffer> idsToRemove = new LinkedList<>();
        activeBuffersBySoundId.entrySet().removeIf(x -> {
            boolean b = x.getValue().remove(player.getUUID());
            idsToRemove.add(x.getValue());
            return b;
        });
        removeBufferIfEmpty(idsToRemove);        
        uploadFileCache.entrySet().removeIf(e -> {
            return e.getValue().getPlayer().getUUID().equals(player.getUUID());
        });
    }

    private static void removeBufferIfEmpty(List<PlayerboundDataBuffer> list) {
        activeBuffers.entrySet().removeIf(x -> {
            boolean b = list.contains(x.getValue()) && !x.getValue().hasListeners();
            if (b) {
                x.getValue().close();
            }
            return b;
        });
    }

    public static synchronized UploadSoundBuffer getOrCreateUploadBuffer(long requestId, int maxSize, ServerPlayer player) {
        return uploadFileCache.computeIfAbsent(requestId, x -> new UploadSoundBuffer(x, maxSize, player));
    }

    public static void closeUploadBuffer(long requestId) {
        if (uploadFileCache.containsKey(requestId)) {
            uploadIdLocker.add(requestId);
            uploadFileCache.remove(requestId).close();
            
            new Thread(() -> {
                try { TimeUnit.MINUTES.sleep(1); } catch (InterruptedException e) { }

                if (uploadFileCache.containsKey(requestId)) {
                    uploadFileCache.remove(requestId).close();
                }
                uploadIdLocker.remove(requestId);
            }, "Upload GC").start();
        }
    }

    

    /* DEBUG AREA */

    public static String debugString() {
        return String.format("DS-API[S] F: %s+%s, U: %s, IdL: %s, C: [%s,%s,%s]",
            activeBuffers.size(),
            activeBuffersBySoundId.size(),
            uploadFileCache.size(),
            uploadIdLocker.size(),
            SoundPlayingCallback.getCount(),
            SoundPlayingCheckCallback.getCount(),
            SoundGetDataCallback.getCount()
        );
    }

    public static MutableComponent debugComponent() {
        return TextUtils.empty().append(TextUtils.text("DragNSounds API Status (Server):").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD))
            .append("\n").append(TextUtils.text("ActiveFileStreams").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(activeBuffers.size())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("ActiveFileStreams (by id)").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(activeBuffersBySoundId.size())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("UploadFileCache").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(uploadFileCache.size())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("UploadIdLocker").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(uploadIdLocker.size())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("SoundPlayingCallback").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(SoundPlayingCallback.getCount())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("SoundPlayingCheckCallback").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(SoundPlayingCheckCallback.getCount())).withStyle(ChatFormatting.RED))
            .append("\n").append(TextUtils.text("SoundGetDataCallback").append(": ").withStyle(ChatFormatting.YELLOW)).append(TextUtils.text(String.valueOf(SoundGetDataCallback.getCount())).withStyle(ChatFormatting.RED))
        ;
    }

    public static void clearCallbacks() {
        SoundPlayingCallback.clear();
        SoundPlayingCheckCallback.clear();
        SoundGetDataCallback.clear();
        activeBuffers.clear();
        activeBuffersBySoundId.clear();
        uploadFileCache.clear();
    }
}
