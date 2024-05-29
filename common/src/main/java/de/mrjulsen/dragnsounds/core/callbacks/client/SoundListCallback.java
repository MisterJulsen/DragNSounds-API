package de.mrjulsen.dragnsounds.core.callbacks.client;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import de.mrjulsen.dragnsounds.api.Api;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;

public class SoundListCallback {
    
    private static final Map<Long, SoundListCallback> callbacks = new HashMap<>();

    public static long create(Consumer<SoundFile[]> task) {
        long id = Api.id();
        callbacks.computeIfAbsent(id, x -> new SoundListCallback(x, task));
        return id;
    }

    public static SoundListCallback get(long id) {
        return callbacks.get(id);
    }

    public static boolean runIfPresent(long id) {
        boolean b = callbacks.containsKey(id);
        if (b) {
            callbacks.get(id).run();
        }
        return b;
    }

    public static void close(long id) {
        callbacks.remove(id);
    }
    
    public static int getCount() {
        return callbacks.size();
    }

    public static void clear() {
        callbacks.clear();
    }



    private final long id;
    private final Set<SoundFile> files = new LinkedHashSet<>();
    private final Consumer<SoundFile[]> task;

    public SoundListCallback(long id, Consumer<SoundFile[]> task) {
        this.id = id;
        this.task = task;
    }

    public void add(SoundFile file) {
        files.add(file);
    }

    public void run() {
        task.accept(files.stream().toArray(SoundFile[]::new));
        close(id);
    }

    public long getId() {
        return id;
    }
}
