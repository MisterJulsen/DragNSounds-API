package de.mrjulsen.dragnsounds.core.data;

import java.util.Arrays;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;

public enum ESoundType implements ITranslatableEnum {
    UI(0, "ui"),
    WORLD(1, "world");

    public int id;
    public String name;

    private ESoundType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public static ESoundType getById(int id) {
        return Arrays.stream(values()).filter(x -> x.getId() == id).findFirst().orElse(UI);
    }

    @Override
    public Data getTranslationData() {
        return new Data(DragNSounds.MOD_ID, "sound_type", name);
    }
    
}
