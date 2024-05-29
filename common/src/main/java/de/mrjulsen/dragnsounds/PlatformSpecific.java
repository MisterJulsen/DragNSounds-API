package de.mrjulsen.dragnsounds;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class PlatformSpecific {
    @ExpectPlatform
    public static void registerConfig() {
        throw new AssertionError();
    }
}
