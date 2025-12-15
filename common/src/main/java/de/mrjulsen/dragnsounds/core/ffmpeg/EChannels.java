package de.mrjulsen.dragnsounds.core.ffmpeg;

import java.util.Arrays;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;

public enum EChannels implements ITranslatableEnum {
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
    public Data getTranslationData() {
        return new Data(DragNSounds.MOD_ID, "audio_channels", name);
    }
    
}
