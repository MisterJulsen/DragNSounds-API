package de.mrjulsen.dragnsounds.registry;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.filter.IFilter;
import net.minecraft.resources.ResourceLocation;

public class FilterRegistry {
    private static final Map<ResourceLocation, Class<? extends IFilter<?>>> registry = new HashMap<>();

    /**
     * Registers a custom filter type.
     * @param clazz The class of the custom filter.
     */
    public static void register(Class<? extends IFilter<?>> clazz) {
        try {
            IFilter<?> filter = clazz.getConstructor(String.class, String.class, ECompareOperation.class).newInstance("", "", ECompareOperation.EQUALS);
            registry.put(filter.getFilterId(), clazz);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            DragNSounds.LOGGER.error("Unable to generate filter class instance.", e);
        }
    }

    public static Optional<IFilter<?>> get(ResourceLocation location, String key, String value, ECompareOperation operation) {
        try {
            IFilter<?> filter = registry.get(location).getConstructor(String.class, String.class, ECompareOperation.class).newInstance(key, value, operation);
            return Optional.of(filter);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            DragNSounds.LOGGER.error("Unable to generate filter class instance.", e);
        }
        return Optional.empty();
    }
}
