package de.mrjulsen.dragnsounds.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.brigadier.arguments.ArgumentType;

import de.mrjulsen.dragnsounds.commands.arguments.SoundChannelsArgument;
import de.mrjulsen.dragnsounds.commands.arguments.SoundFileArgument;
import de.mrjulsen.dragnsounds.commands.arguments.SoundLocationArgument;
import de.mrjulsen.dragnsounds.commands.arguments.SoundSourceArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;

@Mixin(ArgumentTypeInfos.class)
public class ArgumentTypesAccessor {
	@Shadow
	private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(Registry<ArgumentTypeInfo<?, ?>> registry, String string, Class<? extends A> clazz, ArgumentTypeInfo<A, T> argumentSerializer) {
		throw new AssertionError("Nope.");
	}

	@Inject(method = "bootstrap", at = @At("RETURN"))
	private static void onBootstrap(Registry<ArgumentTypeInfo<?, ?>> registry, CallbackInfoReturnable<ArgumentTypeInfo<?, ?>> ci) {
		register(registry, "sound_files", SoundFileArgument.class, SingletonArgumentInfo.contextFree(SoundFileArgument::location));
		register(registry, "sound_location", SoundLocationArgument.class, SingletonArgumentInfo.contextFree(SoundLocationArgument::location));
		register(registry, "sound_source", SoundSourceArgument.class, SingletonArgumentInfo.contextFree(SoundSourceArgument::soundSource));
		register(registry, "sound_channels", SoundChannelsArgument.class, SingletonArgumentInfo.contextFree(SoundChannelsArgument::channels));
	}
}
