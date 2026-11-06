package com.minelittlepony.unicopia.container;

import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface UScreenHandlers {
    MenuType<SpellbookScreenHandler> SPELL_BOOK = register("spell_book", new ExtendedMenuType<>(SpellbookScreenHandler::new, SpellbookState.PACKET_CODEC));
    MenuType<ShapingBenchScreenHandler> SHAPING_BENCH = register("shaping_bench", new MenuType<>(ShapingBenchScreenHandler::new, FeatureFlags.VANILLA_SET));

    static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType<T> type) {
        return Registry.register(BuiltInRegistries.MENU, Unicopia.id(name), type);
    }

    static void bootstrap() { }
}
