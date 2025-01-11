package de.mrjulsen.dragnsounds.core.ext;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

import de.mrjulsen.dragnsounds.DragNSounds;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.FloatSampleSource;

import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CustomJOrbisAudioStream implements FloatSampleSource, DSAudioStreamExt {
    private final SyncState syncState = new SyncState();
    private final Page page = new Page();
    private final StreamState streamState = new StreamState();
    private final Packet packet = new Packet();
    private final Info info = new Info();
    private final DspState dspState = new DspState();
    private final Block block;
    private final AudioFormat audioFormat;
    private final InputStream input;
    private long samplesWritten;
    private long totalSamplesInStream;

    private final long soundId;

    public long getSoundId() {
        return soundId;
    }

    public CustomJOrbisAudioStream(long soundId, InputStream input) throws IOException {
        this.soundId = soundId;
        this.block = new Block(this.dspState);
        this.totalSamplesInStream = Long.MAX_VALUE;
        this.input = input;
        Comment comment = new Comment();
        Page page = this.readPage();
        if (page == null) {
            throw new IOException("Invalid Ogg file - can't find first page");
        } else {
            Packet packet = this.readIdentificationPacket(page);
            if (isError(this.info.synthesis_headerin(comment, packet))) {
                throw new IOException("Invalid Ogg identification packet");
            } else {
                for (int i = 0; i < 2; ++i) {
                    packet = this.readPacket();
                    if (packet == null) {
                        throw new IOException("Unexpected end of Ogg stream");
                    }

                    if (isError(this.info.synthesis_headerin(comment, packet))) {
                        throw new IOException("Invalid Ogg header packet " + i);
                    }
                }

                this.dspState.synthesis_init(this.info);
                this.block.init(this.dspState);
                this.audioFormat = new AudioFormat((float) this.info.rate, 16, this.info.channels, true, false);
            }
        }
    }

    private static boolean isError(int value) {
        return value < 0;
    }

    public AudioFormat getFormat() {
        return this.audioFormat;
    }

    private boolean readToBuffer() throws IOException {
        int i = this.syncState.buffer(8192);
        byte[] bs = this.syncState.data;
        int j = this.input.read(bs, i, 8192);
        if (j == -1) {
            return false;
        } else {
            this.syncState.wrote(j);
            return true;
        }
    }

    @Nullable
    private Page readPage() throws IOException {
        while (true) {
            int i = this.syncState.pageout(this.page);
            switch (i) {
                case -1:
                DragNSounds.LOGGER.error("salz");
                    throw new IllegalStateException("Corrupt or missing data in bitstream");
                case 0:
                    if (this.readToBuffer()) {
                        break;
                    }
                    return null;
                case 1:
                    if (this.page.eos() != 0) {
                        this.totalSamplesInStream = this.page.granulepos();
                    }
                    return this.page;
                default:
                    throw new IllegalStateException("Unknown page decode result: " + i);
            }
        }
    }

    private Packet readIdentificationPacket(Page page) throws IOException {
        this.streamState.init(page.serialno());
        if (isError(this.streamState.pagein(page))) {
            throw new IOException("Failed to parse page");
        } else {
            int i = this.streamState.packetout(this.packet);
            if (i != 1) {
                throw new IOException("Failed to read identification packet: " + i);
            } else {
                return this.packet;
            }
        }
    }

    @Nullable
    private Packet readPacket() throws IOException {
        while (true) {
            int i = this.streamState.packetout(this.packet);
            switch (i) {
                case -1:
                    throw new IOException("Failed to parse packet");
                case 0:
                    Page page = this.readPage();
                    if (page == null) {
                        return null;
                    }

                    if (!isError(this.streamState.pagein(page))) {
                        break;
                    }

                    throw new IOException("Failed to parse page");
                case 1:
                    return this.packet;
                default:
                    throw new IllegalStateException("Unknown packet decode result: " + i);
            }
        }
    }

    private long getSamplesToWrite(int samples) {
        long l = this.samplesWritten + (long) samples;
        long m;
        if (l > this.totalSamplesInStream) {
            m = this.totalSamplesInStream - this.samplesWritten;
            this.samplesWritten = this.totalSamplesInStream;
        } else {
            this.samplesWritten = l;
            m = (long) samples;
        }

        return m;
    }

    public boolean readChunk(FloatConsumer output) throws IOException {
        float[][][] fs = new float[1][][];
        int[] is = new int[this.info.channels];
        Packet packet = this.readPacket();
        if (packet == null) {
            return false;
        } else if (isError(this.block.synthesis(packet))) {
            throw new IOException("Can't decode audio packet");
        } else {
            this.dspState.synthesis_blockin(this.block);

            int i;
            for (; (i = this.dspState.synthesis_pcmout(fs, is)) > 0; this.dspState.synthesis_read(i)) {
                float[][] gs = fs[0];
                long l = this.getSamplesToWrite(i);
                switch (this.info.channels) {
                    case 1:
                        copyMono(gs[0], is[0], l, output);
                        break;
                    case 2:
                        copyStereo(gs[0], is[0], gs[1], is[1], l, output);
                        break;
                    default:
                        copyAnyChannels(gs, this.info.channels, is, l, output);
                }
            }

            return true;
        }
    }

    private static void copyAnyChannels(float[][] source, int channels, int[] startIndexes, long samplesToWrite, FloatConsumer output) {
        for (int i = 0; (long) i < samplesToWrite; ++i) {
            for (int j = 0; j < channels; ++j) {
                int k = startIndexes[j];
                float f = source[j][k + i];
                output.accept(f);
            }
        }

    }

    private static void copyMono(float[] source, int startIndex, long samplesToWrite, FloatConsumer output) {
        for (int i = startIndex; (long) i < (long) startIndex + samplesToWrite; ++i) {
            output.accept(source[i]);
        }

    }

    private static void copyStereo(float[] leftSource, int leftStartIndex, float[] rightSource, int rightStartIndex,
            long samplesToWrite, FloatConsumer output) {
        for (int i = 0; (long) i < samplesToWrite; ++i) {
            output.accept(leftSource[leftStartIndex + i]);
            output.accept(rightSource[rightStartIndex + i]);
        }

    }

    public void close() throws IOException {
        this.input.close();
    }
}
