package de.mrjulsen.dragnsounds.core.callbacks.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.mrjulsen.dragnsounds.api.Api;

public class SoundMetadataCallback {
    private static final Map<Long, Consumer<Map<String, String>>> callbacks = new HashMap<>();

    public static long create(Consumer<Map<String, String>> callback) {
        final long id = Api.id();
        callbacks.put(id, callback);
        return id;
    }   

    public static boolean run(long id, Map<String, String> value) {
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
