package de.mrjulsen.dragnsounds.core.filesystem;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ffmpeg.FFmpegUtils;
import de.mrjulsen.dragnsounds.util.SoundUtils;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.INBTSerializable;
import net.minecraft.nbt.CompoundTag;
import ws.schild.jave.EncoderException;
import ws.schild.jave.info.MultimediaInfo;

/**
 * A collection of read-only properties about the file.
 */
public class SoundFileInfo implements INBTSerializable {

    private static final String NBT_DURATION = "Duration";
    private static final String NBT_SIZE = "Size";
    private static final String NBT_OWNER = "Owner";
    private static final String NBT_UPLOAD_TIME = "UploadTime";
    private static final String NBT_TITLE = "Title";
    private static final String NBT_ARTIST = "Artist";
    private static final String NBT_ALBUM = "Album";
    private static final String NBT_GENRE = "Genre";
    private static final String NBT_YEAR = "Year";
    private static final String NBT_CHANNELS = "Channels";

    public static final String META_TITLE = "title";
    public static final String META_ARTIST = "artist";
    public static final String META_YEAR = "date";
    public static final String META_ALBUM = "album";
    public static final String META_GENRE = "genre";


    private long duration;
    private long fileSize;
    private UUID ownerId = DragNSounds.ZERO_UUID;
    private long uploadTimestamp;
    private int channels;
    private String title;
    private String artist;
    private String year;
    private String album;
    private String genre;

    public SoundFileInfo(long duration, long fileSize, UUID ownerId, long uploadTimestamp, int channels, String title, String artist, String year, String album, String genre) {
        this.duration = duration;
        this.fileSize = fileSize;
        this.ownerId = ownerId;
        this.uploadTimestamp = uploadTimestamp;
        this.channels = channels;
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.album = album;
        this.genre = genre;
    }

    private SoundFileInfo() {}

    /**
     * A shortcut to get the playback duration of the sound file. Use this method instead of other methods to calculate it, since it has higher performance. The value changes automatically when the file hash changes.
     * @return The playback duration in seconds.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * A shortcut to get the size of the sound file. Use this method instead of other methods to calculate it, since it has higher performance. The value changes automatically when the file hash changes.
     * @return The size of the file in bytes.
     */
    public long getSize() {
        return fileSize;
    }

    /**
     * @return The UUID of the player which has uploaded this file.
     */
    public UUID getOwnerId() {
        return ownerId;
    }

    /**
     * @return The timestamp in millis when the player has uploaded this file.
     */
    public long getUploadTimeMillis() {
        return uploadTimestamp;
    }

    /**
     * @return A formatted representation of the time when the player has uploaded this file.
     */
    public Date getUploadDate() {
        return new Date(getUploadTimeMillis());
    }

    /**
     * @return A formatted representation of the time when the player has uploaded this file.
     */
    public String getUploadTimeFormatted() {
        return DragonLib.DATE_FORMAT.format(getUploadDate());
    }

    /**
     * @return The original title of the sound file which has been uploaded.
     */
    public String getOriginalTitle() {
        return title;
    }

    /**
     * @return The original artist of the sound file which has been uploaded.
     */
    public String getArtist() {
        return artist;
    }

    /**
     * @return The original date of the sound file which has been uploaded.
     */
    public String getDate() {
        return year;
    }

    /**
     * @return The original genre of the sound file which has been uploaded.
     */
    public String getGenre() {
        return genre;
    }

    /**
     * @return The original album of the sound file which has been uploaded.
     */
    public String getAlbum() {
        return album;
    }

    /**
     * @return The amount of audio channels in this file.
     */
    public int getChannels() {
        return channels;
    }

    public static SoundFileInfo fromNbt(CompoundTag nbt) {
        SoundFileInfo info = new SoundFileInfo();
        info.deserializeNbt(nbt);
        return info;
    }

    @Override
    public CompoundTag serializeNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong(NBT_DURATION, getDuration());
        nbt.putLong(NBT_SIZE, getSize());
        nbt.putUUID(NBT_OWNER, getOwnerId());
        nbt.putLong(NBT_UPLOAD_TIME, getUploadTimeMillis());
        nbt.putInt(NBT_CHANNELS, getChannels());
        nbt.putString(NBT_TITLE, getOriginalTitle());
        nbt.putString(NBT_ARTIST, getArtist());
        nbt.putString(NBT_YEAR, getDate());
        nbt.putString(NBT_GENRE, getGenre());
        nbt.putString(NBT_ALBUM, getAlbum());
        return nbt;
    }

    @Override
    public void deserializeNbt(CompoundTag nbt) {
        duration = nbt.getLong(NBT_DURATION);
        fileSize = nbt.getLong(NBT_SIZE);
        ownerId = nbt.getUUID(NBT_OWNER);
        uploadTimestamp = nbt.getLong(NBT_UPLOAD_TIME);
        channels = nbt.getInt(NBT_CHANNELS);
        title = nbt.getString(NBT_TITLE);
        artist = nbt.getString(NBT_ARTIST);
        year = nbt.getString(NBT_YEAR);
        album = nbt.getString(NBT_ALBUM);
        genre = nbt.getString(NBT_GENRE);
    }

    public static SoundFileInfo empty() {
        return new SoundFileInfo();
    }

    public static SoundFileInfo of(File file, UUID owner, long timestamp, int initialChannels, long initialDuration) {
        long duration = initialDuration;
        int channels = initialChannels;
        try {
            MultimediaInfo info = FFmpegUtils.getInfo(file);
            duration = info.getDuration();
            channels = info.getAudio().getChannels();
        } catch (EncoderException e) {
            DragNSounds.LOGGER.error("Unable to get audio duration.", e);
        }
        Map<String, String> metadata = SoundUtils.getAudioMetadata(file);

        return new SoundFileInfo(
            duration,
            file.length(),
            owner,
            timestamp,
            channels,
            SoundUtils.getMetaSafe(metadata, META_TITLE),
            SoundUtils.getMetaSafe(metadata, META_ARTIST),
            SoundUtils.getMetaSafe(metadata, META_YEAR),
            SoundUtils.getMetaSafe(metadata, META_ALBUM),
            SoundUtils.getMetaSafe(metadata, META_GENRE)
        );
    }
}
