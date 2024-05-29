package de.mrjulsen.dragnsounds.mixin;

import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.audio.Channel;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ClientInstanceManager;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundChannelsHolder;
import de.mrjulsen.dragnsounds.core.data.ChannelContext;
import de.mrjulsen.dragnsounds.core.ext.CustomOggAudioStream;
import de.mrjulsen.dragnsounds.net.cts.StopSoundNotificationPacket;
import de.mrjulsen.dragnsounds.util.ISelfCast;
import net.minecraft.client.sounds.AudioStream;

@Mixin(Channel.class)
public class ChannelMixin implements ISelfCast<Channel> {

    private long soundId;
    private boolean isCustom;

    @Shadow
    public int source;

    @Shadow
    private AudioStream stream;

    @Shadow
    private int streamingBufferSize;

    @Shadow
    private void pumpBuffers(int amount) {}

    @Inject(method = "attachBufferStream", at = @At(value = "TAIL"))
    private void onAttachBufferStream(AudioStream stream, CallbackInfo ci) { 
        if (stream instanceof CustomOggAudioStream customStream) {
            isCustom = true;
            soundId = customStream.getSoundId();
            SoundChannelsHolder.create(soundId, new ChannelContext(self(), source, soundId, streamingBufferSize, this::pumpBuffers));
            ClientInstanceManager.runRunnableCallback(soundId);
        }
    }

    /**
     * Max of 4 buffered samples!
     */
    @Inject(method = "updateStream()V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/audio/Channel;pumpBuffers(I)V", ordinal = 0), cancellable = true)
    private void onUpdateStream(CallbackInfo ci) {
        int i = AL10.alGetSourcei(this.source, AL10.AL_BUFFERS_QUEUED);
        if (i >= 4) {
            ci.cancel();
        }
    }

    @Inject(method = "destroy", at = @At(value = "TAIL"))
    private void onDestroy(CallbackInfo ci) {
        if (isCustom) {
            SoundChannelsHolder.close(soundId);
            ClientInstanceManager.removeSoundCommandListener(soundId).stop();
            ClientInstanceManager.delayedSoundGC(soundId, 60000);
            DragNSounds.net().sendToServer(new StopSoundNotificationPacket(soundId));
            DragNSounds.LOGGER.info("Audio Channel of sound " + soundId + " removed.");
        }
    }

    @Inject(method = "pumpBuffers", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/AudioStream;read(I)Ljava/nio/ByteBuffer;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onPumpBuffers(int readCount, CallbackInfo ci, int i2) {
        if (readCount > 4) {
            // DO STUFF
        }
    }
}
