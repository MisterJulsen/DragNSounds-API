package de.mrjulsen.dragnsounds.api.playback;

import de.mrjulsen.dragnsounds.core.data.IPlaybackArea;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * This implementation of {@code IPlaybackArea} checks if the player is inside a box relative to the given root position.
 */
public record BoxPlaybackArea(Vec3 root, double x1, double y1, double z1, double x2, double y2, double z2) implements IPlaybackArea {

    public static BoxPlaybackArea of(Vec3 root, Vec3 a, Vec3 b){
        return new BoxPlaybackArea(root, a.x(), a.y(), a.z(), b.x(), b.y(), b.z());
    }

    public static BoxPlaybackArea of(Vec3 root, BlockPos a, BlockPos b){
        return new BoxPlaybackArea(root, a.getX(), a.getY(), a.getZ(), b.getX(), b.getY(), b.getZ());
    }

    public static BoxPlaybackArea of(BlockPos root, BlockPos a, BlockPos b){
        return new BoxPlaybackArea(new Vec3(root.getX(), root.getY(), root.getZ()), a.getX(), a.getY(), a.getZ(), b.getX(), b.getY(), b.getZ());
    }

    @Override
    public boolean canPlayForPlayer(Level level, Player player) {
        double xn = Math.min(x1(), x2()) + root().x();
        double yn = Math.min(y1(), y2()) + root().y();
        double zn = Math.min(z1(), z2()) + root().z();

        double xp = Math.max(x1(), x2()) + root().x();
        double yp = Math.max(y1(), y2()) + root().y();
        double zp = Math.max(z1(), z2()) + root().z();

        Vec3 playerPos = player.position();

        return xn < playerPos.x() && xp > playerPos.x() &&
               yn < playerPos.y() && yp > playerPos.y() &&
               zn < playerPos.z() && zp > playerPos.z();
    }
    
}
