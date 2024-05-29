package de.mrjulsen.dragnsounds.core.callbacks.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.mrjulsen.dragnsounds.core.data.SoundPlaybackData;
import net.minecraft.world.entity.player.Player;

public class SoundGetDataCallback {
    private static final Map<Long, ISoundPlaybackData> callbacks = new HashMap<>();

    public static void create(long soundId, ISoundPlaybackData callback) {
        callbacks.put(soundId, callback);
    }   

    public static boolean run(long id, Player player, SoundPlaybackData value) {
        boolean b = callbacks.containsKey(id);
        if (b) {
            callbacks.remove(id).run(player, Optional.of(value));
        }
        return b;
    }

    public static void clear() {
        callbacks.clear();
    }

    public static int getCount() {
        return callbacks.size();
    }

    @FunctionalInterface
    public static interface ISoundPlaybackData {
        void run(Player player, Optional<SoundPlaybackData> data);
    }
}
