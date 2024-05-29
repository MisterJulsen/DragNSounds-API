package de.mrjulsen.dragnsounds.fabric;

import net.fabricmc.api.ModInitializer;

import de.mrjulsen.dragnsounds.DragNSounds;

public final class DragNSoundsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DragNSounds.init();
    }
}
