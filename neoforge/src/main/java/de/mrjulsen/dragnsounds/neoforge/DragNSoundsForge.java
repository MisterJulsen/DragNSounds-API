package de.mrjulsen.dragnsounds.neoforge;

import de.mrjulsen.dragnsounds.DragNSounds;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(DragNSounds.MOD_ID)
public final class DragNSoundsForge {

    public DragNSoundsForge(ModContainer container) {
        DragNSounds.init();
    }
}
