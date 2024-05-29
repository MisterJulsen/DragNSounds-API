package de.mrjulsen.dragnsounds.core.data.filter;

import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import net.minecraft.resources.ResourceLocation;

public interface IFilter<T> {
    /**
     * @return The unique id of the filter implementation.
     */
    ResourceLocation getFilterId();
    /**
     * @return The compare operation provided with the data packet.
     */
    ECompareOperation compareOperation();
    /**
     * @return The key to search for in the target object.
     */
    String key();
    /**
     * The value to search for in the target object.
     * @return
     */
    String value();
    /**
     * The compare function.
     * @param obj The object to check.
     * @return {@code true} if the given object contains the key and value according to the compare operation.
     */
    boolean isValid(T obj);
}
