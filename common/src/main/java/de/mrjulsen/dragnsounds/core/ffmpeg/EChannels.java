package de.mrjulsen.dragnsounds.core.ffmpeg;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum EChannels implements StringRepresentable, ITranslatableEnum {
    MONO(1, "mono"),
    STEREO(2, "stereo");

    private int channels;
    private String name;

    private EChannels(int channels, String name) {
        this.channels = channels;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getChannels() {
        return channels;
    }

    public static EChannels getByCount(int count) {
        return Arrays.stream(values()).filter(x -> x.getChannels() == count).findFirst().orElse(STEREO);
    }

    public static EChannels getByNameUnsafe(String name) {
        return Arrays.stream(values()).filter(x -> x.getName().equals(name)).findFirst().get();
    }
        
    @Override
    public String getSerializedName() {
        return getName();
    }

    @Override
    public String getEnumName() {
        return "audio_channels";
    }

    @Override
    public String getEnumValueName() {
        return getName();
    }
    
}
