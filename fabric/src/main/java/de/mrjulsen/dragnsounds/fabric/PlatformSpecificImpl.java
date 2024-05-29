package de.mrjulsen.dragnsounds.fabric;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.config.ClientConfig;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class PlatformSpecificImpl {
    public static void registerConfig() {
        ModLoadingContext.registerConfig(DragNSounds.MOD_ID, ModConfig.Type.CLIENT, ClientConfig.SPEC, DragNSounds.MOD_ID + "-client.toml");
        ModLoadingContext.registerConfig(DragNSounds.MOD_ID, ModConfig.Type.COMMON, ClientConfig.SPEC, DragNSounds.MOD_ID + "-common.toml");
    }
}
