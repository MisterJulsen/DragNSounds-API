package de.mrjulsen.dragnsounds.core.ffmpeg;

import java.io.File;

import net.minecraft.nbt.CompoundTag;
import ws.schild.jave.EncoderException;
import ws.schild.jave.InputFormatException;
import ws.schild.jave.MultimediaObject;

public record AudioSettings(EChannels channels, int bitRate, int samplingRate, byte quality) {

    private static final String NBT_CHANNELS = "Channels";
    private static final String NBT_BIT_RATE = "BitRate";
    private static final String NBT_SAMPLING_RATE = "SamplingRate";
    private static final String NBT_QUALITY = "Quality";

    public static AudioSettings getByFile(String path) throws InputFormatException, EncoderException {
        MultimediaObject obj = new MultimediaObject(new File(path));
        return new AudioSettings(
            EChannels.getByCount(obj.getInfo().getAudio().getChannels()),
            obj.getInfo().getAudio().getBitRate(),
            obj.getInfo().getAudio().getSamplingRate(),
            (byte)5
        );
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(NBT_CHANNELS, channels().getChannels());
        nbt.putInt(NBT_BIT_RATE, bitRate());
        nbt.putInt(NBT_SAMPLING_RATE, samplingRate());
        nbt.putByte(NBT_QUALITY, quality());
        return nbt;
    }

    public static AudioSettings fromNbt(CompoundTag nbt) {
        return new AudioSettings(
            EChannels.getByCount(nbt.getInt(NBT_CHANNELS)), 
            nbt.getInt(NBT_BIT_RATE), 
            nbt.getInt(NBT_SAMPLING_RATE), 
            nbt.getByte(NBT_QUALITY)
        );
    }

}
