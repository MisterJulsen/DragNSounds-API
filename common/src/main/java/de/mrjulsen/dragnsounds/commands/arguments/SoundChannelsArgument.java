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

import de.mrjulsen.dragnsounds.core.ffmpeg.EChannels;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.commands.SharedSuggestionProvider;

public class SoundChannelsArgument implements ArgumentType<EChannels> {
    private static final Dynamic2CommandExceptionType INVALID_ENUM = new Dynamic2CommandExceptionType((found, constants) -> TextUtils.translate("commands.forge.arguments.enum.invalid", constants, found));

    public static SoundChannelsArgument channels() {
        return new SoundChannelsArgument();
    }

    @Override
    public EChannels parse(final StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();
        try {
            EChannels source = EChannels.getByNameUnsafe(name);
            if (source == null) {
                throw new IllegalArgumentException();
            }
            return source;
        } catch (IllegalArgumentException e) {
            System.out.println("SALZ");
            throw INVALID_ENUM.createWithContext(reader, name, Arrays.toString(Arrays.stream(EChannels.values()).map(x -> x.getName()).toArray()));
        }
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.stream(EChannels.values()).map(x -> x.getName()).collect(Collectors.toList());
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Arrays.stream(EChannels.values()).map(x -> x.getName()), builder);
    }
}