package de.mrjulsen.dragnsounds.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCheckCallback;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundGetDataCallback.ISoundPlaybackData;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback.ISoundCreatedCallback;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.IPlaybackArea;
import de.mrjulsen.dragnsounds.core.data.PlaybackConfig;
import de.mrjulsen.dragnsounds.core.data.filter.FileInfoFilter;
import de.mrjulsen.dragnsounds.core.data.filter.IFilter;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.net.stc.SoundPlayingCheckPacket;
import de.mrjulsen.dragnsounds.net.stc.StopAllSoundsPacket;
import de.mrjulsen.dragnsounds.net.stc.StopSoundInstancesRequest;
import de.mrjulsen.dragnsounds.net.stc.StopSoundRequest;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundConeDirectionPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundDopplerPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundPauseResumePacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundPositionPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundSeekPacket;
import de.mrjulsen.dragnsounds.net.stc.modify.SoundVolumePacket;
import de.mrjulsen.dragnsounds.registry.FilterRegistry;
import de.mrjulsen.dragnsounds.util.SoundUtils;
import de.mrjulsen.mcdragonlib.data.StatusResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Contains useful methods to play and manipulate custom sounds on the server side. For client-side sound management see {@code ClientApi}.
 * @see ClientApi
 */
public final class ServerApi {
    
    /**
     * Play a custom sound to all selected players.
     * @param file The sound file to play.
     * @param playback The playback settings.
     * @param player The players that should hear that sound.
     * @param soundCallback This callback will be called multiple times and once for EVERY single player in the {@code player}-Array.
     * The callback is called when the client received the first data packet, when the client started playing the sound and when the playback of the client has been stopped.
     * Since this may be different between all players you have to check for the player too. Also don't rely on a {@code STOP} notification, because the server may not receive one in some edge cases.
     * Use {@code player.isAlive()} and other methods to check if the player is still online.
     * @return The id of the sound.
     * @implNote Please note that the sound cannot be manipulated directly after executing this method as the sound data must first be send to the client(s). Use the callback to make sure that the client(s) have started playing before using the sound.
     */
    public static long playSound(SoundFile file, PlaybackConfig playback, ServerPlayer[] player, ISoundCreatedCallback soundCallback) {
        long id = ServerSoundManager.playSound(file, playback, player, 0);
        SoundPlayingCallback.create(id, soundCallback);
        return id;
    }

    /**
     * Enable the doppler effect for the given sound.
     * @param soundId The id of the cusotm sound.
     * @param dopplerValue The doppler factor.
     * @param velocity The velocity of the sound source.
     * @param players The affected players.
     */
    public static void setDoppler(long soundId, float dopplerValue, Vec3 velocity, ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new SoundDopplerPacket(null, soundId, dopplerValue, velocity));
    }

    /**
     * Define the cone in which the sound can be heard. Nothing happens if the given sound does not exist.
     * @param soundId The id of the cusotm sound.
     * @param direction The doppler factor.
     * @param angleA The first angle of the cone.
     * @param angleB The second angle of the cone.
     * @param outerGain The gain at the outside of the cone.
     */
    public static void setCone(long soundId, Vec3 direction, float angleA, float angleB, float outerGain, ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new SoundConeDirectionPacket(null, soundId, angleA, angleB, outerGain, direction));
    }  

    /**
     * Changes the volume, pitch and attenuationDistance of the sound.
     * @param soundId The if of the custom sound.
     * @param volume The new volume value. Use {@code -1} to keep the current value.
     * @param pitch The new pitch value. Use {@code -1} to keep the current value.
     * @param attenuationDistance The attenuationDistance. Use {@code -1} to keep the current value.
     * @param players The affected players.
     */
    public static void setVolumeAndPitch(long soundId, float volume, float pitch, int attenuationDistance, ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new SoundVolumePacket(null, soundId, volume, pitch, attenuationDistance));
    }

    /**
     * Changes to position of the sound source.
     * @param soundId The id of the custom sound.
     * @param pos The new position of the sound source in the world.
     * @param players The affected players.
     */
    public static void setPosition(long soundId, Vec3 pos, ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new SoundPositionPacket(null, soundId, pos));
    }

    /**
     * Starts playing the sound at the given number of ticks.
     * @param soundId The id of the custom sound.
     * @param ticks The amount of time in ticks to skip.
     * @param players The affected players.
     */
    public static void seek(long soundId, int ticks, ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new SoundSeekPacket(null, soundId, ticks));
    }

    /**
     * Changes the state of the sound to {@code paused} or {@code playing}.
     * @param soundId The if of the custom sound.
     * @param pause Paused or playing.
     * @param players The affected players.
     */
    public static void setSoundPaused(long soundId, boolean pause, ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new SoundPauseResumePacket(null, soundId, pause));
    }

    
    /**
     * Enable the doppler effect for the given sound.
     * @param file The custom sound file.
     * @param dopplerValue The doppler factor.
     * @param velocity The velocity of the sound source.
     * @param players The affected players.
     */
    public static void setDopplerAllInstances(SoundFile file, float dopplerValue, Vec3 velocity, ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new SoundDopplerPacket(file, 0, dopplerValue, velocity));
    }

    /**
     * Define the cone in which the sound can be heard. Nothing happens if the given sound does not exist.
     * @param file The custom sound file.
     * @param direction The doppler factor.
     * @param angleA The first angle of the cone.
     * @param angleB The second angle of the cone.
     * @param outerGain The gain at the outside of the cone.
     */
    public static void setConeAllInstances(SoundFile file, Vec3 direction, float angleA, float angleB, float outerGain, ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new SoundConeDirectionPacket(file, 0, angleA, angleB, outerGain, direction));
    }  

    /**
     * Changes the volume, pitch and attenuationDistance of the sound.
     * @param file The custom sound file.
     * @param volume The new volume value. Use {@code -1} to keep the current value.
     * @param pitch The new pitch value. Use {@code -1} to keep the current value.
     * @param attenuationDistance The attenuationDistance. Use {@code -1} to keep the current value.
     * @param players The affected players.
     */
    public static void setVolumeAndPitchAllInstances(SoundFile file, float volume, float pitch, int attenuationDistance, ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new SoundVolumePacket(file, 0, volume, pitch, attenuationDistance));
    }

    /**
     * Changes to position of the sound source.
     * @param file The custom sound file.
     * @param pos The new position of the sound source in the world.
     * @param players The affected players.
     */
    public static void setPositionAllInstances(SoundFile file, Vec3 pos, ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new SoundPositionPacket(file, 0, pos));
    }

    /**
     * Starts playing the sound at the given number of ticks.
     * @param file The custom sound file.
     * @param ticks The amount of time in ticks to skip.
     * @param players The affected players.
     */
    public static void seekAllInstances(SoundFile file, int ticks, ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new SoundSeekPacket(file, 0, ticks));
    }

    /**
     * Gets the sound playback data for each player for the given sound id.
     * @param soundId The sound id to get the data from.
     * @param players The affected players.
     * @param callback Called for each player once and contains all the playback data.
     */
    public static void getPlaybackData(long soundId, ServerPlayer[] players, ISoundPlaybackData callback) {
        ServerSoundManager.getSoundPlaybackData(soundId, players, callback);
    }

    /**
     * Changes the state of the sound to {@code paused} or {@code playing}.
     * @param file The custom sound file.
     * @param pause Paused or playing.
     * @param players The affected players.
     */
    public static void setSoundPausedAllInstances(SoundFile file, boolean pause, ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new SoundPauseResumePacket(file, 0, pause));
    }

    /**
     * Stops the selected custom sound for all players.
     * @param soundId The id of the custom sound.
     * @param players The players for whom the sound should be stopped.
     */
    public static void stopSound(long soundId, ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new StopSoundRequest(soundId));
    }

    /**
     * Stops all playing instances of the given sound file.
     * @param file The file of the sound.
     * @param players The players for whom the sound should be stopped.
     */
    public static void stopAllSoundInstances(SoundFile file, ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new StopSoundInstancesRequest(file));
    }

    /**
     * Stops the currently playing sound. This method will also send a notification to the server.
     * @param players The players for whom the sound should be stopped.
     */
    public static void stopAllCustomSounds(ServerPlayer[] players) {
        DragNSounds.net().sendToPlayers(Arrays.stream(players).toList(), new StopAllSoundsPacket());
    }

    /**
     * Checks if the sound is playing for the player.
     * @param soundId The id of the custom sound.
     * @param player The affected player.
     * @param callback Contains the response of the client.
     */
    public static void isSoundPlaying(long soundId, ServerPlayer player, Consumer<Boolean> callback) {
        final long id = SoundPlayingCheckCallback.create(callback);
        DragNSounds.net().sendToPlayer(player, new SoundPlayingCheckPacket(id, soundId));
    }

    /**
     * A simplified method to get a list of players for specific area definition settings.
     * @param level The current world level.
     * @param playbackArea The area definition settings.
     * @return A list of players that match the given criteria.
     */
    public static ServerPlayer[] selectPlayers(Level level, IPlaybackArea playbackArea) {
        return level.players().stream().filter(x -> x instanceof ServerPlayer player && playbackArea.canPlayForPlayer(level, player)).map(x -> (ServerPlayer)x).toArray(ServerPlayer[]::new);
    }


    /**
     * A simplified method to get all player passengers for a specific entity.
     * @param vehicle The vehicle to get the players from.
     * @return A list of players that are riding that vehicle.
     */
    public static ServerPlayer[] selectPlayers(Entity vehicle) {
        return vehicle.getPassengers().stream().filter(x -> x instanceof ServerPlayer).map(x -> (ServerPlayer)x).toArray(ServerPlayer[]::new);
    }

    /**
     * Get a list of all sound files.
     * @param level The world level to get the sounds from.
     * @param filters Filters for the result.
     * @return The sound files.
     * @see FilterRegistry
     * @implNote All used custom filters MUST be registered in the {@code FilterRegistry} first!
     */
    @SuppressWarnings("unchecked")
    public static Optional<SoundFile[]> getAllSoundFiles(Level level, Collection<IFilter<SoundFile>> filters) {
        try {
            SoundFile[] files = ServerSoundManager.getSoundFileList(level, filters.toArray(new IFilter[filters.size()]));
            return Optional.ofNullable(files);
        } catch (IOException e) {
            DragNSounds.LOGGER.error("Unable to get sound file list.", e);
        }
        return Optional.empty();
    }

    /**
     * Get a list of all sound files at the given location.
     * @param location The location to get the sounds from. Enter {@code null} as a root directory to get all sound files.
     * @param filters Filters for the result.
     * @return The sound files.
     * @see FilterRegistry
     * @implNote All used custom filters MUST be registered in the {@code FilterRegistry} first!
     */
    @SuppressWarnings("unchecked")
    public static Optional<SoundFile[]> getSoundFilesAt(SoundLocation location, Collection<IFilter<SoundFile>> filters) {
        try {
            Collection<IFilter<SoundFile>> filtersColl = filters;
            filtersColl.add(new FileInfoFilter(FileInfoFilter.KEY_LOCATION, location.toString(), ECompareOperation.EQUALS));
            SoundFile[] files = ServerSoundManager.getSoundFileList(location.getLevel(), filtersColl.toArray(new IFilter[filters.size()]));
            return Optional.ofNullable(files);
        } catch (IOException e) {
            DragNSounds.LOGGER.error("Unable to get sound file list.", e);
        }
        return Optional.empty();
    }

    /**
     * Get a {@code SoundFile} from the server at the given location.
     * @param location The location where the sound is saved at.
     * @param id The id of the custom sound.
     * @return The sound file.
     */
    public static Optional<SoundFile> getSoundFile(SoundLocation location, String id) {
        try {
            return Optional.ofNullable(ServerSoundManager.getSoundFile(location, id));
        } catch (IOException e) {
            DragNSounds.LOGGER.warn("Sound file not found.", e);
            return Optional.empty();
        }
    }

    /**
     * Deletes the sound at the given location.
     * @param location The location where the sound is saved at.
     * @param id The id of the custom sound.
     * @return A status notification.
     */
    public static StatusResult deleteSound(SoundLocation location, String id) {
        try {
            return ServerSoundManager.deleteSound(location, id);
        } catch (IOException e) {
            DragNSounds.LOGGER.error("Could not delete sound file.", e);
        }
        return new StatusResult(false, -3, "Could not delete sound file.");
    }

    /**
     * Deletes the sound at the given location.
     * @param file The sound file to delete.
     * @return A status notification.
     */
    public static StatusResult deleteSound(SoundFile file) {
        return deleteSound(file.getLocation(), file.getId());
    }

    /**
     * Reads all metadata from the sound file.
     * @param file The custom sound file.
     * @return All metadata found in the audio file.
     */
    public static Map<String, String> getFileMetadata(SoundFile file) {
        if (file.getAsFile().isPresent() && file.getAsFile().get().exists()) {
            return SoundUtils.getAudioMetadata(file.getAsFile().get());
        }
        return Map.of();
    }

}
