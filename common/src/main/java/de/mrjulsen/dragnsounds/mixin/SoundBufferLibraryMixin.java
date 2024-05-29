package de.mrjulsen.dragnsounds.mixin;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.callbacks.client.SoundStreamHolder;
import de.mrjulsen.dragnsounds.core.ext.CustomOggAudioStream;
import de.mrjulsen.dragnsounds.core.ext.CustomSoundInstance;
import net.minecraft.Util;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;

@Mixin(SoundBufferLibrary.class)
public class SoundBufferLibraryMixin {

    @Inject(method = "getStream", at = @At("HEAD"), cancellable = true)
    public void onGetStream(ResourceLocation resourceLocation, boolean loop, CallbackInfoReturnable<CompletableFuture<AudioStream>> cir) {
        String pathPrefix = CustomSoundInstance.SOUND_LOCATION_ROOT + "/" + CustomSoundInstance.CUSTOM_SOUND_FILENAME_ROOT + "/";
        if (resourceLocation.getNamespace().equals(DragNSounds.MOD_ID) && resourceLocation.getPath().startsWith(pathPrefix)) {
            cir.setReturnValue(
                CompletableFuture.supplyAsync(() -> {
                    try {
                        String relPath = resourceLocation.getPath().replace(pathPrefix, "").replace(".ogg", "");
                        long soundId = Long.parseLong(relPath);
                        InputStream inputStream = SoundStreamHolder.get(soundId);
                        return loop ? new LoopingAudioStream(input -> new CustomOggAudioStream(soundId, input), inputStream) : new CustomOggAudioStream(soundId, inputStream);
                    } catch (Exception iOException) {
                        throw new CompletionException(iOException);
                    }
                }, Util.backgroundExecutor())
            );
            cir.cancel();
        }
    }
}

