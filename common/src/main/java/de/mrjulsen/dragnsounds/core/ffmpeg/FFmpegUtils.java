package de.mrjulsen.dragnsounds.core.ffmpeg;

import java.io.File;
import java.util.function.Consumer;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.api.ClientApi.UploadProgress;
import de.mrjulsen.dragnsounds.api.ClientApi.UploadState;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundUploadCancelCallback;
import de.mrjulsen.mcdragonlib.data.StatusResult;
import net.minecraft.client.Minecraft;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.InputFormatException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.progress.EncoderProgressListener;

/**
 * A collection of some simple tools to work with ffmpeg.
 */
public final class FFmpegUtils {

    public static final String VORBIS_CODEC = "libvorbis";

    /**
     * Converts the given audio file into an ogg vorbis audio file, which can be by in Minecraft, using ffmpeg.
     * @param source The input file.
     * @param target The output file.
     * @param settings The settings for the output file.
     * @param onProgressChanged This method will be called while converting and reports the progress. Pass {@code null}, if you don't want to use this.
     */
    public static void convertToOgg(long requestId, File source, File target, AudioSettings settings, Consumer<UploadProgress> onProgressChanged, Consumer<File> onFinished, Consumer<StatusResult> onError) {
        ConvertProgressListener listener = new ConvertProgressListener(onProgressChanged);
        try {
            // Audio Attributes
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec(VORBIS_CODEC);
            audio.setBitRate(settings.bitRate());
            audio.setChannels(settings.channels().getChannels());
            audio.setSamplingRate(settings.samplingRate());
            audio.setQuality((int)settings.quality());

            // Encoding attributes
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("ogg");
            attrs.setAudioAttributes(audio);            

            // Encode
            Encoder encoder = new Encoder();
            SoundUploadCancelCallback.setCancelAction(requestId, () -> {
                encoder.abortEncoding();
            });
            
            new Thread(() -> {
                try {                    
                    SoundUploadCancelCallback.setCancellable(requestId, true);
                    encoder.encode(new MultimediaObject(source), target, attrs, listener);
                    SoundUploadCancelCallback.setCancellable(requestId, false);
                    if (onFinished != null) {
                        onFinished.accept(target);
                    }
                } catch (IllegalArgumentException | EncoderException e) {
                    DragNSounds.LOGGER.error("Unable to convert audio file.", e);
                    if (onError != null) {
                        Minecraft.getInstance().execute(() -> {
                            onError.accept(new StatusResult(false, -1, e.getLocalizedMessage()));
                        });
                    }
                    SoundUploadCancelCallback.close(requestId);
                    target.delete();
                }
            }, "Audio Converter").start();

        } catch (Exception e) {
            DragNSounds.LOGGER.error("Error converting sound.", e);
            if (onError != null) {
                onError.accept(new StatusResult(false, -2, e.getLocalizedMessage()));
            }
            SoundUploadCancelCallback.close(requestId);
        }
    }

    /**
     * Get information about the given audio file.
     * @param file The file you want to get the info from.
     * @return A JAVE Multimedia Info object containing all information.
     * @throws InputFormatException
     * @throws EncoderException
     */
    public static MultimediaInfo getInfo(File file) throws InputFormatException, EncoderException {
        
        return new MultimediaObject(file).getInfo();
    }

    /**
     * A safe way to get metadata. This method returns the data for the specified key, if available. Otherwiese it will return an empty string.
     * @param info The MultimediaInfo Object
     * @param key The key of the metadata entry.
     * @return A {@code StatusResult} object. The {@code message()} method will return the value (if available) or an empty string.
     */
    public static StatusResult getMetadataSafe(MultimediaInfo info, String key) {
        return info.getAudio().getMetadata().containsKey(key) ? new StatusResult(true, 0, info.getAudio().getMetadata().get(key)) : new StatusResult(false, -1, "");
    }



    private static class ConvertProgressListener implements EncoderProgressListener {

        private final Consumer<UploadProgress> listener;

        public ConvertProgressListener(Consumer<UploadProgress> listener) {
            this.listener = listener;
        }

        @Override
        public void message(String m) { }

        @Override
        public void sourceInfo(MultimediaInfo info) { }

        @Override
        public void progress(int p) {
            double progress = p / 10.00;
            if (listener != null) {
                listener.accept(new UploadProgress(progress, UploadState.CONVERT));
            }
        }
    }
}
