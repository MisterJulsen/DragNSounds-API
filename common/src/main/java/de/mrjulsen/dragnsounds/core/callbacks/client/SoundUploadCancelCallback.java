package de.mrjulsen.dragnsounds.core.callbacks.client;

import java.util.HashMap;
import java.util.Map;

public class SoundUploadCancelCallback {
    private static final Map<Long, Runnable> cancelAction = new HashMap<>();
    private static final Map<Long, Boolean> cancellable = new HashMap<>();

    public static void setCancelAction(long requestId, Runnable action) {
        cancelAction.put(requestId, action);
        cancellable.put(requestId, false);
    } 
    
    public static void setCancellable(long requestId, boolean b) {
        cancellable.put(requestId, b);
    } 

    public static boolean cancelRun(long id) {
        boolean b = cancelAction.containsKey(id) && cancellable.containsKey(id) && cancellable.get(id);
        if (b) {
            cancelAction.get(id).run();
        }
        return b;
    }

    public static boolean canCancel(long id) {
        return cancelAction.containsKey(id) && cancellable.containsKey(id) && cancellable.get(id);
    }

    public static int getCount() {
        return cancelAction.size();
    }

    public static void close(long id) {
        cancelAction.remove(id);
        cancellable.remove(id);
    }

    public static void clear() {
        cancelAction.clear();
        cancellable.clear();
    }
}
