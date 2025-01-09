package de.mrjulsen.dragnsounds.core.ffmpeg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.api.ClientApi.UploadProgress;
import de.mrjulsen.dragnsounds.api.ClientApi.UploadState;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundUploadCancelCallback;
import de.mrjulsen.dragnsounds.core.data.WritableInputStream;
import de.mrjulsen.mcdragonlib.data.StatusResult;
import net.minecraft.client.Minecraft;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.InputFormatException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.ArgType;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.process.ProcessLocator;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;
import ws.schild.jave.progress.EncoderProgressListener;

/**
 * A collection of some simple tools to work with ffmpeg.
 */
public final class FFmpegUtils {

    public static final String VORBIS_CODEC = "libvorbis";
    public static final String OGG = "ogg";

    /**
     * Converts the given audio file into an ogg vorbis audio file, which can be used by in Minecraft, using ffmpeg.
     * @param requestId The id of the request. If unknown, use {@code System.nanoTime()}.
     * @param source The input file.
     * @param target The output file.
     * @param settings The settings for the output file.
     * @param onProgressChanged This method will be called while converting and reports the progress. Pass {@code null}, if you don't want to use this.
     * @param onFinished This method will be called after converting the audio file. Pass {@code null}, if you don't want to use this.
     * @param onError This method will be called when an error occurs. Pass {@code null}, if you don't want to use this.
     * @deprecated Use {@code convertToOggStream} instead, as it doesn't use temp files.
     */
    @Deprecated
    public static void convertToOggFile(long requestId, File source, File target, AudioSettings settings, Consumer<UploadProgress> onProgressChanged, Consumer<File> onFinished, Consumer<StatusResult> onError) {
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
     * Converts the given audio file into an ogg vorbis audio file, which can be used by in Minecraft, using ffmpeg.
     * @param requestId The id of the request. If unknown, use {@code System.nanoTime()}.
     * @param audioInputData The input file data.
     * @param settings The settings for the output file.
     * @param onProgressChanged This method will be called while converting and reports the progress. Pass {@code null}, if you don't want to use this.
     * @param onFinished This method will be called after converting the audio file. Pass {@code null}, if you don't want to use this.
     * @param onError This method will be called when an error occurs. Pass {@code null}, if you don't want to use this.
     */
    public static void convertToOggStream(long requestId, InputStream audioInputData, AudioSettings settings, Consumer<InputStream> onFinished, Consumer<StatusResult> onError, WritableInputStream outStream) throws IOException {
        // Audio Attributes
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec(VORBIS_CODEC);
        audio.setBitRate(settings.bitRate());
        audio.setChannels(settings.channels().getChannels());
        audio.setSamplingRate(settings.samplingRate());
        audio.setQuality((int)settings.quality());

        // Encoding attributes
        EncodingAttributes attributes = new EncodingAttributes();
        attributes.setOutputFormat(OGG);
        attributes.setAudioAttributes(audio);        
        attributes.validate();

        ProcessLocator locator = new DefaultFFMPEGLocator();
        try (FFmpegProcessor ffmpeg = new FFmpegProcessor(locator.getExecutablePath())) {
            // Set global options
            FFmpegProcessor.GLOBAL_OPTIONS
                .stream()
                .filter(ea -> ArgType.GLOBAL.equals(ea.getArgType()))
                .flatMap(eArg -> eArg.getArguments(attributes))
                .forEach(ffmpeg::addArgument);

            // Set input options, must be before -i argument
            FFmpegProcessor.GLOBAL_OPTIONS
                .stream()
                .filter(ea -> ArgType.INFILE.equals(ea.getArgType()))
                .flatMap(eArg -> eArg.getArguments(attributes))
                .forEach(ffmpeg::addArgument);

            ffmpeg.addArgument("-i");
            ffmpeg.addArgument("pipe:0");
            ffmpeg.addArgument("-loglevel");
            ffmpeg.addArgument("quiet");

            // Set output options. Must be after the -i and before the outfile target
            FFmpegProcessor.GLOBAL_OPTIONS
                .stream()
                .filter(ea -> ArgType.OUTFILE.equals(ea.getArgType()))
                .flatMap(eArg -> eArg.getArguments(attributes))
                .forEach(ffmpeg::addArgument);

            ffmpeg.addArgument("-y");
            ffmpeg.addArgument("pipe:1");
            
            SoundUploadCancelCallback.setCancelAction(requestId, () -> {
                ffmpeg.destroy();
            });

            try {
                SoundUploadCancelCallback.setCancellable(requestId, true);
                InputStream output = ffmpeg.execute(audioInputData, true, outStream);
                SoundUploadCancelCallback.setCancellable(requestId, false);
                onFinished.accept(output);
            } catch (Exception e) {
                DragNSounds.LOGGER.error("Unable to convert audio file.", e);
                if (onError != null) {
                    Minecraft.getInstance().execute(() -> {
                        onError.accept(new StatusResult(false, -1, e.getLocalizedMessage()));
                    });
                }
            }
        } finally {            
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
