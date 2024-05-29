package de.mrjulsen.dragnsounds.core.data;

import java.util.function.Consumer;

import com.mojang.blaze3d.audio.Channel;

/**
 * A simple wrapper for {@code Channel} which contains additional data relevant for manipulation.
 */
public record ChannelContext(Channel channel, int source, long soundId, int sampleSize, Consumer<Integer> pumpBuffers) {
}
