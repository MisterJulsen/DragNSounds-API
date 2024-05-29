package de.mrjulsen.dragnsounds.util;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;

public class ExtendedNBTUtils {
    public static CompoundTag saveMapToNBT(Map<String, String> map) {
        CompoundTag compound = new CompoundTag();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            compound.putString(entry.getKey(), entry.getValue());
        }

        return compound;
    }

    public static Map<String, String> loadMapFromNBT(CompoundTag compound) {
        Map<String, String> map = new HashMap<>();

        for (String key : compound.getAllKeys()) {
            map.put(key, compound.getString(key));
        }

        return map;
    }
}
