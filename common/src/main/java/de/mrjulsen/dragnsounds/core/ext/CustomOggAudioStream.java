package de.mrjulsen.dragnsounds.core.ext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.audio.OggAudioStream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.util.Mth;

/**
 * Contains additional data and bugfixes.
 * @see OggAudioStream
 */
public class CustomOggAudioStream implements AudioStream {
    private long handle;
    private final AudioFormat audioFormat;
    private final InputStream input;
    private ByteBuffer buffer = MemoryUtil.memAlloc(8192);

    private final long soundId;

    public long getSoundId() {
        return soundId;
    }

    public CustomOggAudioStream(long soundId, InputStream input) throws IOException {
        this.input = input;
        this.soundId = soundId;
        this.buffer.limit(0);
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            IntBuffer intBuffer2 = memoryStack.mallocInt(1);
            while (this.handle == 0L) {
                if (!this.refillFromStream()) {
                    throw new IOException("Failed to find Ogg header");
                }
                int i = this.buffer.position();
                this.buffer.position(0);
                this.handle = STBVorbis.stb_vorbis_open_pushdata(this.buffer, intBuffer, intBuffer2, null);
                this.buffer.position(i);
                int j = intBuffer2.get(0);
                if (j == 1) {
                    this.forwardBuffer();
                    continue;
                }
                if (j == 0) continue;
                throw new IOException("Failed to read Ogg file " + j);
            }
            this.buffer.position(this.buffer.position() + intBuffer.get(0));
            STBVorbisInfo sTBVorbisInfo = STBVorbisInfo.mallocStack(memoryStack);
            STBVorbis.stb_vorbis_get_info(this.handle, sTBVorbisInfo);
            this.audioFormat = new AudioFormat(sTBVorbisInfo.sample_rate(), 16, sTBVorbisInfo.channels(), true, false);
        }
    }

    private boolean refillFromStream() throws IOException {
        int i = this.buffer.limit();
        int j = this.buffer.capacity() - i;
        if (j == 0) {
            return true;
        }
        byte[] bs = new byte[j];
        int k = this.input.read(bs);
        if (k == -1) {
            return false;
        }
        int l = this.buffer.position();
        this.buffer.limit(i + k);
        this.buffer.position(i);
        this.buffer.put(bs, 0, k);
        this.buffer.position(l);
        return true;
    }

    private void forwardBuffer() {
        boolean bl2;
        boolean bl = this.buffer.position() == 0;
        bl2 = this.buffer.position() == this.buffer.limit();
        if (bl2 && !bl) {
            this.buffer.position(0);
            this.buffer.limit(0);
        } else {
            ByteBuffer byteBuffer = MemoryUtil.memAlloc(this.buffer.capacity()); // FIXED RANDOM CRASH
            byteBuffer.put(this.buffer);
            MemoryUtil.memFree(this.buffer);
            byteBuffer.flip();
            this.buffer = byteBuffer;
        }
    }

    private boolean readFrame(OutputConcat output) throws IOException {
        if (this.handle == 0L) {
            return false;
        }
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            block14: {
                int k;
                PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
                IntBuffer intBuffer = memoryStack.mallocInt(1);
                IntBuffer intBuffer2 = memoryStack.mallocInt(1);
                while (true) {
                    int i = STBVorbis.stb_vorbis_decode_frame_pushdata(this.handle, this.buffer, intBuffer, pointerBuffer, intBuffer2);
                    this.buffer.position(this.buffer.position() + i);
                    int j = STBVorbis.stb_vorbis_get_error(this.handle);
                    if (j == 1) {
                        this.forwardBuffer();
                        if (this.refillFromStream()) continue;
                        break block14;
                    }
                    if (j != 0) {
                        throw new IOException("Failed to read Ogg file " + j);
                    }
                    k = intBuffer2.get(0);
                    if (k != 0) break;
                }
                int l = intBuffer.get(0);
                PointerBuffer pointerBuffer2 = pointerBuffer.getPointerBuffer(l);
                if (l == 1) {
                    this.convertMono(pointerBuffer2.getFloatBuffer(0, k), output);
                    boolean bl = true;
                    return bl;
                }
                if (l == 2) {
                    this.convertStereo(pointerBuffer2.getFloatBuffer(0, k), pointerBuffer2.getFloatBuffer(1, k), output);
                    boolean bl = true;
                    return bl;
                }
                throw new IllegalStateException("Invalid number of channels: " + l);
            }
            boolean bl = false;
            return bl;
        }
    }

    private void convertMono(FloatBuffer channel, OutputConcat output) {
        while (channel.hasRemaining()) {
            output.put(channel.get());
        }
    }

    private void convertStereo(FloatBuffer leftChannel, FloatBuffer rightChannel, OutputConcat output) {
        while (leftChannel.hasRemaining() && rightChannel.hasRemaining()) {
            output.put(leftChannel.get());
            output.put(rightChannel.get());
        }
    }

    @Override
    public void close() throws IOException {
        if (this.handle != 0L) {
            STBVorbis.stb_vorbis_close(this.handle);
            this.handle = 0L;
        }
        MemoryUtil.memFree(this.buffer);
        this.input.close();
    }

    @Override
    public AudioFormat getFormat() {
        return this.audioFormat;
    }

    @Override
    public ByteBuffer read(int size) throws IOException {
        OutputConcat outputConcat = new OutputConcat(size + 8192);
        while (this.readFrame(outputConcat) && outputConcat.byteCount < size) {
        }
        return outputConcat.get();
    }

    public ByteBuffer readAll() throws IOException {
        OutputConcat outputConcat = new OutputConcat(16384);
        while (this.readFrame(outputConcat)) {
        }
        return outputConcat.get();
    }

    @Environment(value=EnvType.CLIENT)
    static class OutputConcat {
        private final List<ByteBuffer> buffers = Lists.newArrayList();
        private final int bufferSize;
        int byteCount;
        private ByteBuffer currentBuffer;

        public OutputConcat(int size) {
            this.bufferSize = size + 1 & 0xFFFFFFFE;
            this.createNewBuffer();
        }

        private void createNewBuffer() {
            this.currentBuffer = BufferUtils.createByteBuffer(this.bufferSize);
        }

        public void put(float sample) {
            if (this.currentBuffer.remaining() == 0) {
                this.currentBuffer.flip();
                this.buffers.add(this.currentBuffer);
                this.createNewBuffer();
            }
            int i = Mth.clamp((int)(sample * 32767.5f - 0.5f), Short.MIN_VALUE, Short.MAX_VALUE);
            this.currentBuffer.putShort((short)i);
            this.byteCount += 2;
        }

        public ByteBuffer get() {
            this.currentBuffer.flip();
            if (this.buffers.isEmpty()) {
                return this.currentBuffer;
            }
            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(this.byteCount);
            this.buffers.forEach(byteBuffer::put);
            byteBuffer.put(this.currentBuffer);
            byteBuffer.flip();
            return byteBuffer;
        }
    }
}

