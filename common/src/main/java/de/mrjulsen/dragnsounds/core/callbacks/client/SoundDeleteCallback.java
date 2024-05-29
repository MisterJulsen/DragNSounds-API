package de.mrjulsen.dragnsounds.core.callbacks.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.mrjulsen.dragnsounds.api.Api;
import de.mrjulsen.mcdragonlib.data.StatusResult;

public class SoundDeleteCallback {
    private static final Map<Long, Consumer<StatusResult>> callbacks = new HashMap<>();

    public static long create(Consumer<StatusResult> callback) {
        final long id = Api.id();
        callbacks.put(id, callback);
        return id;
    }   

    public static boolean run(long id, StatusResult value) {
        boolean b = callbacks.containsKey(id);
        if (b) {
            callbacks.remove(id).accept(value);
        }
        return b;
    }

    public static int getCount() {
        return callbacks.size();
    }

    public static void clear() {
        callbacks.clear();
    }
}
