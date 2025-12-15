package de.mrjulsen.dragnsounds;

import java.util.UUID;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.core.data.filter.FileInfoFilter;
import de.mrjulsen.dragnsounds.core.data.filter.FileMetadataFilter;
import de.mrjulsen.dragnsounds.events.ClientEvents;
import de.mrjulsen.dragnsounds.events.ServerEvents;
import de.mrjulsen.dragnsounds.registry.FilterRegistry;
import de.mrjulsen.dragnsounds.registry.ModCommands;
import de.mrjulsen.dragnsounds.registry.ModNetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;

public final class DragNSounds {
    public static final String MOD_ID = "dragnsounds";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final int DEFAULT_NET_DATA_SIZE = 8192;
    public static final UUID ZERO_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static final ThreadGroup ASYNC_GROUP = new ThreadGroup("Async Tasks");
    public static final String DOCUMENTATION_UTL = "https://misterjulsen.github.io/DragNSounds-API/Mod/commands/";

    public static void init() {
        ModCommands.init();
        ModNetworkManager.init();

        if (Platform.getEnvironment() == Env.CLIENT) {
            ClientSoundManager.init();
            ClientEvents.init();
        }
        ServerEvents.init();

        FilterRegistry.register(FileInfoFilter.class);
        FilterRegistry.register(FileMetadataFilter.class);

        PlatformSpecific.registerConfig();
    }
    
    public static boolean hasServer() {
        return ServerEvents.getCurrentServer() != null;
    }
}
