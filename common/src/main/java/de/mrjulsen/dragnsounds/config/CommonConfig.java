package de.mrjulsen.dragnsounds.config;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.mrjulsen.dragnsounds.api.ServerApi;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.filter.FileInfoFilter;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.VanillaTimeSystem;
import dev.architectury.utils.GameInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    
    public static final ForgeConfigSpec.ConfigValue<Boolean> ADVANCED_LOGGING;

    public static final ForgeConfigSpec.ConfigValue<Integer> USE_SOUND_COMMAND_PERMISSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> MANAGE_SOUND_COMMAND_PERMISSION;
    public static final ForgeConfigSpec.ConfigValue<Boolean> AUTO_CLEANUP;

    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_FILES_PER_USER;
    public static final ForgeConfigSpec.ConfigValue<Long> MAX_FILE_STORAGE_SPACE;
    public static final ForgeConfigSpec.ConfigValue<Long> MAX_FILE_SIZE;
    public static final ForgeConfigSpec.ConfigValue<Long> MAX_AUDIO_DURATION;

    public static final int MAX_FILE_SIZE_BYTES = 1073741824;

    static {
        BUILDER.push("DragNSounds API Config");

        /* CONFIGS */        
        ADVANCED_LOGGING = BUILDER.comment("Enables advanced logging features and more detailed console output.")
            .define("debug.advanced_logging", false);
        
        USE_SOUND_COMMAND_PERMISSION = BUILDER.comment("Minimum permission level required to use the basic features of the /sound command, such as playing, stopping and modifying sounds.")
            .defineInRange("permission.sound_command_usage", 2, 0, 4);
        MANAGE_SOUND_COMMAND_PERMISSION = BUILDER.comment("Minimum permission level required to use all features of the /sound command, such as uploading and deleting sound files.")
            .defineInRange("permission.sound_command_management", 3, 0, 4);
        AUTO_CLEANUP = BUILDER.comment("If active, a file cleanup will be performed at server startup to clean up unreachable ('dead') files or empty folders.")
            .define("cleanup_on_server_start", true);
        
        MAX_FILES_PER_USER = BUILDER.comment("How many audio files one user can upload. (Default: -1, -1 = unlimited, 0 = none)")
            .defineInRange("user_uploads.max_files", -1, -1, Integer.MAX_VALUE);
        MAX_FILE_STORAGE_SPACE = BUILDER.comment(new String[] {"[in Bytes]", "Maximum audio file storage space per user. (Default: -1, -1 = unlimited, 0 = none)"})
            .defineInRange("user_uploads.max_storage_size", -1, -1, Long.MAX_VALUE);
        MAX_FILE_SIZE = BUILDER.comment(new String[] {"[in Bytes]", "Maximum size of one single audio file a user can upload. (Default: -1, -1 = unlimited, 0 = none)"})
            .defineInRange("user_uploads.max_file_size", -1, -1, 1073741824L);
        MAX_AUDIO_DURATION = BUILDER.comment(new String[] {"[in Milliseconds]", "Maximum duration of one single audio file a user can upload. (Default: -1, -1 = unlimited, 0 = none)"})
            .defineInRange("user_uploads.max_audio_duration", -1, -1, Long.MAX_VALUE);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
    
    public static DLStatus checkFilePermissions(int fileSize, ServerPlayer player) {
        if ((MAX_FILE_SIZE.get() >= 0 && fileSize > MAX_FILE_SIZE.get()) || fileSize > MAX_FILE_SIZE_BYTES) {
            return new DLStatus(DLStatus.FLAG_ERROR, -1, "Not allowed to upload files larger than " + MAX_FILE_SIZE.get() + " bytes!");
        }

        if (player != null) {
            Optional<SoundFile[]> result = ServerApi.getAllSoundFiles(GameInstance.getServer().overworld(), List.of(new FileInfoFilter(FileInfoFilter.KEY_OWNER_UUID, player.getUUID().toString(), ECompareOperation.EQUALS)));
            if (result.isPresent()) {
                long totalSize = fileSize;
                int count = result.get().length;
                for (SoundFile file : result.get()) {
                    Optional<File> f = file.getAsFile();
                    totalSize += f.isPresent() ? f.get().length() : 0;
                }
                
                if (MAX_FILES_PER_USER.get() >= 0 && count >= MAX_FILES_PER_USER.get()) {
                    return new DLStatus(DLStatus.FLAG_ERROR, -2, "A maximum of " + MAX_FILES_PER_USER.get() + " files may be uploaded per player!");
                }
                if (MAX_FILE_STORAGE_SPACE.get() >= 0 && totalSize > MAX_FILE_STORAGE_SPACE.get()) {
                    return new DLStatus(DLStatus.FLAG_ERROR, -3, "The maximum storage quota of " + MAX_FILE_STORAGE_SPACE.get() + " bytes has already been exhausted!");
                }
            }
        }
        
        return new DLStatus(DLStatus.FLAG_OK, 0, "");
    }

    public static DLStatus checkAudioPermissions(SoundFile file, UUID player) {
        if (MAX_AUDIO_DURATION.get() >= 0 && file.getInfo().getDuration() > MAX_AUDIO_DURATION.get()) {
            return new DLStatus(DLStatus.FLAG_ERROR, -1, "Not allowed to upload audio files longer than " + DLTime.fromReal(0, 0, 0, MAX_AUDIO_DURATION.get().intValue(), 0, VanillaTimeSystem.INSTANCE).toRealMillis() + "!");
        }

        return new DLStatus(DLStatus.FLAG_OK, 0, "");
    }
}
