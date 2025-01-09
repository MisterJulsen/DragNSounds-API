package de.mrjulsen.dragnsounds.core.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A thread-safe implementation of an InputStream that allows data to be written
 * to it at any time.
 */
public class WritableInputStream extends InputStream {

    private final BlockingQueue<Byte> buffer;
    private volatile boolean closed;

    public WritableInputStream() {
        this.buffer = new LinkedBlockingQueue<>();
        this.closed = false;
    }

    /**
     * Writes data to the stream. This method can be called from another thread.
     *
     * @param data The data to write to the stream.
     * @throws IOException If the stream is closed.
     */
    public void write(byte[] data) throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }
        for (byte b : data) {
            buffer.add(b);
        }
    }
    
    public void write(byte[] data, int len) throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }

        for (int i = 0; i < data.length && i < len; i++) {
            buffer.add(data[i]);

        }
    }

    /**
     * Closes the stream, signaling that no more data will be written.
     */
    @Override
    public void close() {
        closed = true;
    }

    /**
     * Reads the next byte of data from the stream. If no data is available, this
     * method blocks until data is written or the stream is closed.
     *
     * @return The next byte of data, or -1 if the stream is closed and no more
     *         data is available.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        try {
            while (true) {
                if (closed && buffer.isEmpty()) {
                    return -1; // End of stream
                }
                Byte b = buffer.poll();
                if (b != null) {
                    return b & 0xFF; // Convert byte to int
                }
                Thread.sleep(10); // Avoid busy waiting
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Thread was interrupted", e);
        }
    }

    /**
     * Reads up to len bytes of data from the stream into an array of bytes.
     *
     * @param b   The buffer into which the data is read.
     * @param off The start offset in array b at which the data is written.
     * @param len The maximum number of bytes to read.
     * @return The total number of bytes read into the buffer, or -1 if the stream
     *         is closed and no more data is available.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int bytesRead = 0;
        for (int i = 0; i < len; i++) {
            int nextByte = read();
            if (nextByte == -1) {
                return bytesRead == 0 ? -1 : bytesRead;
            }
            b[off + i] = (byte) nextByte;
            bytesRead++;
        }
        return bytesRead;
    }

    @Override
    public int available() throws IOException {
        return buffer.size();
    }
}
