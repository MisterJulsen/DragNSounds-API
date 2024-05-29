package de.mrjulsen.dragnsounds.core.callbacks.client;

import java.util.HashMap;
import java.util.Map;

import de.mrjulsen.dragnsounds.core.data.ChannelContext;

public class SoundChannelsHolder {
    private static final Map<Long, ChannelContext> holder = new HashMap<>();

    public static long create(long requestId, ChannelContext value) {
        holder.put(requestId, value);
        return requestId;
    }   

    public static ChannelContext get(long id) {
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
