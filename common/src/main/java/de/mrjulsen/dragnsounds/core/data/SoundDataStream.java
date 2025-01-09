package de.mrjulsen.dragnsounds.core.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundStreamHolder;
import de.mrjulsen.dragnsounds.net.cts.SoundDataRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.StopSoundNotificationPacket;
import de.mrjulsen.dragnsounds.net.stc.SoundDataPacket;

public class SoundDataStream extends InputStream {

    private final long soundId;
    private Queue<Byte> bufferQ;
    private int maxSize;

    private int pendingBytes;
    private boolean hasData = true;

    private int packetIndexRequested;
    private int packetIndexNeeded;

    private final PriorityQueue<SoundDataPacket> queuedData = new PriorityQueue<>();
    private boolean isStreaming = true;

    public SoundDataStream(int initialSize, long soundId) {
        this.soundId = soundId;
        this.bufferQ = new ConcurrentLinkedQueue<>();
        this.maxSize = initialSize;

        //this.writeIndex = 0;
        
        this.packetIndexRequested = 0;
        this.packetIndexNeeded = 0;

        new Thread(this::dequeue, "Sound Stream " + soundId).start();
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'read'");
    }

    public long getSoundId() {
        return soundId;
    }

    public boolean hasSpace(int length) {
        return length < remainingSpace();
    }

    public boolean willHaveSpace(int length) {
        return length < remainingSpaceAfterRequest();
    }

    public int remainingSpaceAfterRequest() {
        return totalSpace() - filledSpaceAfterRequest();
    }

    public int remainingSpace() {
        return totalSpace() - filledSpace();
    }

    public int filledSpaceAfterRequest() {
        return filledSpace() + pendingBytes;
    }

    public int filledSpace() {
        return bufferQ.size();
    }

    public int totalSpace() {
        return maxSize;
    }

    public int currentlyNeeded() {
        return packetIndexNeeded;
    }

    public boolean isPacketNeeded(int packetIndex) {
        return packetIndexNeeded == packetIndex;
    }

    public boolean canRequestData() {
        return hasData;
    }

    public boolean shouldReject(int packetIndex) {
        return packetIndexNeeded > packetIndex;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return super.read(b);
    }

    public int read(byte[] data) {
        int iteration = 0;
        while (hasData && filledSpace() <= data.length) {
            if (Thread.currentThread().getThreadGroup() != DragNSounds.ASYNC_GROUP) {
                DragNSounds.LOGGER.warn("No sound data available. Stream will be stopped.");
                return -1;
            }            
            if (iteration == 0) {
                DragNSounds.LOGGER.info("Waiting for sound data... (This is NOT a bug when called from 'Sound Command Listener X' Thread)");
            }
            try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException e) { }
            iteration++;
        }

        if (!hasData && (filledSpace() <= 0 || data == null || data.length <= 0)) {
            return -1;
        }

        int maxLen = Math.min(filledSpace(), data.length);
        
        for (int i = 0; i < maxLen; i++) {
            data[i] = bufferQ.poll();
        }
        this.pendingBytes -= maxLen;

        if (canRequestData() && willHaveSpace(8192 * 2)) {
            if (packetIndexRequested < packetIndexNeeded) {
                packetIndexRequested = packetIndexNeeded;
            }
            pendingBytes += 8192 * 2;
            DragNSounds.net().sendToServer(new SoundDataRequestPacket(this.getSoundId(), 8192 * 2, packetIndexRequested));
            packetIndexRequested++;
        }
        return maxLen;
    }

    private synchronized void write(byte[] data, int index, boolean hasMore) {
        if (!hasSpace(data.length)) {
            throw new IllegalStateException("Cannot write to buffer. Not enough space.");
        }
        /*
        if (!canRequestData()) {
            return;
        }
        */

        for (byte b : data) {
            bufferQ.add(b);
        }
        
        if (pendingBytes < 0) {
            pendingBytes = 0;
        }
        packetIndexNeeded++;
    }

    public synchronized void queue(SoundDataPacket packet) {
        if (packet == null) {
            DragNSounds.LOGGER.error("Sound streaming error. Data packet is null.");
            ClientSoundManager.stopSound(soundId);
        }        
        hasData = packet.isHasNext();
        queuedData.add(packet);
    }

    private void dequeue() {
        while (isStreaming) {
            while (!queuedData.isEmpty() && isStreaming) {
                try {
                    SoundDataPacket packet = queuedData.peek();
                    if (packet != null && hasSpace(packet.getData().length) && isPacketNeeded(packet.getIndex())) {
                        queuedData.remove(packet);
                        write(packet.getData(), packet.getIndex(), packet.isHasNext());
                        continue;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException e) { }
            }

            try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException e) { }
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        isStreaming = false;
        bufferQ.clear();
        SoundStreamHolder.close(soundId);
        DragNSounds.net().sendToServer(new StopSoundNotificationPacket(soundId));
        DragNSounds.LOGGER.info("Sound playback has been stopped. (ID " + soundId + ")");
    }
    
}
