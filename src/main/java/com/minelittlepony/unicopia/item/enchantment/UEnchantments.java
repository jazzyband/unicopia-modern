package com.minelittlepony.unicopia.item.enchantment;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import com.minelittlepony.unicopia.Unicopia;

public interface UEnchantments {
    List<ResourceKey<Enchantment>> REGISTRY = new ArrayList<>();

    /**
     * Makes a sound when there are interesting blocks in your area.
     *
     *  EnchantmentTags.IN_ENCHANTING_TABLE
     *  EnchantmentTags.TRADEABLE
     */
    ResourceKey<Enchantment> GEM_FINDER = register("gem_finder");

    /**
     * Protects against wall collisions and earth pony attacks!
     *
     * EnchantmentTags.IN_ENCHANTING_TABLE
     * EnchantmentTags.TRADEABLE
     */
    ResourceKey<Enchantment> PADDED = register("padded");

    /**
     * Allows non-flying races to mine and interact with cloud blocks
     *
     *  EnchantmentTags.NON_TREASURE
     *  EnchantmentTags.IN_ENCHANTING_TABLE
     *  EnchantmentTags.TRADEABLE
     */
    ResourceKey<Enchantment> FEATHER_TOUCH = register("feather_touch");

    /**
     * Heavy players move more slowly but are less likely to be flung around wildly.
     *
     *  EnchantmentTags.NON_TREASURE
     *  EnchantmentTags.IN_ENCHANTING_TABLE
     *  EnchantmentTags.TRADEABLE
     */
    ResourceKey<Enchantment> HEAVY = register("heavy");

    /**
     * It's dangerous to go alone, take this!
     *
     * Weapons will become stronger the more allies you have around.
     *
     *  EnchantmentTags.IN_ENCHANTING_TABLE
     *  EnchantmentTags.TRADEABLE
     */
    ResourceKey<Enchantment> HERDS = register("herds");

    /**
     * Alters gravity
     *
     *  EnchantmentTags.TRADEABLE
     */
    ResourceKey<Enchantment> REPULSION = register("repulsion");

    /**
     * I want it, I neeeed it!
     *
     * Mobs really want your candy. You'd better give it to them.
     *
     *  EnchantmentTags.CURSE
     *  EnchantmentTags.TRADEABLE
     */
    ResourceKey<Enchantment> WANT_IT_NEED_IT = register("want_it_need_it");

    /**
     * Hahaha geddit?
     *
     * Random things happen.
     *
     *  EnchantmentTags.NON_TREASURE
     *  EnchantmentTags.CURSE
     *  EnchantmentTags.TRADEABLE
     */
    ResourceKey<Enchantment> POISONED_JOKE = register("poisoned_joke");

    /**
     * Who doesn't like a good freakout?
     *
     *  EnchantmentTags.CURSE
     *  EnchantmentTags.TRADEABLE
     */
    ResourceKey<Enchantment> STRESSED = register("stressed");

    /**
     * This item just wants to be held.
     *
     *  EnchantmentTags.IN_ENCHANTING_TABLE
     *  EnchantmentTags.TRADEABLE
     */
    ResourceKey<Enchantment> CLINGY = register("clingy");

    /**
     * Items with loyalty are kept after death.
     * Only works if they don't also have curse of binding.
     *
     *  EnchantmentTags.IN_ENCHANTING_TABLE
     */
    ResourceKey<Enchantment> HEART_BOUND = register("heart_bound");

    /**
     * Consumes drops whilst mining and produces experience instead
     *
     *  EnchantmentTags.IN_ENCHANTING_TABLE
     *  EnchantmentTags.TRADEABLE
     */
    ResourceKey<Enchantment> CONSUMPTION = register("consumption");

    static ResourceKey<Enchantment> register(String name) {
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, Unicopia.id(name));
        REGISTRY.add(key);
        return key;
    }

    static void bootstrap() {
        UEnchantmentEffects.bootstrap();
    }
}
