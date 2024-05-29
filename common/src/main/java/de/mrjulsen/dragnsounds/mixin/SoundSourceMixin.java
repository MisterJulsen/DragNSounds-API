package de.mrjulsen.dragnsounds.mixin;

import java.util.ArrayList;
import java.util.Arrays;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

import de.mrjulsen.dragnsounds.core.ext.CustomSoundSource;
import net.minecraft.sounds.SoundSource;

@Mixin(SoundSource.class)
@Unique
public class SoundSourceMixin {

	@Shadow
	@Final
	@Mutable
	private static SoundSource[] $VALUES;

	static {
		for (CustomSoundSource src : CustomSoundSource.values()) {
			soundsources$addVariant(src.getName().toUpperCase(), src.getName());
		}
	}

	@Invoker("<init>")
	public static SoundSource soundsources$invokeInit(String internalName, int internalId, String name) {
		throw new AssertionError();
	}

	private static SoundSource soundsources$addVariant(String internalName, String name) {
		ArrayList<SoundSource> variants = new ArrayList<SoundSource>(Arrays.asList(SoundSourceMixin.$VALUES));
		SoundSource source = soundsources$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1, name);
		variants.add(source);
		SoundSourceMixin.$VALUES = variants.toArray(new SoundSource[0]);
		return source;
	}
}
