package de.mrjulsen.dragnsounds.commands.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import de.mrjulsen.dragnsounds.core.ext.CustomSoundSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;

public class SoundSourceArgument implements ArgumentType<SoundSource> {
    private static final Dynamic2CommandExceptionType INVALID_ENUM = new Dynamic2CommandExceptionType((found, constants) -> new TranslatableComponent("commands.forge.arguments.enum.invalid", constants, found));

    public static SoundSourceArgument soundSource() {
        return new SoundSourceArgument();
    }

    @Override
    public SoundSource parse(final StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();
        try {
            SoundSource source = CustomSoundSource.getSoundSourceByName(name);
            if (source == null) {
                throw new IllegalArgumentException();
            }
            return source;
        } catch (IllegalArgumentException e) {
            throw INVALID_ENUM.createWithContext(reader, name, Arrays.toString(Arrays.stream(SoundSource.values()).map(x -> x.getName()).toArray()));
        }
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.stream(SoundSource.values()).map(x -> x.getName()).collect(Collectors.toList());
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Arrays.stream(SoundSource.values()).map(x -> x.getName()), builder);
    }
}