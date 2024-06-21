package de.mrjulsen.dragnsounds.api;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ClientInstanceManager;
import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundChannelsHolder;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundDeleteCallback;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundMetadataCallback;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundUploadCancelCallback;
import de.mrjulsen.dragnsounds.core.data.ChannelContext;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.PlaybackConfig;
import de.mrjulsen.dragnsounds.core.data.SoundPlaybackData;
import de.mrjulsen.dragnsounds.core.data.filter.FileInfoFilter;
import de.mrjulsen.dragnsounds.core.data.filter.IFilter;
import de.mrjulsen.dragnsounds.core.ffmpeg.AudioSettings;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.net.cts.AllMetadataRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.PlaySoundRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.SoundDeleteRequestPacket;
import de.mrjulsen.dragnsounds.registry.FilterRegistry;
import de.mrjulsen.dragnsounds.util.SoundUtils;
import de.mrjulsen.mcdragonlib.data.StatusResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

/**
 * Contains useful methods to play and manipulate custom sounds on the client side. For server-side sound management see {@code ServerApi}.
 * @see ServerApi
 */
public final class ClientApi {

    /**
     * Send a {@code playSound} request to the server for the given custom sound.
     * @param file Information about the sound file. Do not create your own instance of {@code SoundFile}. Use {@code getAllSoundFiles()} instead.
     * @param playback Playback configuration with information such as position, volume, pitch and more.
     * @param responseCallback Called after the server responds with the sound id.
     * @return The id of the request. (NOT the id of the sound!)
     */
    public static long playSound(SoundFile file, PlaybackConfig playback, Consumer<Long> responseCallback) {
        long requestId = ClientInstanceManager.addSoundRequestCallback(responseCallback);
        DragNSounds.net().sendToServer(new PlaySoundRequestPacket(requestId, file, playback));
        return requestId;
    }

    /**
     * Returns the current sound channel instance of the given sound.
     * @param soundId The id of the custom sound.
     * @return An optional containing the {@code ChannelContext} if available.
     */
    public static Optional<ChannelContext> getSound(long soundId) {
        return SoundChannelsHolder.has(soundId) ? Optional.ofNullable(SoundChannelsHolder.get(soundId)) : Optional.empty();
    }

    /**
     * Enable the doppler effect for the given sound if the custom sound is available. If not, nothing happens.
     * @param soundId The id of the cusotm sound.
     * @param dopplerValue The doppler factor.
     * @param velocity The velocity of the sound source.
     */
    public static void setDoppler(long soundId, float dopplerValue, Vec3 velocity) {
        ClientSoundManager.setDoppler(soundId, dopplerValue, velocity);
    }

    /**
     * Define the cone in which the sound can be heard. Nothing happens if the given sound does not exist.
     * @param soundId The id of the cusotm sound.
     * @param direction The doppler factor.
     * @param angleA The first angle of the cone.
     * @param angleB The second angle of the cone.
     * @param outerGain The gain at the outside of the cone.
     */
    public static void setCone(long soundId, Vec3 direction, float angleA, float angleB, float outerGain) {
        ClientSoundManager.setCone(soundId, direction, angleA, angleB, outerGain);
    }    

    /**
     * Set the direction of the sound source. Nothing happens if the sound does not exist.
     * @param soundId The id of the custom sound.
     * @param direction The direction of the sound source.
     */
    public static void setDirection(long soundId, Vec3 direction) {
        ClientSoundManager.setDirection(soundId, direction);
    }

    /**
     * Changes the volume of the sound.
     * @param soundId The id of the custom sound.
     * @param volume The new volume.
     */
    public static void setVolume(long soundId, float volume) {
        ClientSoundManager.setVolume(soundId, volume);
    }

    /**
     * Changes the pitch of the sound.
     * @param soundId The id of the custom sound.
     * @param pitch The new pitch.
     */
    public static void setPitch(long soundId, float pitch) {
        ClientSoundManager.setPitch(soundId, pitch);
    }

    /**
     * Changes the attenuation distance of the sound.
     * @param soundId The id of the custom sound.
     * @param distance The new distance.
     */
    public static void setAttenuationDistance(long soundId, float distance) {
        ClientSoundManager.setAttenuationDistance(soundId, distance);
    }
    
    /**
     * Changes the position of the sound.
     * @param soundId The id of the custom sound.
     * @param pos The new position.
     */
    public static void setPosition(long soundId, Vec3 pos) {
        ClientSoundManager.setPosition(soundId, pos);
    }

    /**
     * Checks if the sound is still playing.
     * @param soundId The id of the custom sound.
     * @return {@code true} when a sound with the given id is playing.
     */
    public static boolean isPlaying(long soundId) {
        return SoundChannelsHolder.has(soundId);
    }

    /**
     * Checks if the sound is still playing.
     * @param file The custom sound file object.
     * @return {@code true} when a instance of this sound is playing.
     */
    public static boolean isAnyPlaying(SoundFile file) {
        return ClientInstanceManager.isAnyInstanceOfSoundPlaying(file);
    }

    /**
     * Starts playing the sound at the given number of ticks.
     * @param soundId The id of the custom sound.
     * @param ticks The amount of time in ticks to skip.
     */
    public static void seek(long soundId, int ticks) {
        ClientSoundManager.seek(soundId, ticks);
    }

    /**
     * Gets the sound playback data for the given sound id.
     * @param soundId The sound id to get the data from.
     * @return The playback data if available.
     */
    public static Optional<SoundPlaybackData> getPlaybackData(long soundId) {
        return Optional.ofNullable(ClientSoundManager.getData(soundId));
    }

    /**
     * Stops the currently playing sound. This method will also send a notification to the server.
     * @param soundId The id of the custom sound.
     */
    public static void stopSound(long soundId) {
        ClientSoundManager.stopSound(soundId);
    }
    
    /**
     * Stops all playing instances the given sound. This method will also send a notification to the server.
     * @param file The sound file object of the sound.
     */
    public static void stopAllSoundInstances(SoundFile file) {
        ClientSoundManager.stopAllSoundInstances(file);
    }

    /**
     * Converts the given sound file into the {@code ogg} format and uploads it to the server.
     * @param srcFilePath The path to the source audio file.
     * @param builder A Builder to define how the sound file should look like on the server (contains information such as the location, the name and more)
     * @param settings Settings for the sound conversion. 
     * @param callback Called after the server returns that the upload was successfull.
     * @param progress Called when the progress changes. The first value is the client-side progress, the second value is the server-side progress. Both values may differ a bit.
     * @param error Called when an error occurs, either on the client or the server side.or if the upload was cancelled.
     * @return The id of the upload. Use it to cancel the upload.
     */
    public static long uploadSound(String srcFilePath, SoundFile.Builder builder, AudioSettings settings, Consumer<Optional<SoundFile>> callback, BiConsumer<UploadProgress, UploadProgress> progress, Consumer<StatusResult> error) {
        return ClientSoundManager.uploadSound(srcFilePath, builder, settings, callback, progress, error);
    } 

    /**
     * Checks if the upload can be cancelled at this moment.
     * @param uploadId The id of the upload.
     * @return {@code true} if the upload can be cancelled.
     */
    public static boolean canCancelUpload(long uploadId) {
        return SoundUploadCancelCallback.canCancel(uploadId);
    }

    /**
     * Cancels an active file upload. Does nothing if the upload cannot be cancelled at the moment.
     * @param uploadId The id of the upload.
     * @return {@code true} if the cancellation was successfull.
     */
    public static boolean cancelUpload(long uploadId) {
        if (canCancelUpload(uploadId)) {
            return SoundUploadCancelCallback.cancelRun(uploadId);
        }
        return false;
    }

    /**
     * Get a list of all sound files.
     * @param filters Filter for the result.
     * @param callback Contains the response of the server.
     * @see FilterRegistry
     * @implNote All used custom filters MUST be registered in the {@code FilterRegistry} first!
     */
    public static void getAllSoundFiles(Collection<IFilter<SoundFile>> filters, Consumer<SoundFile[]> callback) {
        ClientSoundManager.getAllSoundFiles(filters, callback);
    }

    /**
     * Get a list of all sound files at the given location.
     * @param location The location to get the sounds from.
     * @param filters Filter for the result.
     * @param callback Contains the response of the server.
     * @see FilterRegistry
     * @implNote All used custom filters MUST be registered in the {@code FilterRegistry} first!
     */
    public static void getSoundFilesAt(SoundLocation location, Collection<IFilter<SoundFile>> filters, Consumer<SoundFile[]> callback) {
        Collection<IFilter<SoundFile>> filtersColl = filters;
        filtersColl.add(new FileInfoFilter(FileInfoFilter.KEY_LOCATION, location.toString(), ECompareOperation.EQUALS));
        ClientSoundManager.getAllSoundFiles(filtersColl, callback);
    }

    /**
     * Get a {@code SoundFile} from the server at the given location.
     * @param location The location where the sound is saved at.
     * @param id The id of the custom sound.
     * @param callback Contains the response of the server.
     */
    public static void getSoundFile(SoundLocation location, String id, Consumer<Optional<SoundFile>> callback) {
        ClientSoundManager.getSoundFile(location, id, callback);
    }

    /**
     * Deletes the sound at the given location.
     * @param location The location where the sound is saved at.
     * @param id The id of the custom sound.
     * @param callback Called after the server has responded.
     */
    public static void deleteSound(SoundLocation location, String id, Consumer<StatusResult> callback) {
        final long requestId = SoundDeleteCallback.create(callback);
        DragNSounds.net().sendToServer(new SoundDeleteRequestPacket(requestId, location, id));
    }

    /**
     * Deletes the sound at the given location.
     * @param file The sound file to delete.
     * @param callback Called after the server has responded.
     */
    public static void deleteSound(SoundFile file, Consumer<StatusResult> callback) {
        deleteSound(file.getLocation(), file.getId(), callback);
    }

    /**
     * Reads all metadata from the sound file.
     * @param file The custom sound file.
     * @param callback Contains all the metadata from the server response.
     */
    public static void getFileMetadata(SoundFile file, Consumer<Map<String, String>> callback) {
        final long requestId = SoundMetadataCallback.create(callback);
        DragNSounds.net().sendToServer(new AllMetadataRequestPacket(requestId, file));
    }

    /**
     * Opens a file dialog to select one or more audio files which can then be uploaded to the server.
     * @param multiselect Whether one or more files can be selected at once. Please note that only one file can be uploaded at once!
     * @param callback Called after the user has closed the dialog. If the user selected some files, their paths are then available. Otherwise the {@code Optional} is empty.
     */
    public static void showFileDialog(boolean multiselect, Consumer<Optional<Path[]>> callback) {
        SoundUtils.showUploadDialog(multiselect, callback);
    }

    public static final record UploadProgress(double progress, UploadState state) {

        private static final String NBT_PROGRESS = "Progress";
        private static final String NBT_STATE = "State";

        public CompoundTag toNbt() {
            CompoundTag nbt = new CompoundTag();
            nbt.putDouble(NBT_PROGRESS, progress);
            nbt.putInt(NBT_STATE, state.getIndex());

            return nbt;
        }

        public static UploadProgress fromNbt(CompoundTag nbt) {
            return new UploadProgress(
                nbt.getDouble(NBT_PROGRESS),
                UploadState.getByIndex(nbt.getInt(NBT_STATE))
            );
        }
    }

    public static enum UploadState {
        CONVERT(0),
        UPLOAD(1);

        private int index;

        private UploadState(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public static UploadState getByIndex(int index) {
            return Arrays.stream(values()).filter(x -> x.getIndex() == index).findFirst().orElse(CONVERT);
        }
    }
}
