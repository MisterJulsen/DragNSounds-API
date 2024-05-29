package de.mrjulsen.dragnsounds;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.core.data.filter.FileInfoFilter;
import de.mrjulsen.dragnsounds.core.data.filter.FileMetadataFilter;
import de.mrjulsen.dragnsounds.events.ClientEvents;
import de.mrjulsen.dragnsounds.events.ServerEvents;
import de.mrjulsen.dragnsounds.net.cts.AllMetadataRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.CancelUploadSoundPacket;
import de.mrjulsen.dragnsounds.net.cts.FinishUploadSoundPacket;
import de.mrjulsen.dragnsounds.net.cts.PlaySoundRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.RemoveMetadataPacket;
import de.mrjulsen.dragnsounds.net.cts.SoundCreatedResponsePacket;
import de.mrjulsen.dragnsounds.net.cts.SoundDataRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.SoundDeleteRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.SoundFileRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.SoundGetDataResponsePacket;
import de.mrjulsen.dragnsounds.net.cts.SoundListRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.SoundPlayingCheckResponsePacket;
import de.mrjulsen.dragnsounds.net.cts.StopSoundNotificationPacket;
import de.mrjulsen.dragnsounds.net.cts.UpdateMetadataPacket;
import de.mrjulsen.dragnsounds.net.cts.UploadSoundPacket;
import de.mrjulsen.dragnsounds.net.stc.AllMetadataResponsePacket;
import de.mrjulsen.dragnsounds.net.stc.PlaySoundPacket;
import de.mrjulsen.dragnsounds.net.stc.SoundDataPacket;
import de.mrjulsen.dragnsounds.net.stc.SoundDeleteResponsePacket;
import de.mrjulsen.dragnsounds.net.stc.SoundFileResponsePacket;
import de.mrjulsen.dragnsounds.net.stc.SoundListChunkResponsePacket;
import de.mrjulsen.dragnsounds.net.stc.SoundPlayingCheckPacket;
import de.mrjulsen.dragnsounds.net.stc.SoundUploadCommandPacket;
import de.mrjulsen.dragnsounds.net.stc.StopAllSoundsPacket;
import de.mrjulsen.dragnsounds.net.stc.StopSoundInstancesRequest;
import de.mrjulsen.dragnsounds.net.stc.StopSoundRequest;
import de.mrjulsen.dragnsounds.net.stc.UploadFailedPacket;
import de.mrjulsen.dragnsounds.net.stc.UploadProgressPacket;
import de.mrjulsen.dragnsounds.net.stc.UploadSuccessPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundConeDirectionPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundDopplerPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundGetDataRequestPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundPauseResumePacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundPositionPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundSeekPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundVolumePacket;
import de.mrjulsen.dragnsounds.registry.FilterRegistry;
import de.mrjulsen.dragnsounds.registry.ModCommands;
import de.mrjulsen.dragnsounds.test.ModItems;
import de.mrjulsen.mcdragonlib.net.NetworkManagerBase;
import dev.architectury.networking.NetworkChannel;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;

public final class DragNSounds {
    public static final String MOD_ID = "dragnsounds";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final int DEFAULT_NET_DATA_SIZE = 8192;
    public static final UUID ZERO_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static final ThreadGroup ASYNC_GROUP = new ThreadGroup("Async Tasks");

    private static NetworkManagerBase networkManager;

    public static void init() {
        ModItems.register();
        ModCommands.init();

        if (Platform.getEnvironment() == Env.CLIENT) {
            ClientSoundManager.init();
            ClientEvents.init();
        }
        ServerEvents.init();

        FilterRegistry.register(FileInfoFilter.class);
        FilterRegistry.register(FileMetadataFilter.class);

        networkManager = new NetworkManagerBase(MOD_ID, "blockbeats_network", List.of(
            // CTS
            CancelUploadSoundPacket.class,
            FinishUploadSoundPacket.class,
            PlaySoundRequestPacket.class,
            RemoveMetadataPacket.class,
            SoundDataRequestPacket.class,
            SoundFileRequestPacket.class,
            SoundListRequestPacket.class,
            StopSoundNotificationPacket.class,
            UpdateMetadataPacket.class,
            UploadSoundPacket.class,
            SoundPlayingCheckResponsePacket.class,
            AllMetadataRequestPacket.class,
            SoundDeleteRequestPacket.class,
            SoundCreatedResponsePacket.class,
            SoundGetDataResponsePacket.class,

            // STC
            PlaySoundPacket.class,
            SoundDataPacket.class,
            SoundFileResponsePacket.class,
            SoundListChunkResponsePacket.class,
            StopSoundRequest.class,
            StopSoundInstancesRequest.class,
            UploadFailedPacket.class,
            UploadProgressPacket.class,
            UploadSuccessPacket.class,
            SoundPlayingCheckPacket.class,
            AllMetadataResponsePacket.class,
            SoundDeleteResponsePacket.class,
            SoundUploadCommandPacket.class,
            StopAllSoundsPacket.class,

            SoundConeDirectionPacket.class,
            SoundDopplerPacket.class,
            SoundPositionPacket.class,
            SoundVolumePacket.class,
            SoundSeekPacket.class,
            SoundPauseResumePacket.class,
            SoundGetDataRequestPacket.class

        ));

        PlatformSpecific.registerConfig();
    }

    public static NetworkChannel net() {
        return networkManager.CHANNEL;
    }
}
