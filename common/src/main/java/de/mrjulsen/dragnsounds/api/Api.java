package de.mrjulsen.dragnsounds.api;

/**
 * General utilities.
 */
public final class Api {

    /**
     * Generates a new long id from the System's nano time value.
     */
    public static final long id() {
        return System.nanoTime();
    }
}
