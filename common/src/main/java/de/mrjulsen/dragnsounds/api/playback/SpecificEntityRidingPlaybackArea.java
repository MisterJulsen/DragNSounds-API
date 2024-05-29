package de.mrjulsen.dragnsounds.api.playback;

import de.mrjulsen.dragnsounds.core.data.IPlaybackArea;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * This implementation of {@code IPlaybackArea} checks if the player is riding a specific entity. If you want to use any instance of an entity, but not a specific one, you should use {@code EntityTypeRidingPlaybackArea} instead.
 * @see EntityTypeRidingPlaybackArea
 */
public record SpecificEntityRidingPlaybackArea(Entity vehicle) implements IPlaybackArea {

    @Override
    public boolean canPlayForPlayer(Level level, Player player) {
        return player.getVehicle() == vehicle();
    }
    
}
