package de.mrjulsen.dragnsounds.test;

import de.mrjulsen.dragnsounds.DragNSounds;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(DragNSounds.MOD_ID, Registry.ITEM_REGISTRY);

    public static final RegistrySupplier<Item> TEST_ITEM = ITEMS.register("test", TestItem::new);

    public static final void register() {
        ITEMS.register();
    }
}
