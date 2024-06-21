package de.mrjulsen.dragnsounds.core.filesystem;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.mcdragonlib.data.INBTSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

/**
 * An object to locate sound files.
 */
public class SoundLocation implements INBTSerializable {

    private static final String NBT_NAMESPACE = "Namespace";
    private static final String NBT_PATH = "Path";

    protected Level level;
    protected String namespace = "";
    protected String relativePath = "";

    /**
     * Creates an object to locate a specific folder containing the desired sound files.
     * @param namespace The namespace directory name. The namespace is the first directory (root) inside the mod's data folder inside the world's data folder.
     * @param relativePath The relative path inside the namespace directory. Can be an empty string.
     */
    public SoundLocation(Level level, String namespace, String relativePath) {
        this(level);
        if (namespace.isBlank() || namespace.chars().anyMatch(x -> !isAllowedInNamespace((char)x))) {
            throw new IllegalArgumentException(String.format("'%s' is no valid namespace in SoundLocation.", namespace));
        }
        this.namespace = namespace;
        if (relativePath.chars().anyMatch(x -> !isAllowedInPath((char)x))) {
            throw new IllegalArgumentException(String.format("'%s' is no valid path in SoundLocation.", relativePath));
        }
        this.relativePath = relativePath;
    }
    /**
     * Creates an object to locate a specific folder containing the desired sound files.
     * @param location The string value of a sound location. Should contain {@code :} to separate namespace and path.
     * @example namespace:path/to/file
     */
    public SoundLocation(Level level, String location) {
        this(level);
        String[] parts = location.split(":");
        
        if (parts.length < 1) {
            throw new IllegalArgumentException(String.format("SoundLocation must not be empty: %s", location));
        }        

        String namespace = parts[0];
        String relativePath = "";
        
        if (parts.length > 1) {
            relativePath = parts[1];
        }
        
        if (namespace.isBlank() || namespace.chars().anyMatch(x -> !isAllowedInNamespace((char)x))) {
            throw new IllegalArgumentException(String.format("'%s' is no valid namespace in SoundLocation.", namespace));
        }
        this.namespace = namespace;
        if (relativePath.chars().anyMatch(x -> !isAllowedInPath((char)x))) {
            throw new IllegalArgumentException(String.format("'%s' is no valid path in SoundLocation.", relativePath));
        }
        this.relativePath = relativePath;
    }

    public SoundLocation(Level level, Path location) {
        this(level);
        Path relativePath = getModDirectory(level).relativize(location);
        String pathStr = relativePath.toString().replace("\\", "/");
        this.namespace = pathStr.split("/")[0];
        String path = pathStr.replace(namespace, "");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        this.relativePath = path;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    private SoundLocation(Level level) {
        this.level = level;
    }
    
    public static boolean isAllowedInNamespace(char character) {
        return character >= '0' && character <= '9' || character >= 'a' && character <= 'z' || character == '_' || character == '.' || character == '-';
    }
    
    public static boolean isAllowedInPath(char character) {
        return character >= '0' && character <= '9' || character >= 'a' && character <= 'z' || character == '_' || character == '/' || character == '.' || character == '-';
    }

    public static boolean isAllowedInSoundLocation(char character) {
        return character >= '0' && character <= '9' || character >= 'a' && character <= 'z' || character == '_' || character == ':' || character == '/' || character == '.' || character == '-';
    }

    /**
     * @return The name of the working directory where the sound files are saved. The mod's sound folder in each world is divided into several subfolders (namespaces), each of which represents its own workspace in which each addon can work as desired. Addons can then decide for themselves whether they want to expand these working areas even further.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @return The relative path within the namespace folder.
     */
    public String getRelativePath() {
        return relativePath;
    }

    /**
     * @return The String of the complete relative path containing the namespace and relative path variable. (e.g. {@code my_namespace/my/sub/dir})
     */
    public String buildPath() {
        return namespace + "/" + relativePath;
    }

    /**
     * Validates the given path to prevent access of files outside the sound's root directory.
     * @return A filled {@code Optional} when the given path in this object is valid.
     */
    public final Optional<Path> resolve() {
        Path validRoot = getModDirectory(level).toAbsolutePath().normalize();
        Path userPath = validRoot.resolve(buildPath()).normalize();

        if (!validRoot.equals(userPath) && userPath.startsWith(validRoot)) {
            return Optional.ofNullable(userPath);
        }
        return Optional.empty();
    }

    /**
     * @param level The world the sound is saved in.
     * @return The path to the directory inside the world's data folder where the sounds are saved.
     */
    public static final Path getModDirectory(Level level) {
        return level.getServer().getWorldPath(new LevelResource("data\\" + DragNSounds.MOD_ID));
    }

    /**
     * The world the sound is located in.
     * @return
     */
    public Level getLevel() {
        return level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNamespace(), getRelativePath());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SoundLocation other && getNamespace().equals(other.getNamespace()) && getRelativePath().equals(other.getRelativePath());
    }

    @Override
    public String toString() {
        return String.format("%s:%s", getNamespace(), getRelativePath().replace("\\", "/"));
    }

    @Override
    public CompoundTag serializeNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(NBT_NAMESPACE, getNamespace());
        nbt.putString(NBT_PATH, getRelativePath());
        return nbt;
    }

    @Override
    public void deserializeNbt(CompoundTag nbt) {
        namespace = nbt.getString(NBT_NAMESPACE);
        relativePath = nbt.getString(NBT_PATH);
    }

    public static SoundLocation fromNbt(CompoundTag nbt, Level level) {
        SoundLocation location = new SoundLocation(level);
        location.deserializeNbt(nbt);
        return location;
    }

    public static SoundLocation empty() {
        return new SoundLocation(null);
    }
}
