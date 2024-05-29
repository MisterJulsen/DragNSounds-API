package de.mrjulsen.dragnsounds.core.callbacks.server;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.mrjulsen.dragnsounds.api.Api;

public class SoundPlayingCheckCallback {
    private static final Map<Long, Consumer<Boolean>> callbacks = new HashMap<>();

    public static long create(Consumer<Boolean> callback) {
        final long id = Api.id();
        callbacks.put(id, callback);
        return id;
    }   

    public static boolean run(long id, boolean value) {
        boolean b = callbacks.containsKey(id);
        if (b) {
            callbacks.remove(id).accept(value);
        }
        return b;
    }

    public static void clear() {
        callbacks.clear();
    }

    public static int getCount() {
        return callbacks.size();
    }
}
