package de.mrjulsen.dragnsounds.core.callbacks.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;

public class SoundUploadCallback {
    private static final Map<Long, Consumer<Optional<SoundFile>>> callbacks = new HashMap<>();

    public static long create(long requestId, Consumer<Optional<SoundFile>> callback) {
        callbacks.put(requestId, callback);
        return requestId;
    }   

    public static boolean run(long id, Optional<SoundFile> value) {
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
