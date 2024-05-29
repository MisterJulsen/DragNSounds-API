package de.mrjulsen.dragnsounds.mixin;

import java.nio.IntBuffer;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.Library.ChannelPool;
import com.mojang.blaze3d.audio.Library.CountingChannelPool;

import de.mrjulsen.dragnsounds.config.ClientConfig;

@Mixin(Library.class)
public abstract class LibraryMixin {

    @Shadow
    private ChannelPool streamingChannels;
    
    @Shadow
    private ChannelPool staticChannels;

    @Shadow
    private long context;
    
    @Shadow
    private long currentDevice;

    

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(@Nullable String deviceSpecifier, CallbackInfo ci) {
        streamingChannels = new CountingChannelPool(ClientConfig.MAX_STREAMING_CHANNELS.get());
        staticChannels = new CountingChannelPool(247);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/ALC10;alcCreateContext(JLjava/nio/IntBuffer;)J", ordinal = 0), cancellable = true)
    private void onCreateContext(CallbackInfo ci) {;
        IntBuffer b = BufferUtils.createIntBuffer(5);
        b.put(ALC11.ALC_MONO_SOURCES);
        b.put(247 + ClientConfig.MAX_STREAMING_CHANNELS.get());
        b.put(ALC11.ALC_STEREO_SOURCES);
        b.put(247 + ClientConfig.MAX_STREAMING_CHANNELS.get());
        b.put(0);
        b.flip();
        this.context = ALC10.alcCreateContext(this.currentDevice, b);

    }
}