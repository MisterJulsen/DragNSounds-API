package de.mrjulsen.dragnsounds.registry;

import de.mrjulsen.dragnsounds.commands.arguments.SoundFileArgument;
import de.mrjulsen.dragnsounds.commands.arguments.SoundLocationArgument;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;

public class ModCommands {
    public static void init() {        
        ArgumentTypes.register("sound_files", SoundFileArgument.class, new EmptyArgumentSerializer<>(SoundFileArgument::location));
        ArgumentTypes.register("sound_location", SoundLocationArgument.class, new EmptyArgumentSerializer<>(SoundLocationArgument::location));
    }
}
