package de.mrjulsen.dragnsounds.core.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.mcdragonlib.data.INBTSerializable;
import de.mrjulsen.mcdragonlib.util.IOUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.Level;

/**
 * A file containing information about all custom sound files in the same directory. Files that are not registered in this index file cannot be accessed.
 */
public class IndexFile implements INBTSerializable, AutoCloseable {

    public static final String INDEX_FILENAME = "index.nbt";
    /** The current version of the index file save format. */ public static final int DATA_VERSION = 1;

    private static final String NBT_VERSION = "Version";
    private static final String NBT_FILES = "Files";

    private final SoundLocation location;
    private final Path path;
    private final boolean readOnly;
    private Map<UUID, SoundFile> files = new HashMap<>();

    /**
     * Get the index file of the given location.
     * @param location The target directory location.
     * @throws IOException
     */
    public IndexFile(SoundLocation location, boolean readOnly) throws IOException {
        Optional<Path> path = location.resolve();
        if (!path.isPresent()) {
            throw new IOException("Unable to get sound location path.");
        }

        this.location = location;
        this.path = path.get();
        this.readOnly = readOnly;
        checkAndRepair();
    }

    /**
     * Checks all entries and removes files that no longer exist at the current location.
     */
    public void checkAndRepair() {        
        files.entrySet().removeIf(x -> !x.getValue().getAsFile().isPresent() || !x.getValue().getAsFile().get().exists());
        files.values().forEach(x -> x.validateHash());
    }

    /**
     * @return The location of this file.
     */
    public SoundLocation getLocation() {
        return location;
    }

    /**
     * Registers the given sound file in the index file so it can be accessed later. This is very important, otherwise the sound file gets lost and may be deleted.
     * @param file The sound file to register.
     * @apiNote  Cannot be used in read-only mode!
     */
    public void add(SoundFile file) {
        throwIfReadOnly();
        files.put(file.getId(), file);
    }

    /**
     * Checks if a sound file with the given id exists.
     * @param soundId The id of the custom sound file you want to check.
     * @return {@code true} if the file exists, {@code false} otherwise.
     */
    public boolean has(UUID soundId) {
        return files.containsKey(soundId);
    }

    /**
     * Checks if the sound file exists.
     * @param file The file you want to check.
     * @return {@code true} if the file exists, {@code false} otherwise.
     */
    public boolean has(File file) {
        try {
            return files.containsKey(UUID.fromString(IOUtils.getFileNameWithoutExtension(file.toPath().toString())));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Deletes the sound file from the disk and from the index file.
     * @param soundId The id of the custom sound file.
     * @return Whether the action was successfull.
     * @apiNote Cannot be used in read-only mode!
     */
    public boolean delete(UUID soundId) {
        throwIfReadOnly();
        boolean success = false;
        Optional<File> fileObj = getSoundFile(soundId).getAsFile();
        if (fileObj.isPresent()) {
            success = fileObj.get().delete();
        }
        if (success) {
            files.remove(soundId);
        }
        return success;
    }

    /**
     * Get the sound file with the given id at the current location of the index file.
     * @param soundId The id of the cusotm sound file.
     * @return The {@code SoundFile} if available, {@code null} otherwise.
     */
    public SoundFile getSoundFile(UUID soundId) {
        return files.get(soundId);
    }

    /**
     * @return A list of all registered sound files that also exist on disk.
     */
    public SoundFile[] getAll() {
        return files.values().stream().filter(x -> x.getAsFile().isPresent() && x.getAsFile().get().exists()).toArray(SoundFile[]::new);
    }

    /**
     * @return If this instance of the file is read-only.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Saves the changed index file to disk. Run this every time after changing some values. You can also use try-with-resources which saves the changed index file automatically.
     * @apiNote Cannot be used in read-only mode!
     */
    public synchronized void save() {
        throwIfReadOnly();

        CompoundTag nbt = serializeNbt();
        try {
            checkAndRepair();
            NbtIo.writeCompressed(nbt, new File(path.toString() + "/" + INDEX_FILENAME));
        } catch (IOException e) {
            DragNSounds.LOGGER.error("Unable to save index file.", e);
        }
    }

    /**
     * Open the index file at the given location. If no index file exists, a new one will be created (only in memory, not on disk).
     * @param location The location of the index file.
     * @param readOnly If the instance should be read-only. Use this whenever possible to make the file accessible for other processes.
     * @return The index file.
     * @throws IOException
     */
    public static IndexFile open(SoundLocation location, boolean readOnly) throws IOException {
        Optional<Path> path = location.resolve();
        if (path.isPresent()) {
            File indexFile = new File(path.get().toString() + "/" + INDEX_FILENAME);
            if (indexFile.exists()) {
                IndexFile file = new IndexFile(location, readOnly);
                file.deserializeNbt(NbtIo.readCompressed(indexFile));
                return file;
            }
        }
        return new IndexFile(location, readOnly);
    }

    /**
     * Checks if an index file exists at the given location.
     * @param location The location to check.
     * @return {@code true} if there is an index file at this location.
     */
    public static boolean existsIn(SoundLocation location) {
        Optional<Path> path = location.resolve();
        if (path.isPresent()) {
            File indexFile = new File(path.get().toString() + "/" + INDEX_FILENAME);
            return indexFile.exists();
        }
        return false;
    }

    /**
     * Generates a new id that doesn't already exist at this location.
     * @return The new id.
     */
    public UUID generateId() {
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (files.containsKey(id) && files.get(id).getAsFile().isPresent() && files.get(id).getAsFile().get().exists());
        return id;
    }

    private void throwIfReadOnly() {
        if (readOnly) {
            throw new IllegalAccessError("Cannot perform this action in read-only mode!");
        }
    }

    @Override
    public CompoundTag serializeNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(NBT_VERSION, DATA_VERSION);
        nbt.put(NBT_FILES, saveMapToNBT());
        return nbt;
    }

    @Override
    public void deserializeNbt(CompoundTag nbt) {
        @SuppressWarnings("unused") int version = nbt.getInt(NBT_VERSION);
        loadMapFromNBT(nbt.getCompound(NBT_FILES), location.getLevel());
    }

    private CompoundTag saveMapToNBT() {
        CompoundTag compound = new CompoundTag();
        for (Map.Entry<UUID, SoundFile> entry : files.entrySet()) {
            compound.put(entry.getKey().toString(), entry.getValue().serializeNbt());
        }

        return compound;
    }

    private void loadMapFromNBT(CompoundTag compound, Level level) {
        for (String key : compound.getAllKeys()) {
            files.put(UUID.fromString(key), SoundFile.fromNbt(compound.getCompound(key), level));
        }
    }

    @Override
    public void close() {
        if (!readOnly) {
            save();
        }
    }

    /**
     * @return The amount of registered files.
     */
    public int count() {
        return files.size();
    }
    
}
