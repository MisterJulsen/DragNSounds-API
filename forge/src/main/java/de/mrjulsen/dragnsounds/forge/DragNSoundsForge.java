package de.mrjulsen.dragnsounds.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import de.mrjulsen.dragnsounds.DragNSounds;

@Mod(DragNSounds.MOD_ID)
public final class DragNSoundsForge {
    public DragNSoundsForge() {
        EventBuses.registerModEventBus(DragNSounds.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        DragNSounds.init();
    }
}
