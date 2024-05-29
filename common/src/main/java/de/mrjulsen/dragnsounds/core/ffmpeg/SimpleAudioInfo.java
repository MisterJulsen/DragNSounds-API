package de.mrjulsen.dragnsounds.core.ffmpeg;

import de.mrjulsen.mcdragonlib.data.INBTSerializable;
import net.minecraft.nbt.CompoundTag;
import ws.schild.jave.info.MultimediaInfo;

public class SimpleAudioInfo implements INBTSerializable {

    private final String NBT_DURATION = "Duration";
    private final String NBT_BIT_RATE = "BitRate";
    private final String NBT_CHANNELS = "Channels";
    private final String NBT_SAMPLE_RATE = "SampleRate";
    private final String NBT_BIT_DEPTH = "BitDepth";
    private final String NBT_DECODER = "Decoder";

    private long duration;
    private int bitRate;
    private EChannels channels;
    private int sampleRate;
    private String bitDepth;
    private String decoder;

    private SimpleAudioInfo() { }

    /**
     * 
     * @param info
     * @return
     */
    public static SimpleAudioInfo of(MultimediaInfo info) {
        SimpleAudioInfo n = new SimpleAudioInfo();
        n.duration = info.getDuration();
        n.bitRate = info.getAudio().getBitRate();
        n.sampleRate = info.getAudio().getSamplingRate();
        n.bitDepth = info.getAudio().getBitDepth();
        n.decoder = info.getAudio().getDecoder();
        n.channels = EChannels.getByCount(info.getAudio().getChannels());
        return n;
    }

    /**
     * @return The playback duration in milliseconds.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @return The playback duration in seconds.
     */
    public double getDurationSeconds() {
        return (double)getDuration() / 1000D;
    }

    /**
     * @return The playback duration in ticks.
     */
    public double getDurationTicks() {
        return (double)getDuration() / 50D;
    }

    public int getBitRate() {
        return bitRate;
    }

    public EChannels getChannels() {
        return channels;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public String getBitDepth() {
        return bitDepth;
    }

    public String getDecoder() {
        return decoder;
    }

    @Override
    public CompoundTag serializeNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong(NBT_DURATION, duration);
        nbt.putInt(NBT_BIT_RATE, bitRate);
        nbt.putInt(NBT_CHANNELS, channels.getChannels());
        nbt.putInt(NBT_SAMPLE_RATE, sampleRate);
        nbt.putString(NBT_DECODER, decoder);
        nbt.putString(NBT_BIT_DEPTH, bitDepth);
        return nbt;
    }

    @Override
    public void deserializeNbt(CompoundTag nbt) {
        duration = nbt.getLong(NBT_DURATION);
        bitRate = nbt.getInt(NBT_BIT_RATE);
        channels = EChannels.getByCount(nbt.getInt(NBT_CHANNELS));
        sampleRate = nbt.getInt(NBT_SAMPLE_RATE);
        bitDepth = nbt.getString(NBT_BIT_DEPTH);
        decoder = nbt.getString(NBT_DECODER);
    }
}
