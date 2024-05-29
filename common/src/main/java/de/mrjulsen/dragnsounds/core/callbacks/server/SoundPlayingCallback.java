package de.mrjulsen.dragnsounds.core.callbacks.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.entity.player.Player;

public class SoundPlayingCallback {
        private static final Map<Long, ISoundCreatedCallback> callbacks = new HashMap<>();

    public static void create(long soundId, ISoundCreatedCallback callback) {
        callbacks.put(soundId, callback);
    }   

    public static boolean run(long id, Player player, ESoundPlaybackStatus status) {
        boolean b = callbacks.containsKey(id);
        if (b) {
            callbacks.get(id).run(player, id, status);
        }
        return b;
    }

    public static boolean runAndClose(long id, Player player, ESoundPlaybackStatus status) {
        boolean b = callbacks.containsKey(id);
        if (b) {
            callbacks.remove(id).run(player, id, status);
        }
        return b;
    }

    public static int getCount() {
        return callbacks.size();
    }

    public static void clear() {
        callbacks.entrySet().forEach(x -> x.getValue().run(null, x.getKey(), ESoundPlaybackStatus.STOP));
        callbacks.clear();
    }

    @FunctionalInterface
    public static interface ISoundCreatedCallback {
        /**
         * Called when the client received a sound data packet.
         * @param player The player.
         * @param soundId The id of the custom sound.
         * @param status {@code true} if the client has started playing the sound, {@code false} when the client has only received the first data packet.
         */
        void run(Player player, long soundId, ESoundPlaybackStatus status);
    }

    public static enum ESoundPlaybackStatus {
        PREPARE(0),
        PLAY(1),
        STOP(2);

        private int id;

        private ESoundPlaybackStatus(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static ESoundPlaybackStatus getById(int id) {
            return Arrays.stream(values()).filter(x -> x.getId() == id).findFirst().orElse(STOP);
        }
    }
}
