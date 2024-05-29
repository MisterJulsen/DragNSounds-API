package de.mrjulsen.dragnsounds.core.callbacks.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import de.mrjulsen.dragnsounds.api.Api;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;

public class SoundFileCallback {
    private static final Map<Long, Consumer<Optional<SoundFile>>> callbacks = new HashMap<>();

    public static long create(Consumer<Optional<SoundFile>> callback) {
        final long id = Api.id();
        callbacks.put(id, callback);
        return id;
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

    public static void clear() {
        callbacks.clear();
    }
}
