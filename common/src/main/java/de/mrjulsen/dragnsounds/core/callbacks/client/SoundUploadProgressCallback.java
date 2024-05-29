package de.mrjulsen.dragnsounds.core.callbacks.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.mrjulsen.dragnsounds.api.ClientApi.UploadProgress;

public class SoundUploadProgressCallback {
    private static final Map<Long, Consumer<UploadProgress>> callbacks = new HashMap<>();

    public static long create(long requestId, Consumer<UploadProgress> callback) {
        callbacks.put(requestId, callback);
        return requestId;
    }   

    public static boolean run(long id, UploadProgress value) {
        boolean b = callbacks.containsKey(id);
        if (b) {
            callbacks.get(id).accept(value);
        }
        return b;
    }

    public static int getCount() {
        return callbacks.size();
    }

    public static void close(long id) {
        callbacks.remove(id);
    }

    public static void clear() {
        callbacks.clear();
    }
}
