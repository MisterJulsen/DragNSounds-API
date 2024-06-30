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

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.events.ServerEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

public class SoundFileArgument implements ArgumentType<SoundFile> {

    private static final Collection<String> EXAMPLES = Arrays.asList("namespace:path/to/file/filename");

    public static SoundFileArgument location() {
        return new SoundFileArgument();
    }

    public static SoundFile getFile(CommandContext<CommandSourceStack> context, String name) {
        SoundFile file = context.getArgument(name, SoundFile.class);
        file.getLocation().setLevel(context.getSource().getLevel());
        return file;
    }

    @Override
    public SoundFile parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        while (reader.canRead() && SoundLocation.isAllowedInSoundLocation(reader.peek())) {
            reader.skip();
        }
        String string = reader.getString().substring(i, reader.getCursor());
        int lastSlashIndex = string.lastIndexOf('/');

        if (DragNSounds.hasServer()) {
            SoundLocation loc = new SoundLocation(ServerEvents.getCurrentServer().overworld(), string.substring(0, lastSlashIndex < 0 ? string.length() : lastSlashIndex));
            return SoundFile.of(loc, string.substring(lastSlashIndex < 0 ? 0 : lastSlashIndex + 1)).get();
        } else {
            return ClientSoundManager.getClientDummySoundFile(string.substring(0, lastSlashIndex < 0 ? string.length() : lastSlashIndex), string.substring(lastSlashIndex < 0 ? 0 : lastSlashIndex + 1));
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        S object = context.getSource();
        if (object instanceof CommandSourceStack commandSourceStack) {
            return SharedSuggestionProvider.suggest(ServerSoundManager.getAllSoundFiles(commandSourceStack.getServer().overworld()).stream().map(x -> x.toString()).toList(), builder);
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