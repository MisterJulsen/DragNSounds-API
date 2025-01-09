package de.mrjulsen.dragnsounds.events;

import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import dev.architectury.event.events.client.ClientGuiEvent;

public class ClientEvents {
    public static void init() {
        ClientGuiEvent.DEBUG_TEXT_LEFT.register((content) -> {
            ClientSoundManager.printDebug(content);
        });        
    }
}
