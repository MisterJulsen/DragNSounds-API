package de.mrjulsen.dragnsounds.core.callbacks.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.data.DLStatus;

public class SoundStartUploadCallback {
    private static final Map<Long, Consumer<DLStatus>> callbacks = new HashMap<>();

    public static long create(long requestId, Consumer<DLStatus> callback) {
        callbacks.put(requestId, callback);
        return requestId;
    }   

    public static boolean run(long id, DLStatus value) {
        boolean b = callbacks.containsKey(id);
        if (b) {
            callbacks.remove(id).accept(value);
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
