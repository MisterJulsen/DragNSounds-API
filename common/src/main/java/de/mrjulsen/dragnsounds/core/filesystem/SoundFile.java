package de.mrjulsen.dragnsounds.core.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import java.util.Arrays;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ffmpeg.EChannels;
import de.mrjulsen.dragnsounds.net.cts.RemoveMetadataPacket;
import de.mrjulsen.dragnsounds.net.cts.UpdateMetadataPacket;
import de.mrjulsen.dragnsounds.util.ExtendedNBTUtils;
import de.mrjulsen.mcdragonlib.util.IOUtils;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import ws.schild.jave.EncoderException;
import ws.schild.jave.InputFormatException;

/**
 * This object contains information about one specific sound file on the server. While most of the values are read-only, addons can use the metadata to store additional information (such as visibility to other players, etc.)
 */
public class SoundFile {

    /** Default metadata key to get the display name of the file which is stored in the metadata. (because it is not read-only) */
    public static final String META_DISPLAY_NAME = "DisplayName";
    public static final String[] META_DEFAULT_KEYS = {META_DISPLAY_NAME};

    public static final String DEFAULT_AUDIO_FILE_EXTENSION = "ogg";

    private static final String NBT_LOCATION = "Location";
    private static final String NBT_ID = "Id";
    private static final String NBT_HASH = "Hash";
    private static final String NBT_INFO = "Info";
    private static final String NBT_METADATA = "Metadata";

    // FileSystem
    private SoundLocation location = SoundLocation.empty();
    private UUID id = DragNSounds.ZERO_UUID;
    private String hash = "";

    // Info
    private SoundFileInfo info = SoundFileInfo.empty();
    protected Map<String, String> metadata = new HashMap<>();

    private SoundFile() {
    }

    /**
     * Additional data about this file. Addons can use this to store their own info about sound files.
     * @return A copy of the metadata of this file. (Not the actual object to prevent changes)
     * @related {@code updateMetadata(...)} - Add or replace metadata entries.
     * @related {@code removeMetadata(...)} - Remove metadata entries.
     */
    public Map<String, String> getMetadata() {
        return new HashMap<>(metadata);
    }

    /**
     * Update the metadata of this file. The data will be send to the server and applied there.
     * @param meta The collection of the new keys and values. This is NOT the complete {@code Map} containing ALL metadata entries, but only a collection of the changes. New keys will be added and existing ones will be replaced.
     * @related {@code removeMetadata(...)} - Remove metadata entries.
     */
    public void updateMetadata(Map<String, String> meta) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            DragNSounds.net().sendToServer(new UpdateMetadataPacket(id, location, meta));
        });
        EnvExecutor.runInEnv(Env.SERVER, () -> () -> {
            try {
                updateMetadataInternal(location, id, meta);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Update the metadata of this file. The data will be send to the server and applied there.
     * @param keys A collection of metadata keys that should be removed from the list.
     * @apiNote You CANNOT remove any of the built-in metadata entries, such as {@code META_DISPLAY_NAME}, as they are changeable, but not optional. You can add these keys to the {@code Set}, but they get removed while processing.
     * @related {@code updateMetadata(...)} - Add or replace metadata entries.
     * @related {@code META_DEFAULT_KEYS} - Contains all built-in metadata keys.
     */
    public void removeMetadata(Set<String> keys) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            DragNSounds.net().sendToServer(new RemoveMetadataPacket(id, location, keys));
        });
        EnvExecutor.runInEnv(Env.SERVER, () -> () -> {
            try {
                removeMetadataInternal(location, id, keys);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * A safe method to get the metadata of the sound file.
     * @param key The key name of the metadata entry.
     * @return The metadata string value or an empty string if the metadata entry does not exist.
     */
    public String getMetadataSafe(String key) {
        return metadata.containsKey(key) ? metadata.get(key) : "";
    }

    /**
     * @return The location where the sound file is saved.
     */
    public SoundLocation getLocation() {
        return location;
    }

    /**
     * @return The id of the sound and the name of the sound file on disk.
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return The file hash value.
     */
    public String hash() {
        return hash;
    }

    /**
     * @return Some additional read-only information about the file itself, such as the owner or the upload timestamp. It also contains shortcuts to important information for better performance. These values are changed automatically when the file hash changes.
     */
    public SoundFileInfo getInfo() {
        return info;
    }

    /**
     * @return An {@code Optional} containing the path to the sound file or an empty {@code Optional} if the path is invalid or there is an error while generating the path.
     */
    public Optional<Path> getPath() {
        return getPath(getLocation(), getId());
    }

    protected static Optional<Path> getPath(SoundLocation location, UUID soundId) {
        Optional<Path> path = location.resolve();
        if (path.isPresent()) {
            return Optional.of(Paths.get(path.get().toString() + "\\" + soundId + "." + DEFAULT_AUDIO_FILE_EXTENSION));
        }
        return path;
    }

    /**
     * Returns the file object of this sound.
     * @return The file, if available.
     * @side Server
     */
    public Optional<File> getAsFile() {
        Optional<Path> path = getPath();
        if (path.isPresent()) {
            return Optional.of(path.get().toFile());
        }
        return Optional.empty();
    }

    /**
     * A shortcut to a default metadata value.
     * @return The display name of the file.
     * @apiNote Metadata key: {@code META_DISPLAY_NAME}
     */
    public String getDisplayName() {
        return getMetadataSafe(META_DISPLAY_NAME);
    }

    /**
     * A shortcut to get the audio channels.
     * @return Audio channels enum value.
     */
    public EChannels getAudioChannels() {
        return EChannels.getByCount(getInfo().getChannels());
    }

    public CompoundTag serializeNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.put(NBT_LOCATION, getLocation().serializeNbt());
        nbt.putUUID(NBT_ID, getId());
        nbt.putString(NBT_HASH, hash());
        nbt.put(NBT_INFO, getInfo().serializeNbt());
        nbt.put(NBT_METADATA, ExtendedNBTUtils.saveMapToNBT(metadata));
        return nbt;
    }

    public void deserializeNbt(CompoundTag nbt, Level level) {
        location = SoundLocation.fromNbt(nbt.getCompound(NBT_LOCATION), level);
        id = nbt.getUUID(NBT_ID);
        hash = nbt.getString(NBT_HASH);
        info = SoundFileInfo.fromNbt(nbt.getCompound(NBT_INFO));
        metadata = ExtendedNBTUtils.loadMapFromNBT(nbt.getCompound(NBT_METADATA));
    }

    public static SoundFile fromNbt(CompoundTag nbt, Level level) {
        SoundFile file = new SoundFile();
        file.deserializeNbt(nbt, level);
        return file;
    }

    /**
     * Called on the server-side after loading the file. Do not call this by yourself!
     */
    public void validateHash() {
        Optional<Path> filePath = getPath();
        if (filePath.isPresent()) {
            Path path = filePath.get();
            String newHash = IOUtils.getFileHash(path.toString());
            if (!hash().equals(newHash)) {
                File file = new File(path.toString());
                info = SoundFileInfo.of(file, getInfo().getOwnerId(), getInfo().getUploadTimeMillis(), getInfo().getChannels(), getInfo().getDuration());
                hash = newHash;
                DragNSounds.LOGGER.warn("Refreshed custom sound file info of '" + path.toString() + "' because the hash value has changed.");
            }
        }
    }

    /**
     * Get a {@code SoundFile} object by location and filename (Id).
     * @param location The location where the sound is saved at.
     * @param id The if of the sound file (filename on disk without extension)
     * @return The {@code SoundFile}, if available.
     * @side Server
     */
    public static Optional<SoundFile> of(SoundLocation location, UUID id) {
        try {
            try (IndexFile index = IndexFile.open(location, true)) {
                if (index.has(id)) {
                    return Optional.of(index.getSoundFile(id));
                }
            }
        } catch (IOException e) {
            DragNSounds.LOGGER.error("Unable to open index file.", e);
        }
        return Optional.empty();
    }

    public static SoundFile client(SoundLocation location, UUID id) {
        SoundFile file = new SoundFile();
        file.location = location;
        file.id = id;
        return file;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash(), getId(), getLocation());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SoundFile other && getLocation().equals(other.getLocation()) && getId().equals(other.getId());
    }

    @Override
    public String toString() {
        return String.format("%s/%s", getLocation(), getId());
    }

    public static void updateMetadataInternal(SoundLocation location, UUID id, Map<String, String> meta) throws IOException {
        try (IndexFile index = IndexFile.open(location, false)) {
            if (index.has(id)) {
                SoundFile file = index.getSoundFile(id);
                meta.entrySet().forEach(e -> file.metadata.put(e.getKey(), e.getValue()));
            }
        }
    }

    public static void removeMetadataInternal(SoundLocation location, UUID id, Set<String> keys) throws IOException {
        try (IndexFile index = IndexFile.open(location, false)) {
            if (index.has(id)) {
                SoundFile file = index.getSoundFile(id);
                file.metadata.entrySet().removeIf(x -> Arrays.stream(META_DEFAULT_KEYS).noneMatch(y -> y.equals(x.getKey())) && keys.contains(x.getKey()));
            }
        }
    }


    /**
     * Data collection to create a new sound file on the server.
     */
    public static class Builder {

        private static final String NBT_LOCATION = "Location";
        private static final String NBT_METADATA = "Metadata";

        private SoundLocation location;
        private Map<String, String> metadata = new HashMap<>();

        /**
         * @param location The location where the sound file should be stored in.
         * @param displayName The display name of the sound file (not the filename). Can contain any characters.
         * @param initialMetadata A collection of metadata that should be added after creating the file.
         */
        public Builder(SoundLocation location, String displayName, Map<String, String> initialMetadata) {
            this.location = location;
            this.metadata.put(META_DISPLAY_NAME, displayName);
            this.metadata.put(META_DISPLAY_NAME, displayName);
            this.metadata.putAll(initialMetadata);
        }

        private Builder() {}

        /**
         * Create the new sound file and write it to disk.
         * @param owner The owner of the sound file (usually the UUID of the player).
         * @param dataStream The stream containing all the data of the file.
         * @param initialChannels A fallback option to get the amount of audio channels which is calculated on the client side (may be manipulated there too)
         * @param initialDuration A fallback option to get the audio playback duration which is calculated on the client side (may be manipulated there too)
         * @return A SoundFile object of the new file.
         * @side Server
         * @implNote You should not call this method by yourself, as this is managed by the library.
         * @throws IOException
         * @throws InputFormatException
         * @throws EncoderException
         */
        public synchronized SoundFile save(UUID owner, ByteArrayOutputStream dataStream, int initialChannels, long initialDuration) throws IOException, InputFormatException, EncoderException {
            try (IndexFile registry = IndexFile.open(location, false)) {
                UUID fileId = registry.generateId();
                Path path = getPath(location, fileId).get();
                File file = path.toFile();
                file.getParentFile().mkdirs();
                try (FileOutputStream out = new FileOutputStream(file)) {
                    dataStream.writeTo(out);
                }
                SoundFile soundFile = new SoundFile();
                soundFile.location = location;
                soundFile.id = fileId;
                soundFile.hash = IOUtils.getFileHash(path.toString());
                soundFile.info = SoundFileInfo.of(
                    path.toFile(),
                    owner,
                    System.currentTimeMillis(),
                    initialChannels,
                    initialDuration
                );
                soundFile.metadata.putAll(metadata);
                registry.add(soundFile);
                return soundFile;
            }
        }


        public CompoundTag serializeNbt() {
            CompoundTag nbt = new CompoundTag();
            nbt.put(NBT_LOCATION, location.serializeNbt());
            nbt.put(NBT_METADATA, ExtendedNBTUtils.saveMapToNBT(metadata));
            return nbt;
        }

        public void deserializeNbt(CompoundTag nbt, Level level) {
            location = SoundLocation.fromNbt(nbt.getCompound(NBT_LOCATION), level);
            metadata = ExtendedNBTUtils.loadMapFromNBT(nbt.getCompound(NBT_METADATA));
        }

        public static Builder fromNbt(CompoundTag nbt, Level level) {
            Builder builder = new Builder();
            builder.deserializeNbt(nbt, level);
            return builder;
        }
    }

}
