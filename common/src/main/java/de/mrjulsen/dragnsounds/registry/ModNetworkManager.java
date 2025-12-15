package de.mrjulsen.dragnsounds.registry;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.net.cts.AllMetadataRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.CancelUploadSoundPacket;
import de.mrjulsen.dragnsounds.net.stc.AllMetadataResponsePacket;
import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketType;
import de.mrjulsen.mcdragonlib.util.DLUtils;

import de.mrjulsen.dragnsounds.net.stc.UploadSuccessPacket;
import de.mrjulsen.dragnsounds.net.stc.UploadProgressPacket;
import de.mrjulsen.dragnsounds.net.stc.UploadFailedPacket;
import de.mrjulsen.dragnsounds.net.stc.StopSoundRequest;
import de.mrjulsen.dragnsounds.net.stc.StopSoundInstancesRequest;
import de.mrjulsen.dragnsounds.net.stc.StopAllSoundsPacket;
import de.mrjulsen.dragnsounds.net.stc.StartUploadResponsePacket;
import de.mrjulsen.dragnsounds.net.stc.SoundUploadCommandPacket;
import de.mrjulsen.dragnsounds.net.stc.SoundPlayingCheckPacket;
import de.mrjulsen.dragnsounds.net.stc.SoundListChunkResponsePacket;
import de.mrjulsen.dragnsounds.net.stc.SoundFileResponsePacket;
import de.mrjulsen.dragnsounds.net.stc.SoundDeleteResponsePacket;
import de.mrjulsen.dragnsounds.net.stc.SoundDataPacket;
import de.mrjulsen.dragnsounds.net.stc.PrintDebugPacket;
import de.mrjulsen.dragnsounds.net.stc.PlaySoundPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundVolumePacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundSeekPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundPositionPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundPauseResumePacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundGetDataRequestPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundDopplerPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundConeDirectionPacket;

import de.mrjulsen.dragnsounds.net.cts.UploadSoundPacket;
import de.mrjulsen.dragnsounds.net.cts.UpdateMetadataPacket;
import de.mrjulsen.dragnsounds.net.cts.StopSoundNotificationPacket;
import de.mrjulsen.dragnsounds.net.cts.StartUploadSoundPacket;
import de.mrjulsen.dragnsounds.net.cts.SoundPlayingCheckResponsePacket;
import de.mrjulsen.dragnsounds.net.cts.SoundListRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.SoundGetDataResponsePacket;
import de.mrjulsen.dragnsounds.net.cts.SoundFileRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.SoundDeleteRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.SoundDataRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.SoundCreatedResponsePacket;
import de.mrjulsen.dragnsounds.net.cts.RemoveMetadataPacket;
import de.mrjulsen.dragnsounds.net.cts.PlaySoundRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.FinishUploadSoundPacket;

public class ModNetworkManager {
    
    public static final DLNetworkManager NETWORK = new DLNetworkManager(DLUtils.resourceLocation(DragNSounds.MOD_ID, "network"), "1");

    public static final NetworkPacketType.Send<NetworkDirection.C2S, AllMetadataRequestPacket> REQUEST_ALL_METADATA = NETWORK.registerSendOnlyPacket("request_all_metadata", NetworkDirection.C2S, AllMetadataRequestPacket::handle, AllMetadataRequestPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, CancelUploadSoundPacket> CANCEL_UPLOAD_SOUND = NETWORK.registerSendOnlyPacket("cancel_upload_sound", NetworkDirection.C2S, CancelUploadSoundPacket::handle, CancelUploadSoundPacket::new);
    
    public static final NetworkPacketType.Send<NetworkDirection.S2C, AllMetadataResponsePacket> RESPONSE_ALL_METADATA = NETWORK.registerSendOnlyPacket("response_all_metadata", NetworkDirection.S2C, AllMetadataResponsePacket::handle, AllMetadataResponsePacket::new);

    // S2C packets
    public static final NetworkPacketType.Send<NetworkDirection.S2C, UploadSuccessPacket> UPLOAD_SUCCESS = NETWORK.registerSendOnlyPacket("upload_success", NetworkDirection.S2C, UploadSuccessPacket::handle, UploadSuccessPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, UploadProgressPacket> UPLOAD_PROGRESS = NETWORK.registerSendOnlyPacket("upload_progress", NetworkDirection.S2C, UploadProgressPacket::handle, UploadProgressPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, UploadFailedPacket> UPLOAD_FAILED = NETWORK.registerSendOnlyPacket("upload_failed", NetworkDirection.S2C, UploadFailedPacket::handle, UploadFailedPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, StopSoundRequest> STOP_SOUND_REQUEST = NETWORK.registerSendOnlyPacket("stop_sound_request", NetworkDirection.S2C, StopSoundRequest::handle, StopSoundRequest::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, StopSoundInstancesRequest> STOP_SOUND_INSTANCES = NETWORK.registerSendOnlyPacket("stop_sound_instances", NetworkDirection.S2C, StopSoundInstancesRequest::handle, StopSoundInstancesRequest::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, StopAllSoundsPacket> STOP_ALL_SOUNDS = NETWORK.registerSendOnlyPacket("stop_all_sounds", NetworkDirection.S2C, StopAllSoundsPacket::handle, StopAllSoundsPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, StartUploadResponsePacket> START_UPLOAD_RESPONSE = NETWORK.registerSendOnlyPacket("start_upload_response", NetworkDirection.S2C, StartUploadResponsePacket::handle, StartUploadResponsePacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, SoundUploadCommandPacket> SOUND_UPLOAD_COMMAND = NETWORK.registerSendOnlyPacket("sound_upload_command", NetworkDirection.S2C, SoundUploadCommandPacket::handle, SoundUploadCommandPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, SoundPlayingCheckPacket> SOUND_PLAYING_CHECK = NETWORK.registerSendOnlyPacket("sound_playing_check", NetworkDirection.S2C, SoundPlayingCheckPacket::handle, SoundPlayingCheckPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, SoundListChunkResponsePacket> SOUND_LIST_CHUNK_RESPONSE = NETWORK.registerSendOnlyPacket("sound_list_chunk_response", NetworkDirection.S2C, SoundListChunkResponsePacket::handle, SoundListChunkResponsePacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, SoundFileResponsePacket> SOUND_FILE_RESPONSE = NETWORK.registerSendOnlyPacket("sound_file_response", NetworkDirection.S2C, SoundFileResponsePacket::handle, SoundFileResponsePacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, SoundDeleteResponsePacket> SOUND_DELETE_RESPONSE = NETWORK.registerSendOnlyPacket("sound_delete_response", NetworkDirection.S2C, SoundDeleteResponsePacket::handle, SoundDeleteResponsePacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, SoundDataPacket> SOUND_DATA = NETWORK.registerSendOnlyPacket("sound_data", NetworkDirection.S2C, SoundDataPacket::handle, SoundDataPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, PrintDebugPacket> PRINT_DEBUG = NETWORK.registerSendOnlyPacket("print_debug", NetworkDirection.S2C, PrintDebugPacket::handle, PrintDebugPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, PlaySoundPacket> PLAY_SOUND = NETWORK.registerSendOnlyPacket("play_sound", NetworkDirection.S2C, PlaySoundPacket::handle, PlaySoundPacket::new);

    // S2C modify packets
    public static final NetworkPacketType.Send<NetworkDirection.S2C, SoundVolumePacket> SOUND_VOLUME = NETWORK.registerSendOnlyPacket("sound_volume", NetworkDirection.S2C, SoundVolumePacket::handle, SoundVolumePacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, SoundSeekPacket> SOUND_SEEK = NETWORK.registerSendOnlyPacket("sound_seek", NetworkDirection.S2C, SoundSeekPacket::handle, SoundSeekPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, SoundPositionPacket> SOUND_POSITION = NETWORK.registerSendOnlyPacket("sound_position", NetworkDirection.S2C, SoundPositionPacket::handle, SoundPositionPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, SoundPauseResumePacket> SOUND_PAUSE_RESUME = NETWORK.registerSendOnlyPacket("sound_pause_resume", NetworkDirection.S2C, SoundPauseResumePacket::handle, SoundPauseResumePacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, SoundGetDataRequestPacket> SOUND_GET_DATA_REQUEST = NETWORK.registerSendOnlyPacket("sound_get_data_request", NetworkDirection.S2C, SoundGetDataRequestPacket::handle, SoundGetDataRequestPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, SoundDopplerPacket> SOUND_DOPPLER = NETWORK.registerSendOnlyPacket("sound_doppler", NetworkDirection.S2C, SoundDopplerPacket::handle, SoundDopplerPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.S2C, SoundConeDirectionPacket> SOUND_CONE_DIRECTION = NETWORK.registerSendOnlyPacket("sound_cone_direction", NetworkDirection.S2C, SoundConeDirectionPacket::handle, SoundConeDirectionPacket::new);

    // C2S packets
    public static final NetworkPacketType.Send<NetworkDirection.C2S, UploadSoundPacket> UPLOAD_SOUND = NETWORK.registerSendOnlyPacket("upload_sound", NetworkDirection.C2S, UploadSoundPacket::handle, UploadSoundPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, UpdateMetadataPacket> UPDATE_METADATA = NETWORK.registerSendOnlyPacket("update_metadata", NetworkDirection.C2S, UpdateMetadataPacket::handle, UpdateMetadataPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, StopSoundNotificationPacket> STOP_SOUND_NOTIFICATION = NETWORK.registerSendOnlyPacket("stop_sound_notification", NetworkDirection.C2S, StopSoundNotificationPacket::handle, StopSoundNotificationPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, StartUploadSoundPacket> START_UPLOAD_SOUND = NETWORK.registerSendOnlyPacket("start_upload_sound", NetworkDirection.C2S, StartUploadSoundPacket::handle, StartUploadSoundPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, SoundPlayingCheckResponsePacket> SOUND_PLAYING_CHECK_RESPONSE = NETWORK.registerSendOnlyPacket("sound_playing_check_response", NetworkDirection.C2S, SoundPlayingCheckResponsePacket::handle, SoundPlayingCheckResponsePacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, SoundListRequestPacket> SOUND_LIST_REQUEST = NETWORK.registerSendOnlyPacket("sound_list_request", NetworkDirection.C2S, SoundListRequestPacket::handle, SoundListRequestPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, SoundGetDataResponsePacket> SOUND_GET_DATA_RESPONSE = NETWORK.registerSendOnlyPacket("sound_get_data_response", NetworkDirection.C2S, SoundGetDataResponsePacket::handle, SoundGetDataResponsePacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, SoundFileRequestPacket> SOUND_FILE_REQUEST = NETWORK.registerSendOnlyPacket("sound_file_request", NetworkDirection.C2S, SoundFileRequestPacket::handle, SoundFileRequestPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, SoundDeleteRequestPacket> SOUND_DELETE_REQUEST = NETWORK.registerSendOnlyPacket("sound_delete_request", NetworkDirection.C2S, SoundDeleteRequestPacket::handle, SoundDeleteRequestPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, SoundDataRequestPacket> SOUND_DATA_REQUEST = NETWORK.registerSendOnlyPacket("sound_data_request", NetworkDirection.C2S, SoundDataRequestPacket::handle, SoundDataRequestPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, SoundCreatedResponsePacket> SOUND_CREATED_RESPONSE = NETWORK.registerSendOnlyPacket("sound_created_response", NetworkDirection.C2S, SoundCreatedResponsePacket::handle, SoundCreatedResponsePacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, RemoveMetadataPacket> REMOVE_METADATA = NETWORK.registerSendOnlyPacket("remove_metadata", NetworkDirection.C2S, RemoveMetadataPacket::handle, RemoveMetadataPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, PlaySoundRequestPacket> PLAY_SOUND_REQUEST = NETWORK.registerSendOnlyPacket("play_sound_request", NetworkDirection.C2S, PlaySoundRequestPacket::handle, PlaySoundRequestPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, FinishUploadSoundPacket> FINISH_UPLOAD_SOUND = NETWORK.registerSendOnlyPacket("finish_upload_sound", NetworkDirection.C2S, FinishUploadSoundPacket::handle, FinishUploadSoundPacket::new);
    // CancelUploadSoundPacket already registered above as CANCEL_UPLOAD_SOUND


    public static void init() {
    }
}
