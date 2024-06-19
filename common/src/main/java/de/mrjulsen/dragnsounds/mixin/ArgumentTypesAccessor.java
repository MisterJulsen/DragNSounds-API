package de.mrjulsen.dragnsounds.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;

@Mixin(ArgumentTypeInfos.class)
public interface ArgumentTypesAccessor {
	@Accessor("BY_CLASS")
	static Map<Class<?>, ArgumentTypeInfo<?, ?>> getClassMap() {
		throw new AssertionError("");
	}
}
