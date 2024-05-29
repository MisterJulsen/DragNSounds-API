package de.mrjulsen.dragnsounds.util;

public interface ISelfCast<T> {
    @SuppressWarnings("unchecked")
    default T self() {
        return (T)(Object)this;
    }
}
