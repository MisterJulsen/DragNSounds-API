package de.mrjulsen.dragnsounds.api.playback;

import de.mrjulsen.dragnsounds.core.data.IPlaybackArea;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * This implementation of {@code IPlaybackArea} checks if the player is riding any entity of the given type.
 */
public record EntityTypeRidingPlaybackArea<E extends Entity>(Class<E> entityType) implements IPlaybackArea {

    @Override
    public boolean canPlayForPlayer(Level level, Player player) {
        return entityType().equals(player.getVehicle().getClass());
    }
    
}
