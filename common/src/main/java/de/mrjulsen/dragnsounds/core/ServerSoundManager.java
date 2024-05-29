package de.mrjulsen.dragnsounds.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.api.Api;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundGetDataCallback;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundGetDataCallback.ISoundPlaybackData;
import de.mrjulsen.dragnsounds.core.data.PlaybackConfig;
import de.mrjulsen.dragnsounds.core.data.PlayerboundDataBuffer;
import de.mrjulsen.dragnsounds.core.data.filter.IFilter;
import de.mrjulsen.dragnsounds.core.filesystem.IndexFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.net.cts.UploadSoundPacket;
import de.mrjulsen.dragnsounds.net.stc.PlaySoundPacket;
import de.mrjulsen.dragnsounds.net.stc.SoundDataPacket;
import de.mrjulsen.dragnsounds.net.stc.SoundListChunkResponsePacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundGetDataRequestPacket;
import de.mrjulsen.dragnsounds.util.SoundUtils;
import de.mrjulsen.mcdragonlib.data.StatusResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ServerSoundManager {

    public static final int BUFFER_BLOCK_SIZE = 8;

    public static long playSound(SoundFile file, PlaybackConfig playback, ServerPlayer[] players, long clientCallbackRequestId) {
        final long soundId = (int)Api.id();
        new Thread(() -> {
            // Open File
            PlayerboundDataBuffer buffer = ServerInstanceManager.loadFile(file, soundId);
            Arrays.stream(players).forEach(x -> buffer.register(x.getUUID(), soundId));

            final int INITIAL_SIZE = DragNSounds.DEFAULT_NET_DATA_SIZE * (BUFFER_BLOCK_SIZE + 1) * 2;
            for (ServerPlayer player : players) {
                boolean hasNext = true;
                int i = 0;
                while (i < BUFFER_BLOCK_SIZE && hasNext) {
                    byte[] data = new byte[buffer.maxSize(player.getUUID(), soundId, DragNSounds.DEFAULT_NET_DATA_SIZE * 2)];
                    hasNext = buffer.read(player.getUUID(), soundId, data);
                    DragNSounds.net().sendToPlayer(player, new SoundDataPacket(soundId, i, INITIAL_SIZE, hasNext, data));
                    i++;
                }

                DragNSounds.net().sendToPlayer(player, new PlaySoundPacket(soundId, i - 1, file, playback, clientCallbackRequestId));
            }

        }, "Sound Player Worker").start();
        return soundId;
    }

    public static synchronized void sendSoundData(Player player, long soundId, int size, int index) {
        PlayerboundDataBuffer buffer = ServerInstanceManager.getBySoundId(soundId);
        byte[] data = new byte[buffer == null ? 0 : buffer.maxSize(player.getUUID(), soundId, size)];
        boolean hasNext = buffer == null ? false : buffer.read(player.getUUID(), soundId, data);
        if (!hasNext) {
            ServerInstanceManager.closeSound(player, soundId);
        }
        DragNSounds.net().sendToPlayer((ServerPlayer)player, new SoundDataPacket(soundId, index, -1, hasNext, data));
    }

    public static void closeOnDisconnect(Player player) {
        ServerInstanceManager.closeAll(player);
        DragNSounds.LOGGER.info("Sound listener removed for " + player + ". Disconnected.");
    }

    public static void getSoundPlaybackData(long soundId, ServerPlayer[] players, ISoundPlaybackData callback) {
        SoundGetDataCallback.create(soundId, callback);
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new SoundGetDataRequestPacket(soundId));
    }

    public static void stopSound(Player player, long soundId) {
        ServerInstanceManager.closeSound(player, soundId);
    }

    public static void receiveUploadPacket(ServerPlayer player, UploadSoundPacket packet) {
        ServerInstanceManager.getOrCreateUploadBuffer(packet.getRequestId(), packet.getMaxSize(), player).queue(packet);
    }

    public static void closeUpload(long requestId) {
        ServerInstanceManager.closeUploadBuffer(requestId);
    }

    public static void sendFileListToPlayer(Player player, long requestId, Level level, IFilter<SoundFile>[] filters) {
        new Thread(() -> {
            final int filesPerPacket = 8;
            SoundFile[] files;
            try {
                files = getSoundFileList(level, filters);
            } catch (IOException e) {
                DragNSounds.LOGGER.error("Unable to get sound file list.", e);
                DragNSounds.net().sendToPlayer((ServerPlayer)player, new SoundListChunkResponsePacket(requestId, false, new SoundFile[0]));
                return;
            }

            for (int i = 0; i < files.length; i += filesPerPacket) {
                SoundFile[] chunk = new SoundFile[Math.min(filesPerPacket, files.length - i)];
                System.arraycopy(files, i, chunk, 0, chunk.length);
                
                boolean hasMore = i + filesPerPacket < files.length;
                DragNSounds.net().sendToPlayer((ServerPlayer)player, new SoundListChunkResponsePacket(requestId, hasMore, chunk));
            }
        }, "Sound List Loader").start();
    }

    public static SoundFile[] getSoundFileList(Level level, IFilter<SoundFile>[] filters) throws IOException {
        return getAllSoundFiles(level).stream().filter(x -> filters.length <= 0 || Arrays.stream(filters).allMatch(y -> y == null || y.isValid(x))).toArray(SoundFile[]::new);
    }

    public static SoundFile getSoundFile(SoundLocation location, UUID id) throws IOException {
        try (IndexFile index = IndexFile.open(location, true)) {
            return index.getSoundFile(id);
        }
    }

    public static StatusResult deleteSound(SoundLocation loc, UUID id) throws IOException {
        try (IndexFile index = IndexFile.open(loc, false)) {
            if (!index.has(id)) {
                return new StatusResult(false, -1, "File not found.");
            }
            boolean success = index.delete(id);            
            return success ? new StatusResult(true, 0, "Success") : new StatusResult(false, -2, "Failed to delete sound file.");
        }
    }

    public static Map<String, String> getAllSoundFileMetadata(SoundLocation loc, UUID id) {
        Optional<SoundFile> fileObj = SoundFile.of(loc, id);
        if (fileObj.isPresent() && fileObj.get().getAsFile().isPresent() && fileObj.get().getAsFile().get().exists()) {
            return SoundUtils.getAudioMetadata(fileObj.get().getAsFile().get());
        }
        return Map.of();
    }

    public static Collection<SoundFile> getAllSoundFiles(Level level) {
        return getAllSoundFilesInternal(level, SoundLocation.getModDirectory(level));
    }

    public static Collection<SoundFile> getAllSoundFilesIn(SoundLocation location) {
        return getAllSoundFilesInternal(location.getLevel(), location.resolve().get());
    }

    private static Collection<SoundFile> getAllSoundFilesInternal(Level level, Path startPath) {
        Collection<SoundFile> files = new LinkedList<>();
        File root = startPath.toFile();
        File[] list = root.listFiles();

        if (list == null) {
            return files;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                try (IndexFile file = IndexFile.open(new SoundLocation(level, f.toPath()), true)) {
                    files.addAll(Arrays.stream(file.getAll()).toList());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                files.addAll(getAllSoundFilesInternal(level, f.toPath()));
            }
        }
        return files;
    }

    
    public static Collection<SoundLocation> getAllUsedLocations(Level level) {
        return getAllUsedLocationsInternal(level, SoundLocation.getModDirectory(level));
    }

    public static Collection<SoundLocation> getAllUsedLocationsIn(SoundLocation location) {
        return getAllUsedLocationsInternal(location.getLevel(), location.resolve().get());
    }

    private static Collection<SoundLocation> getAllUsedLocationsInternal(Level level, Path startPath) {
        Collection<SoundLocation> files = new LinkedList<>();
        File root = startPath.toFile();
        File[] list = root.listFiles();

        if (list == null) {
            return files;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                SoundLocation loc = new SoundLocation(level, f.toPath());
                if (IndexFile.existsIn(loc)) {
                    files.add(loc);
                }
                files.addAll(getAllUsedLocationsInternal(level, f.toPath()));
            }
        }
        return files;
    }

    public static void cleanUp(Level level, boolean async) {
        Thread t = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            DragNSounds.LOGGER.info("Sound file cleanup started.");
            cleanUpInternal(level, SoundLocation.getModDirectory(level));
            long usedTime = System.currentTimeMillis() - startTime;
            DragNSounds.LOGGER.info(String.format("Sound file cleanup finished. Took %sms.", usedTime));
        }, "Sound File Cleaner");

        if (async) {
            t.start();
        } else {
            t.run();
        }
    }


    private static final boolean cleanUpInternal(Level level, Path startPath) {

        boolean hasFiles = false;
        File root = startPath.toFile();
        File[] list = root.listFiles();

        if (list == null) {
            return false;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                SoundLocation loc = new SoundLocation(level, f.toPath());

                if (IndexFile.existsIn(loc)) {
                    try (IndexFile index = IndexFile.open(loc, false)) {
                        for (File file : f.listFiles()) {
                            if (file.isDirectory()) {
                                continue;
                            }
                            
                            if (!index.has(file)) {
                                file.delete();
                            }
                        }
                        hasFiles = index.count() > 0;
                    } catch (IOException e) {
                        DragNSounds.LOGGER.warn("Unable to cleanup directory '" + f + "'.", e);
                    }
                }
                
                hasFiles = cleanUpInternal(level, f.toPath()) || hasFiles;

                if (!hasFiles) {
                    deleteDirectoryWithContent(f);
                }
            }
        }
        return hasFiles;
    }

    private static void deleteDirectoryWithContent(File directory) {
        for (File f : directory.listFiles()) {
            if (f.isDirectory()) {
                deleteDirectoryWithContent(f);
            } else {
                f.delete();
            }
        }
        directory.delete();
    }
}
