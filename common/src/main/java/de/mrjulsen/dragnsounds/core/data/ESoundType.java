package de.mrjulsen.dragnsounds.core.data;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum ESoundType implements StringRepresentable, ITranslatableEnum {
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
    public String getEnumName() {
        return "sound_type";
    }

    @Override
    public String getEnumValueName() {
        return getName();
    }

    @Override
    public String getSerializedName() {
        return getName();
    }
    
}
