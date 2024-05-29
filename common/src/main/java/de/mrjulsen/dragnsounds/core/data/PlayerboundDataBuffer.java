package de.mrjulsen.dragnsounds.core.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.mrjulsen.dragnsounds.api.Api;
import de.mrjulsen.mcdragonlib.data.StatusResult;

public class PlayerboundDataBuffer implements AutoCloseable {
    private byte[] buffer;
    private Map<UUID, Map<Long, Integer>> positions = new HashMap<>();
    private final long id;

    public static final int NO_LISTENERS = 1;
    public static final int NO_PLAYER_WITH_UUID = 2;
    public static final int NO_SOUND_WITH_ID = 3;
    public static final int NO_SOUND_AND_PLAYER = 4;

    public PlayerboundDataBuffer(InputStream stream) throws IOException {
        this.buffer = stream.readAllBytes();
        this.id = Api.id();
        stream.close();
    }

    public long getId() {
        return id;
    }

    public boolean hasPlayerOrSound(UUID player, long soundId) {
        return positions.containsKey(player) && positions.get(player).containsKey(soundId);
    }

    public int maxSize(UUID player, long soundId, int targetLength) {
        int index = positions.get(player).get(soundId);
        int bufferLength = buffer.length;
        return Math.min(bufferLength - index, targetLength);
    }

    public synchronized boolean read(UUID player, long soundId, byte[] data) {
        int index = positions.get(player).get(soundId);
        int bufferLength = buffer.length;
        int maxLen = Math.min(bufferLength - index, data.length);

        System.arraycopy(buffer, index, data, 0, maxLen);

        positions.get(player).replace(soundId, index + maxLen);
        
        boolean b = data.length < bufferLength - index;
        return b;
    }

    public void register(UUID player, long soundId) {
        positions.computeIfAbsent(player, x -> new HashMap<>()).computeIfAbsent(soundId, x -> 0);
    }

    public StatusResult remove(UUID player, long soundId, int returnReason) {
        if (positions.containsKey(player)) {            
            Map<Long, Integer> positionsBySound = positions.get(player);
            positionsBySound.remove(soundId);
            if (positionsBySound.isEmpty()) {
                positions.remove(player);
            }
        }

        switch (returnReason) {
            case NO_PLAYER_WITH_UUID:
                return new StatusResult(!positions.containsKey(player), NO_PLAYER_WITH_UUID, null);
            case NO_SOUND_WITH_ID:
                return new StatusResult(positions.values().stream().allMatch(x -> x.keySet().stream().noneMatch(y -> y == soundId)), NO_SOUND_WITH_ID, null);
            case NO_SOUND_AND_PLAYER:
                return new StatusResult(!positions.containsKey(player) && positions.values().stream().allMatch(x -> x.keySet().stream().noneMatch(y -> y == soundId)), NO_SOUND_AND_PLAYER, null);
            default:
                return new StatusResult(positions.isEmpty(), NO_LISTENERS, null);            
        }
    }

    public boolean remove(UUID player) {
        if (positions.containsKey(player)) {
            positions.remove(player);
        }

        return positions.isEmpty();
    }

    public boolean hasListeners() {
        return !positions.isEmpty();
    }

    public int getSize() {
        return buffer.length;
    }

    @Override
    public void close() {
        buffer = null;
        positions.clear();
    }
}
