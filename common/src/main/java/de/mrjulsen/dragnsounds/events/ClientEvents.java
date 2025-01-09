package de.mrjulsen.dragnsounds.events;

import de.mrjulsen.dragnsounds.config.CommonConfig;
import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.platform.Platform;

public class ClientEvents {
    public static void init() {
        ClientGuiEvent.DEBUG_TEXT_LEFT.register((content) -> {
            if (CommonConfig.ADVANCED_LOGGING.get() || Platform.isDevelopmentEnvironment()) {
                ClientSoundManager.printDebug(content);
            }
        });        
    }
}
