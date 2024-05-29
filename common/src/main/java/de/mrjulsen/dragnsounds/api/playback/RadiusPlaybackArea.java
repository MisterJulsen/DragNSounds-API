package de.mrjulsen.dragnsounds.api.playback;

import de.mrjulsen.dragnsounds.core.data.IPlaybackArea;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * This implementation of {@code IPlaybackArea} checks if the player is inside the radius around the given center position.
 */
public record RadiusPlaybackArea(Vec3 center, double radius) implements IPlaybackArea {

    @Override
    public boolean canPlayForPlayer(Level level, Player player) {
        return center().distanceToSqr(player.position()) <= radius();
    }
    
}
