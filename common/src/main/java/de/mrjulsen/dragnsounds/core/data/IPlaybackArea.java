package de.mrjulsen.dragnsounds.core.data;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IPlaybackArea {
    /**
     * Check if the player matches the criteria.
     * @param level The current world level.
     * @param player The player to check.
     * @return {@code true} if the player matches the given criteria, {@code false} otherwise.
     */
    boolean canPlayForPlayer(Level level, Player player);
}
