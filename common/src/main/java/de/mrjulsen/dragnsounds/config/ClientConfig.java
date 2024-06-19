package de.mrjulsen.dragnsounds.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_STREAMING_CHANNELS;


    static {
        BUILDER.push("DragNSounds API Config");

        /* CONFIGS */
        MAX_STREAMING_CHANNELS = BUILDER.comment("The amount of streaming sound channels. By default, Minecraft has 8 streaming channels, but this mod increases it to 32. (Restart required)")
            .defineInRange("max_streaming_channels", 32, 8, 255);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
