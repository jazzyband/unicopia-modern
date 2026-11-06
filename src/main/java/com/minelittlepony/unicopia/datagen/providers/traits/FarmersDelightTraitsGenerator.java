package com.minelittlepony.unicopia.datagen.providers.traits;

import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.Identifier;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader.TraitStream;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader.TraitStream.TraitSet;
import com.minelittlepony.unicopia.datagen.FarmersDelightContent;

public class FarmersDelightTraitsGenerator extends TraitsGenerator {

    @Override
    public void generate(Provider registries, BiConsumer<Identifier, TraitStream> exporter) {
        exporter.accept(FarmersDelightContent.id("items/overworld/organic_plant_derived_artificial"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 3).with(Trait.ORDER, 1))
                .tag(FarmersDelightContent.CABINETS)
                .tag(FarmersDelightContent.CANVAS_SIGNS)
                .build());
    }

}
