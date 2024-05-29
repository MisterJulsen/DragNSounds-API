package de.mrjulsen.dragnsounds.commands.arguments;

import java.util.Arrays;
import java.util.Collection;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import net.minecraft.commands.CommandSourceStack;

public class SoundLocationArgument implements ArgumentType<SoundLocation> {

    private static final Collection<String> EXAMPLES = Arrays.asList("foo:bar");

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
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}