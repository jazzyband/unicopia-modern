package com.minelittlepony.unicopia.datagen.providers.traits;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader.TraitStream.Key;

public abstract class TraitsGenerator {
    public abstract void generate(Provider registries, BiConsumer<Identifier, TraitLoader.TraitStream> exporter);

    protected TraitLoader.TraitStream.TraitSet.Builder applyItemsWhere(TraitLoader.TraitStream.TraitSet.Builder builder, HolderLookup<Item> items, Predicate<String> idPredicate) {
        items.listElements().filter(i -> {
            String path = i.key().identifier().getPath();
            return idPredicate.test(path);
        }).forEach(i -> builder.item(i.value()));
        return builder;
    }

    protected static Key tag(TagKey<Item> tag) {
        return new Key.Tag(tag);
    }

    @SuppressWarnings("deprecation")
    protected static Key item(ItemLike item) {
        return new Key.Id(item.asItem().builtInRegistryHolder().key());
    }

    protected static SpellTraits.Builder boatTraits() {
        return SpellTraits.builder().with(Trait.KNOWLEDGE, 3).with(Trait.ORDER, 1);
    }
}
