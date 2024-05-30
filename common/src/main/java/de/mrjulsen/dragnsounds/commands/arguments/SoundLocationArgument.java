package de.mrjulsen.dragnsounds.commands.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

public class SoundLocationArgument implements ArgumentType<SoundLocation> {

    private static final Collection<String> EXAMPLES = Arrays.asList("namespace:path/to/file");

    public static SoundLocationArgument location() {
        return new SoundLocationArgument();
    }

    public static SoundLocation getFile(CommandContext<CommandSourceStack> context, String name) {
        SoundLocation location = context.getArgument(name, SoundLocation.class);
        location.setLevel(context.getSource().getLevel());
        return location;
    }

    @Override
    public SoundLocation parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        while (reader.canRead() && SoundLocation.isAllowedInSoundLocation(reader.peek())) {
            reader.skip();
        }
        String string = reader.getString().substring(i, reader.getCursor());
        return new SoundLocation(null, string);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        S object = context.getSource();
        if (object instanceof CommandSourceStack commandSourceStack) {
            return SharedSuggestionProvider.suggest(ServerSoundManager.getAllUsedLocations(commandSourceStack.getServer().overworld()).stream().map(x -> x.toString()).toList(), builder);
        } else if (object instanceof SharedSuggestionProvider sharedSuggestionProvider) {
            return sharedSuggestionProvider.customSuggestion(context);
        } else {
            return Suggestions.empty();
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}