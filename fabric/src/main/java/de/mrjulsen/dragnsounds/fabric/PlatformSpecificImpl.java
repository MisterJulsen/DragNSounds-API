package de.mrjulsen.dragnsounds.fabric;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.config.ClientConfig;
import de.mrjulsen.dragnsounds.config.CommonConfig;
import fuzs.forgeconfigapiport.fabric.impl.core.ForgeConfigRegistryImpl;
import net.neoforged.fml.config.ModConfig;

public class PlatformSpecificImpl {
    public static void registerConfig() {
        ForgeConfigRegistryImpl.INSTANCE.register(DragNSounds.MOD_ID, ModConfig.Type.CLIENT, ClientConfig.SPEC, DragNSounds.MOD_ID + "-client.toml");
        ForgeConfigRegistryImpl.INSTANCE.register(DragNSounds.MOD_ID, ModConfig.Type.COMMON, CommonConfig.SPEC, DragNSounds.MOD_ID + "-common.toml");
    }
}
