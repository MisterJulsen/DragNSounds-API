package de.mrjulsen.dragnsounds.registry;

import java.util.function.Supplier;

import com.mojang.brigadier.arguments.ArgumentType;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.commands.arguments.SoundChannelsArgument;
import de.mrjulsen.dragnsounds.commands.arguments.SoundFileArgument;
import de.mrjulsen.dragnsounds.commands.arguments.SoundLocationArgument;
import de.mrjulsen.dragnsounds.commands.arguments.SoundSourceArgument;
import de.mrjulsen.dragnsounds.mixin.ArgumentTypesAccessor;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

public class ModCommands {

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENTS = DeferredRegister.create(DragNSounds.MOD_ID, Registries.COMMAND_ARGUMENT_TYPE);

    public static final RegistrySupplier<ArgumentTypeInfo<?, ?>> SOUND_FILES_ARG = register(new ResourceLocation(DragNSounds.MOD_ID, "sound_files"), SoundFileArgument.class, () -> SingletonArgumentInfo.contextFree(SoundFileArgument::location));
    public static final RegistrySupplier<ArgumentTypeInfo<?, ?>> SOUND_LOCATION_ARG = register(new ResourceLocation(DragNSounds.MOD_ID, "sound_location"), SoundLocationArgument.class, () -> SingletonArgumentInfo.contextFree(SoundLocationArgument::location));
    public static final RegistrySupplier<ArgumentTypeInfo<?, ?>> SOUND_SOURCE_ARG = register(new ResourceLocation(DragNSounds.MOD_ID, "sound_source"), SoundSourceArgument.class, () -> SingletonArgumentInfo.contextFree(SoundSourceArgument::soundSource));
    public static final RegistrySupplier<ArgumentTypeInfo<?, ?>> SOUND_CHANNELS_ARG = register(new ResourceLocation(DragNSounds.MOD_ID, "sound_channels"), SoundChannelsArgument.class, () -> SingletonArgumentInfo.contextFree(SoundChannelsArgument::channels));

    private static RegistrySupplier<ArgumentTypeInfo<?, ?>> register(ResourceLocation id, Class<? extends ArgumentType<?>> clazz, Supplier<? extends ArgumentTypeInfo<?, ?>> supplier) {
        ArgumentTypesAccessor.getClassMap().put(clazz, supplier.get());
        return COMMAND_ARGUMENTS.register(id, supplier);
    }

    public static void init() {
        COMMAND_ARGUMENTS.register();
    }
}
