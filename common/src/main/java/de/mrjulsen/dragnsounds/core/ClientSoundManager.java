package de.mrjulsen.dragnsounds.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.api.Api;
import de.mrjulsen.dragnsounds.api.ClientApi.UploadProgress;
import de.mrjulsen.dragnsounds.api.ClientApi.UploadState;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundChannelsHolder;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundErrorCallback;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundFileCallback;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundListCallback;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundStreamHolder;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundUploadCallback;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundUploadCancelCallback;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundUploadProgressCallback;
import de.mrjulsen.dragnsounds.core.callbacks.server.SoundPlayingCallback.ESoundPlaybackStatus;
import de.mrjulsen.dragnsounds.core.data.ChannelContext;
import de.mrjulsen.dragnsounds.core.data.PlaybackConfig;
import de.mrjulsen.dragnsounds.core.data.SoundPlaybackData;
import de.mrjulsen.dragnsounds.core.data.filter.IFilter;
import de.mrjulsen.dragnsounds.core.ext.CustomSoundInstance;
import de.mrjulsen.dragnsounds.core.ext.CustomSoundSource;
import de.mrjulsen.dragnsounds.core.ffmpeg.AudioSettings;
import de.mrjulsen.dragnsounds.core.ffmpeg.FFmpegUtils;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.exceptions.CancelException;
import de.mrjulsen.dragnsounds.net.cts.CancelUploadSoundPacket;
import de.mrjulsen.dragnsounds.net.cts.FinishUploadSoundPacket;
import de.mrjulsen.dragnsounds.net.cts.SoundCreatedResponsePacket;
import de.mrjulsen.dragnsounds.net.cts.SoundFileRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.SoundListRequestPacket;
import de.mrjulsen.dragnsounds.net.cts.UploadSoundPacket;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.StatusResult;
import de.mrjulsen.mcdragonlib.util.MathUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import ws.schild.jave.EncoderException;
import ws.schild.jave.info.MultimediaInfo;

public final class ClientSoundManager {

    public static void init() {}
    

    public static void playUiSound(long soundId, SoundFile file, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(CustomSoundInstance.ui(soundId, file, CustomSoundSource.getSoundSourceByName(CustomSoundSource.CUSTOM.getName()), pitch, volume));
    }

    public static void playWorldSound(long soundId, SoundFile file, float volume, float pitch, Vec3 pos, boolean relative, int attenuationDistance) {        
        Minecraft.getInstance().getSoundManager().play(CustomSoundInstance.world(soundId, file, CustomSoundSource.getSoundSourceByName(CustomSoundSource.CUSTOM.getName()), pitch, volume, pos, relative, attenuationDistance));
    }

    public static void playUiSound(long soundId, SoundFile file, SoundSource source, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(CustomSoundInstance.ui(soundId, file, source, pitch, volume));
    }

    public static void playWorldSound(long soundId, SoundFile file, SoundSource source, float volume, float pitch, Vec3 pos, boolean relative, int attenuationDistance) {        
        Minecraft.getInstance().getSoundManager().play(CustomSoundInstance.world(soundId, file, source, pitch, volume, pos, relative, attenuationDistance));
    }

    @SuppressWarnings("resource")
    public static void playSoundQueue(long soundId, int triggerIndex, SoundFile file, PlaybackConfig playback, long clientCallbackRequestId) {
        new Thread(() -> {
            while (SoundStreamHolder.get(soundId).currentlyNeeded() < triggerIndex) {
                try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException e) { }
            }

            ClientInstanceManager.addRunnableCallback(soundId, () -> {
                if (playback.offsetTicks() > 0) {
                    seek(soundId, playback.offsetTicks());
                }
    
                if (playback.showNowPlayingText()) {
                    Minecraft.getInstance().gui.setNowPlaying(TextUtils.text(file.getDisplayName()));
                }
    
                if (clientCallbackRequestId > 0) {
                    ClientInstanceManager.runSoundRequestCallback(clientCallbackRequestId, soundId);
                }

                ClientInstanceManager.getSoundCommandListener(soundId).start(file);
                DragNSounds.net().sendToServer(new SoundCreatedResponsePacket(soundId, ESoundPlaybackStatus.PLAY));
            });

            switch (playback.type()) {
                case WORLD:
                    playWorldSound(soundId, file, CustomSoundSource.getSoundSourceByName(playback.soundSourceName()), playback.volume(), playback.pitch(), playback.pos(), playback.relative(), playback.attenuationDistance());
                    break;
                default:
                    playUiSound(soundId, file, playback.volume(), playback.pitch());
                    break;
            }
        }, "Sound Playback Trigger").start();
    }

    public static SoundPlaybackData getData(long soundId) {
        if (!SoundChannelsHolder.has(soundId)) {
            return null;
        }
        ChannelContext channel = SoundChannelsHolder.get(soundId);
        float[] x = new float[1];
        float[] y = new float[1];
        float[] z = new float[1];
        AL10.alGetSource3f(channel.source(), AL11.AL_POSITION, x, y, z);
        float[] dx = new float[1];
        float[] dy = new float[1];
        float[] dz = new float[1];
        AL10.alGetSource3f(channel.source(), AL11.AL_DIRECTION, dx, dy, dz);
        float[] vx = new float[1];
        float[] vy = new float[1];
        float[] vz = new float[1];
        AL10.alGetSource3f(channel.source(), AL11.AL_VELOCITY, vx, vy, vz);

        return new SoundPlaybackData(
            AL10.alGetSourcef(channel.source(), AL11.AL_GAIN),
            AL10.alGetSourcef(channel.source(), AL11.AL_PITCH),
            AL10.alGetSourcei(channel.source(), AL11.AL_MAX_DISTANCE),
            new Vec3(x[0], y[0], z[0]),
            new Vec3(dx[0], dy[0], dz[0]),
            new Vec3(vx[0], vy[0], vz[0]),
            AL10.alGetSourcef(channel.source(), AL11.AL_DOPPLER_FACTOR),
            AL10.alGetSourcef(channel.source(), AL11.AL_CONE_INNER_ANGLE),
            AL10.alGetSourcef(channel.source(), AL11.AL_CONE_OUTER_ANGLE),
            AL10.alGetSourcef(channel.source(), AL11.AL_CONE_OUTER_GAIN),
            AL10.alGetSourcei(channel.source(), AL11.AL_SOURCE_STATE) == AL11.AL_PAUSED,
            (int)((float)AL10.alGetSourcei(channel.source(), AL11.AL_BYTE_OFFSET) / (float)channel.sampleSize() / 20.0F)
        );
    }

    public static void setDoppler(long soundId, float dopplerFactor, Vec3 velocity) {
        queueTask(soundId, () -> {
            if (SoundChannelsHolder.has(soundId)) {
                ChannelContext context = SoundChannelsHolder.get(soundId);
                AL10.alSource3f(context.source(), AL11.AL_VELOCITY, (float)velocity.x(), (float)velocity.y(), (float)velocity.z());
                AL10.alSourcef(context.source(), AL11.AL_DOPPLER_FACTOR, MathUtils.clamp(dopplerFactor, 0, 1));
            }
        });
    }

    public static void setCone(long soundId, Vec3 direction, float angleA, float angleB, float outerGain) {
        queueTask(soundId, () -> {
            if (SoundChannelsHolder.has(soundId)) {
                ChannelContext context = SoundChannelsHolder.get(soundId);
                setDirection(soundId, direction);
                AL10.alSourcef(context.source(), AL10.AL_CONE_INNER_ANGLE, angleA);
                AL10.alSourcef(context.source(), AL10.AL_CONE_OUTER_ANGLE, angleB);
                AL10.alSourcef(context.source(), AL10.AL_CONE_OUTER_GAIN, outerGain);
            }
        });
    }

    public static void setDirection(long soundId, Vec3 direction) {
        queueTask(soundId, () -> {
            if (SoundChannelsHolder.has(soundId)) {
                ChannelContext context = SoundChannelsHolder.get(soundId);
                AL10.alSource3f(context.source(), AL11.AL_VELOCITY, (float)direction.x(), (float)direction.y(), (float)direction.z());
            }
        });
    }

    public static void setVolume(long soundId, float volume) {
        queueTask(soundId, () -> {
            if (SoundChannelsHolder.has(soundId)) {
                ChannelContext context = SoundChannelsHolder.get(soundId);
                context.channel().setVolume(MathUtils.clamp(volume, CustomSoundInstance.VOLUME_MIN, CustomSoundInstance.VOLUME_MAX));
            }
        });
    }

    public static void setPaused(long soundId, boolean pause) {
        queueTask(soundId, () -> {
            if (SoundChannelsHolder.has(soundId)) {
                ChannelContext context = SoundChannelsHolder.get(soundId);
                if (pause) {
                    context.channel().pause();
                } else {
                    context.channel().play();
                }
            }
        });
    }

    public static void setPitch(long soundId, float pitch) {
        queueTask(soundId, () -> {
            if (SoundChannelsHolder.has(soundId)) {
                ChannelContext context = SoundChannelsHolder.get(soundId);
                context.channel().setPitch(MathUtils.clamp(pitch, CustomSoundInstance.PITCH_MIN, CustomSoundInstance.PITCH_MAX));
            }
        });
    }

    public static void setPosition(long soundId, Vec3 position) {
        queueTask(soundId, () -> {
            if (SoundChannelsHolder.has(soundId)) {
                ChannelContext context = SoundChannelsHolder.get(soundId);
                context.channel().setSelfPosition(position);
            }
        });
    }

    public static void setAttenuationDistance(long soundId, float distance) {
        queueTask(soundId, () -> {
            if (SoundChannelsHolder.has(soundId)) {
                ChannelContext context = SoundChannelsHolder.get(soundId);
                context.channel().linearAttenuation(distance);
            }
        });
    }

    public static void seek(long soundId, int ticks) {
        queueTask(soundId, () -> {
            if (SoundChannelsHolder.has(soundId)) {
                ChannelContext context = SoundChannelsHolder.get(soundId);
                context.channel().pause();
                float seconds = (float)ticks / DragonLib.TPS;
                context.pumpBuffers().accept(200);
                while (AL10.alGetSourcei(context.source(), AL11.AL_BUFFERS_QUEUED) < (int)seconds + 1) {
                    try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException e) { }
                }
                AL10.alSourcef(context.source(), AL11.AL_BYTE_OFFSET, (float)context.sampleSize() * seconds);
                
                context.channel().unpause();
            }
        });
    }

    public static void stopSound(long soundId) {
        queueTask(soundId, () -> {
            if (SoundChannelsHolder.has(soundId)) {
                SoundChannelsHolder.get(soundId).channel().stop();
            }
        });
    }

    
    public static void stopAllSoundInstances(SoundFile file) {
        Arrays.stream(ClientInstanceManager.getInstancesOfSound(file)).forEach(x -> stopSound(x));
    }


    public static void stopAllSounds() {
        Arrays.stream(ClientInstanceManager.getAllSoundIds()).forEach(x -> stopSound(x));
    }

    public static void queueTask(long soundId, Runnable action) {
        if (!ClientInstanceManager.isSoundIdBlocked(soundId)) {
            if (ClientInstanceManager.getSoundCommandListener(soundId) != null) {
                ClientInstanceManager.getSoundCommandListener(soundId).queue(action);
            } else {
                DragNSounds.LOGGER.warn("Failed to queue sound modification command. The sound has not been created yet or is already closed. ID: " + soundId);
            }
            
        }
    }


    

    public static long uploadSound(String filePath, SoundFile.Builder targetSettings, AudioSettings settings, Consumer<Optional<SoundFile>> afterUpload, BiConsumer<UploadProgress, UploadProgress> progress, Consumer<StatusResult> onError) {
        final long requestId = Api.id();
        new Thread(() -> {
            try {
                File input = new File(filePath);
                File tempFile = File.createTempFile(DragNSounds.MOD_ID, "upload");
                tempFile.deleteOnExit();
                FFmpegUtils.convertToOgg(requestId, input, tempFile, settings, (prg) -> progress.accept(prg, prg), (output) -> uploadFileInternal(requestId, output, targetSettings, afterUpload, progress, onError), onError);
            } catch (IOException e) {
                DragNSounds.LOGGER.error("Unable to upload custom sound.", e);
                if (onError != null) {
                    Minecraft.getInstance().execute(() -> {
                        onError.accept(new StatusResult(false, -3, e.getLocalizedMessage()));
                    });
                }
                SoundUploadCancelCallback.close(requestId);
            }
        }, "Sound Converter").start();
        return requestId;
    } 

    private static void uploadFileInternal(long requestId, File file, SoundFile.Builder data, Consumer<Optional<SoundFile>> afterUpload, BiConsumer<UploadProgress, UploadProgress> progress, Consumer<StatusResult> onError) {
        
        try (InputStream stream = new FileInputStream(file)) {
            final int size = stream.available();
            int index = 0;
            int bytesRead = 0;
            byte[] buffer = new byte[DragNSounds.DEFAULT_NET_DATA_SIZE * 2];
            AtomicReference<UploadProgress> serverProgress = new AtomicReference<>(new UploadProgress(0, UploadState.UPLOAD));
            AtomicBoolean cancelled = new AtomicBoolean(false);

            // Setup cancel function
            SoundUploadCancelCallback.setCancelAction(requestId, () -> cancelled.set(true));
            SoundUploadCancelCallback.setCancellable(requestId, true);

            // Register callbacks
            double clientProgress = 100D / size * (size - stream.available());
            SoundUploadProgressCallback.create(requestId, (server) -> {
                serverProgress.set(server);
                if (progress != null) {
                    progress.accept(new UploadProgress(clientProgress, UploadState.UPLOAD), serverProgress.get());
                }
            });
            SoundErrorCallback.create(requestId, onError);

            while ((bytesRead = stream.read(buffer)) != -1) {
                if (cancelled.get()) { // check cancel request
                    throw new CancelException("Sound upload cancelled.");
                }

                if (bytesRead < buffer.length) { // Fix 0-bytes at the end of the file which may cause problems while playing
                    byte[] tmp = new byte[bytesRead];
                    System.arraycopy(buffer, 0, tmp, 0, bytesRead);
                    buffer = tmp;
                }

                DragNSounds.net().sendToServer(new UploadSoundPacket(requestId, index, stream.available() > 0, size, buffer));
                if (progress != null) {
                    progress.accept(new UploadProgress(clientProgress, UploadState.UPLOAD), serverProgress.get());
                }
                index++;

                try { TimeUnit.MILLISECONDS.sleep(1); } catch (InterruptedException e) { }
            }
            SoundUploadCancelCallback.setCancellable(requestId, false);

            // Finish upload
            SoundUploadCallback.create(requestId, afterUpload);
            long duration = 0;
            int channels = 2;
            try {
                MultimediaInfo info = FFmpegUtils.getInfo(file);
                duration = info.getDuration();
                channels = info.getAudio().getChannels();
            } catch (EncoderException e) {
                DragNSounds.LOGGER.error("Unable to get audio duration.", e);
            }
            DragNSounds.net().sendToServer(new FinishUploadSoundPacket(requestId, size, data, channels, duration));

        } catch (IOException e) {
            DragNSounds.LOGGER.error("Unable to upload custom sound.", e);
            if (onError != null) {
                Minecraft.getInstance().execute(() -> {
                    onError.accept(new StatusResult(false, -4, e.getLocalizedMessage()));
                });
            }
            DragNSounds.net().sendToServer(new CancelUploadSoundPacket(requestId));
            ClientInstanceManager.closeUploadCallbacks(requestId);
        } catch (CancelException e) {
            DragNSounds.LOGGER.warn("Upload aborted.", e);
            DragNSounds.net().sendToServer(new CancelUploadSoundPacket(requestId));
            ClientInstanceManager.closeUploadCallbacks(requestId);
            if (onError != null) {
                Minecraft.getInstance().execute(() -> {
                    onError.accept(new StatusResult(true, 1, e.getLocalizedMessage()));
                });
            }
        } finally {
            file.delete();
            SoundUploadCancelCallback.close(requestId);
        }
    }

    @SuppressWarnings("unchecked")
    public static void getAllSoundFiles(Collection<IFilter<SoundFile>> filters, Consumer<SoundFile[]> callback) {
        final long requestId = SoundListCallback.create(callback);
        DragNSounds.net().sendToServer(new SoundListRequestPacket(requestId, filters.toArray(new IFilter[filters.size()])));        
    }

    
    public static void getSoundFile(SoundLocation location, String id, Consumer<Optional<SoundFile>> callback) {
        final long requestId = SoundFileCallback.create(callback);
        DragNSounds.net().sendToServer(new SoundFileRequestPacket(requestId, id, location));
    }

    @SuppressWarnings("resource")
    public static SoundFile getClientDummySoundFile(String location, String id) {
        SoundLocation loc = new SoundLocation(Minecraft.getInstance().level, location);
        return SoundFile.client(loc, id);
    }


    /* DEBUG AREA */

    public static void printDebug(List<String> debugScreen) {
        if (DragNSounds.hasServer()) {
            debugScreen.add(ServerInstanceManager.debugString());
        }
        if (Platform.getEnvironment() == Env.CLIENT) {
            debugScreen.add(ClientInstanceManager.debugString());
        }
    }
}
