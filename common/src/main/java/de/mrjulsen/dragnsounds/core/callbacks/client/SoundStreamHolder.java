package de.mrjulsen.dragnsounds.core.callbacks.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.core.data.SoundDataStream;

public class SoundStreamHolder {
    private static final Map<Long, SoundDataStream> holder = new HashMap<>();

    public static SoundDataStream getOrCreate(long requestId, Supplier<SoundDataStream> value) {
        return holder.computeIfAbsent(requestId, x -> value.get());
    }   

    public static SoundDataStream get(long id) {
        return holder.get(id);
    }

    public static boolean has(long id) {
        return holder.containsKey(id);
    }

    public static int getCount() {
        return holder.size();
    }

    public static void close(long id) {
        holder.remove(id);
    }

    public static void clear() {
        holder.clear();
    }
}
