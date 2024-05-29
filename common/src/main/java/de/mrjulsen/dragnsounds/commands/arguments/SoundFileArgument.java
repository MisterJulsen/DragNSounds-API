package de.mrjulsen.dragnsounds.commands.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.events.ServerEvents;
import net.minecraft.commands.CommandSourceStack;

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
        SoundLocation loc = new SoundLocation(ServerEvents.getCurrentServer().overworld(), string.substring(0, lastSlashIndex));
        return SoundFile.of(loc, UUID.fromString(string.substring(lastSlashIndex + 1))).get();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}