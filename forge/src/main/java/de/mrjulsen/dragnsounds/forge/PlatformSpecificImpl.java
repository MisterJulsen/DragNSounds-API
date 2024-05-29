package de.mrjulsen.dragnsounds.forge;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.config.ClientConfig;
import de.mrjulsen.dragnsounds.config.CommonConfig;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class PlatformSpecificImpl {
    public static void registerConfig() {        
        if (Platform.getEnvironment() == Env.CLIENT) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, DragNSounds.MOD_ID + "-client.toml");
            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, DragNSounds.MOD_ID + "-common.toml");
        }
    }
}
