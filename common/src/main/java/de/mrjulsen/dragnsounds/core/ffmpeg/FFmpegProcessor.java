package de.mrjulsen.dragnsounds.core.ffmpeg;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.config.CommonConfig;
import de.mrjulsen.dragnsounds.core.data.WritableInputStream;
import ws.schild.jave.encode.ArgType;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingArgument;
import ws.schild.jave.encode.PredicateArgument;
import ws.schild.jave.encode.ValueArgument;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.encode.VideoFilterArgument;
import ws.schild.jave.encode.enums.TuneEnum;
import ws.schild.jave.encode.enums.VsyncMethod;
import ws.schild.jave.encode.enums.X264_PROFILE;
import ws.schild.jave.filters.FilterGraph;
import ws.schild.jave.info.VideoSize;
import ws.schild.jave.process.ProcessKiller;

public class FFmpegProcessor implements AutoCloseable {

    /** @see ws.schild.jave.Encoder globalOptions */
    public static final List<EncodingArgument> GLOBAL_OPTIONS = new ArrayList<>(Arrays.asList(
            new ValueArgument(ArgType.GLOBAL, "--filter_thread",
                    ea -> ea.getFilterThreads().map(Object::toString)),
            new ValueArgument(ArgType.GLOBAL, "-ss", ea -> ea.getOffset().map(Object::toString)),
            new ValueArgument(ArgType.INFILE, "-threads",
                    ea -> ea.getDecodingThreads().map(Object::toString)),
            new PredicateArgument(ArgType.INFILE, "-loop", "1",
                    ea -> ea.getLoop() && ea.getDuration().isPresent()),
            new ValueArgument(ArgType.INFILE, "-f", ea -> ea.getInputFormat()),
            new ValueArgument(ArgType.INFILE, "-safe", ea -> ea.getSafe().map(Object::toString)),
            new ValueArgument(ArgType.OUTFILE, "-t", ea -> ea.getDuration().map(Object::toString)),
            // Video Options
            new PredicateArgument(ArgType.OUTFILE, "-vn", ea -> !ea.getVideoAttributes().isPresent()),
            new ValueArgument(ArgType.OUTFILE, "-vcodec",
                    ea -> ea.getVideoAttributes().flatMap(VideoAttributes::getCodec)),
            new ValueArgument(ArgType.OUTFILE, "-vtag",
                    ea -> ea.getVideoAttributes().flatMap(VideoAttributes::getTag)),
            new ValueArgument(ArgType.OUTFILE, "-vb",
                    ea -> ea.getVideoAttributes()
                            .flatMap(VideoAttributes::getBitRate)
                            .map(Object::toString)),
            new ValueArgument(ArgType.OUTFILE, "-r",
                    ea -> ea.getVideoAttributes()
                            .flatMap(VideoAttributes::getFrameRate)
                            .map(Object::toString)),
            new ValueArgument(ArgType.OUTFILE, "-s",
                    ea -> ea.getVideoAttributes()
                            .flatMap(VideoAttributes::getSize)
                            .map(VideoSize::asEncoderArgument)),
            new PredicateArgument(ArgType.OUTFILE, "-movflags", "faststart",
                    ea -> ea.getVideoAttributes().isPresent()),
            new ValueArgument(ArgType.OUTFILE, "-profile:v",
                    ea -> ea.getVideoAttributes()
                            .flatMap(VideoAttributes::getX264Profile)
                            .map(X264_PROFILE::getModeName)),
            new VideoFilterArgument(ArgType.OUTFILE,
                    ea -> ea.getVideoAttributes()
                            .map(VideoAttributes::getVideoFilters)
                            .map(Collection::stream)
                            .map(s -> s.flatMap(vf -> Stream.of(vf.getExpression())))
                            .orElseGet(Stream::empty)),
            new ValueArgument(ArgType.OUTFILE, "-filter_complex",
                    ea -> ea.getVideoAttributes()
                            .flatMap(VideoAttributes::getComplexFiltergraph)
                            .map(FilterGraph::getExpression)),
            new ValueArgument(ArgType.OUTFILE, "-qscale:v",
                    ea -> ea.getVideoAttributes()
                            .flatMap(VideoAttributes::getQuality)
                            .map(Object::toString)),
            // Audio Options
            new PredicateArgument(ArgType.OUTFILE, "-an", ea -> !ea.getAudioAttributes().isPresent()),
            new ValueArgument(ArgType.OUTFILE, "-acodec",
                    ea -> ea.getAudioAttributes().flatMap(AudioAttributes::getCodec)),
            new ValueArgument(ArgType.OUTFILE, "-ab",
                    ea -> ea.getAudioAttributes()
                            .flatMap(AudioAttributes::getBitRate)
                            .map(Object::toString)),
            new ValueArgument(ArgType.OUTFILE, "-ac",
                    ea -> ea.getAudioAttributes()
                            .flatMap(AudioAttributes::getChannels)
                            .map(Object::toString)),
            new ValueArgument(ArgType.OUTFILE, "-ar",
                    ea -> ea.getAudioAttributes()
                            .flatMap(AudioAttributes::getSamplingRate)
                            .map(Object::toString)),
            new ValueArgument(ArgType.OUTFILE, "-vol",
                    ea -> ea.getAudioAttributes()
                            .flatMap(AudioAttributes::getVolume)
                            .map(Object::toString)),
            new ValueArgument(ArgType.OUTFILE, "-qscale:a",
                    ea -> ea.getAudioAttributes()
                            .flatMap(AudioAttributes::getQuality)
                            .map(Object::toString)),
            new ValueArgument(ArgType.OUTFILE, "-f", ea -> ea.getOutputFormat()),
            new ValueArgument(ArgType.OUTFILE, "-threads",
                    ea -> ea.getEncodingThreads().map(Object::toString)),
            new PredicateArgument(ArgType.OUTFILE, "-map_metadata", "0",
                    ea -> ea.isMapMetaData()),
            new ValueArgument(ArgType.OUTFILE, "-pix_fmt",
                    ea -> ea.getVideoAttributes().flatMap(VideoAttributes::getPixelFormat)),
            new ValueArgument(ArgType.OUTFILE, "-vsync",
                    ea -> ea.getVideoAttributes().flatMap(VideoAttributes::getVsync).map(VsyncMethod::getMethodName)),
            new ValueArgument(ArgType.OUTFILE, "-crf",
                    ea -> ea.getVideoAttributes().flatMap(VideoAttributes::getCrf).map(Object::toString)),
            new ValueArgument(ArgType.OUTFILE, "-preset",
                    ea -> ea.getVideoAttributes().flatMap(VideoAttributes::getPreset)),
            new ValueArgument(ArgType.OUTFILE, "-tune",
                    ea -> ea.getVideoAttributes().flatMap(VideoAttributes::getTune).map(TuneEnum::getTuneName))));

    private final String ffmpegExecutablePath;
    private final ArrayList<String> args = new ArrayList<>();
    private Process ffmpeg = null;
    private ProcessKiller ffmpegKiller = null;
    private InputStream errorStream = null;
    private Thread inputThread;
    private Thread outputThread;
    private boolean inputInterrupted = false;
    private boolean outputInterrupted = false;
    
    public FFmpegProcessor(String ffmpegExecutablePath) {
        this.ffmpegExecutablePath = ffmpegExecutablePath;
    }
    
    public void addArgument(String arg) {
        args.add(arg);
    }
    
    public InputStream execute(InputStream input, boolean printError, WritableInputStream outStream) throws Exception {
        Stream<String> execArgs = Stream.concat(Stream.of(ffmpegExecutablePath), args.stream());
        execArgs = enhanceArguments(execArgs);
        List<String> execList = execArgs.collect(Collectors.toList());

        ProcessBuilder builder = new ProcessBuilder(execList.toArray(new String[0]));
        ffmpeg = builder.start();
        errorStream = ffmpeg.getErrorStream();

        ffmpegKiller = new ProcessKiller(ffmpeg);
        Runtime.getRuntime().addShutdownHook(ffmpegKiller);

        AtomicReference<Exception> ex = new AtomicReference<>();
        inputThread = new Thread(() -> {
            try (OutputStream processInput = ffmpeg.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1 && !inputInterrupted) {
                    processInput.write(buffer, 0, bytesRead);
                }
                processInput.flush();
                if (inputInterrupted) {
                    throw new Exception("Process cancelled.");
                }
            } catch (Exception e) {
                ex.set(e);
            }
        });
        if (ex.get() != null) throw ex.get();

        AtomicReference<InputStream> resultInputStream = new AtomicReference<>();
        outputThread = new Thread(() -> {
            try (InputStream processOutput = ffmpeg.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = processOutput.read(buffer)) != -1 && !outputInterrupted) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    if (outStream != null) {
                        outStream.write(buffer, bytesRead);
                    }
                }
                resultInputStream.set(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                if (inputInterrupted) {
                    throw new Exception("Process cancelled.");
                }
            } catch (Exception e) {
                ex.set(e);
            }
        });
        if (ex.get() != null) throw ex.get();

        inputThread.start();
        outputThread.start();
        inputThread.join();
        outputThread.join();
        int exitCode = ffmpeg.waitFor();

        if (CommonConfig.ADVANCED_LOGGING.get()) DragNSounds.LOGGER.info("Process finished with exit code: " + exitCode);
        
        if (printError && ffmpeg.waitFor() != 0) {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()))) {
                StringBuilder errorMessage = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorMessage.append(line).append("\n");
                }
                throw new Exception("Process failed: " + errorMessage);
            }
        }

        return resultInputStream.get();
    }
    
    protected Stream<String> enhanceArguments(Stream<String> execArgs) {
        return execArgs;
    }
    
    public void destroy() {
        if (ffmpeg != null) {
            ffmpeg.destroy();
            ffmpeg = null;
        }

        inputInterrupted = true;
        outputInterrupted = true;

        if (ffmpegKiller != null) {
            Runtime runtime = Runtime.getRuntime();
            runtime.removeShutdownHook(ffmpegKiller);
            ffmpegKiller = null;
        }
    }

    @Override
    public void close() {
        destroy();
    }

    public InputStream getErrorStream() {
        return errorStream;
    }
}
