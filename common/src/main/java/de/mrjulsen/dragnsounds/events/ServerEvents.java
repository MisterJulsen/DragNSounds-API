package de.mrjulsen.dragnsounds.events;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.commands.CustomSoundCommand;
import de.mrjulsen.dragnsounds.commands.StatusCommand;
import de.mrjulsen.dragnsounds.config.CommonConfig;
import de.mrjulsen.dragnsounds.core.ServerInstanceManager;
import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.MinecraftServer;

public class ServerEvents {
    private static MinecraftServer currentServer;

	public static MinecraftServer getCurrentServer() {
		return currentServer;
	}

    public static void init() {
        LifecycleEvent.SERVER_STARTED.register(server -> {
            if (CommonConfig.AUTO_CLEANUP.get()) {
                ServerSoundManager.cleanUp(server.overworld(), true);
            }
		});

        LifecycleEvent.SERVER_STARTING.register((server) -> {
            DragNSounds.LOGGER.info("Stored current server instance.");
			currentServer = server;
        });
        
        LifecycleEvent.SERVER_STOPPING.register((server) -> {
            ServerInstanceManager.clearCallbacks();
            currentServer = null;
        });

        PlayerEvent.PLAYER_QUIT.register((player) -> {
            ServerSoundManager.closeOnDisconnect(player);
        });

        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            CustomSoundCommand.register(dispatcher, selection);
            StatusCommand.register(dispatcher, selection);
            DragNSounds.LOGGER.info("Custom commands registered.");
        });
    }
}
