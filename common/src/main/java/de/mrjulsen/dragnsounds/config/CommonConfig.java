package de.mrjulsen.dragnsounds.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    
    public static final ForgeConfigSpec.ConfigValue<Integer> USE_SOUND_COMMAND_PERMISSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> MANAGE_SOUND_COMMAND_PERMISSION;
    public static final ForgeConfigSpec.ConfigValue<Boolean> AUTO_CLEANUP;


    static {
        BUILDER.push("DragNSounds API Config");

        /* CONFIGS */
        USE_SOUND_COMMAND_PERMISSION = BUILDER.comment("Minimum permission level required to use the basic features of the /sound command, such as playing, stopping and modifying sounds.")
            .defineInRange("permission.sound_command_usage", 2, 0, 4);
        MANAGE_SOUND_COMMAND_PERMISSION = BUILDER.comment("Minimum permission level required to use all features of the /sound command, such as uploading and deleting sound files.")
            .defineInRange("permission.sound_command_management", 3, 0, 4);
        AUTO_CLEANUP = BUILDER.comment("If active, a file cleanup will be performed at server startup to clean up unreachable ('dead') files or empty folders.")
            .define("cleanup_on_server_start", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
