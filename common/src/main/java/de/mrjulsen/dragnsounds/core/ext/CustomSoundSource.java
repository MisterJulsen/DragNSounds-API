package de.mrjulsen.dragnsounds.core.ext;

import java.util.Arrays;

import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;

public enum CustomSoundSource implements StringRepresentable {
    CUSTOM("custom");

    private String name;

    private CustomSoundSource(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CustomSoundSource getByName(String name) {
        return Arrays.stream(values()).filter(x -> x.getName().equals(name)).findFirst().orElse(CUSTOM);
    }

    public static SoundSource getSoundSourceByName(String name) {
        return Arrays.stream(SoundSource.values()).filter(x -> x.getName().equals(name)).findFirst().orElse(SoundSource.MASTER);
    }

    @Override
    public String getSerializedName() {
        return getName();
    }
    
}
