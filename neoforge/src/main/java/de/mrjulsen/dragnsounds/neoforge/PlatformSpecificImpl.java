package de.mrjulsen.dragnsounds.neoforge;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.config.CommonConfig;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import fuzs.forgeconfigapiport.neoforge.impl.forge.ForgeConfigRegistryImpl;
import net.neoforged.fml.config.ModConfig;

public class PlatformSpecificImpl {
    public static void registerConfig() {   
        if (Platform.getEnvironment() == Env.CLIENT) {
            ForgeConfigRegistryImpl.INSTANCE.register(DragNSounds.MOD_ID, ModConfig.Type.CLIENT, CommonConfig.SPEC, DragNSounds.MOD_ID + "-client.toml");
        }
        ForgeConfigRegistryImpl.INSTANCE.register(DragNSounds.MOD_ID, ModConfig.Type.COMMON, CommonConfig.SPEC, DragNSounds.MOD_ID + "-common.toml");
    }
}
