package de.mrjulsen.dragnsounds.core.data;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;

public class SoundCommandListener {
    private final Deque<Runnable> queue = new ArrayDeque<>();
    private boolean valid = true;
    private boolean paused = true;

    private SoundFile file;

    private final long soundId;

    public SoundCommandListener(long soundId) {
        this.soundId = soundId;
    }

    public void start(SoundFile file) {
        this.file = file;
        new Thread(DragNSounds.ASYNC_GROUP, () -> {
            while (valid) {

                while (!queue.isEmpty() && valid && !paused) {
                    queue.poll().run();
                }

                try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) { }
            }
        }, "Sound Command Listener " + soundId).start();
        resume();
    }

    public void queue(Runnable action) {
        queue.add(action);
    }

    public void queueFirst(Runnable action) {
        queue.addFirst(action);
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;
    }

    public void stop() {
        this.valid = false;
    }

    public SoundFile getSoundFile() {
        return file;
    }
}
