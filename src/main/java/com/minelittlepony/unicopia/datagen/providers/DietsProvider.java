package com.minelittlepony.unicopia.datagen.providers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.datagen.DataCollector;
import com.minelittlepony.unicopia.diet.DietProfile;
import com.minelittlepony.unicopia.diet.FoodGroup;
import com.minelittlepony.unicopia.diet.FoodGroupEffects;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.TagsProvider.TagLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class DietsProvider implements DataProvider {
    private final DataCollector<DietProfile> dietsCollector;
    private final DataCollector<FoodGroupEffects> categoriesCollector;

    private final CompletableFuture<TagLookup<Item>> itemTagLookup;

    public DietsProvider(FabricPackOutput output, TagsProvider<Item> tagProvider) {
        this.dietsCollector = new DataCollector<>(output.createPathProvider(PackOutput.Target.DATA_PACK, "diet/races"), DietProfile.CODEC);
        this.categoriesCollector = new DataCollector<>(output.createPathProvider(PackOutput.Target.DATA_PACK, "diet/food_groups"), FoodGroup.EFFECTS_CODEC);
        itemTagLookup = tagProvider.contentsGetter();
    }

    @Override
    public String getName() {
        return "Diets";
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return itemTagLookup.thenCompose(tagLookup -> {
            var diets = categoriesCollector.prime();
            Map<Identifier, Set<Identifier>> keyToGroupId = new HashMap<>();
            new FoodGroupsGenerator().generate((id, builder) -> {
                var attributes = builder.build();
                attributes.tags().forEach(key -> {
                    if (!tagLookup.contains(TagKey.create(Registries.ITEM, key.id()))) {
                        throw new IllegalArgumentException("Food group " + id + " references unknown item tag " + key.id());
                    }
                    keyToGroupId.computeIfAbsent(key.id(), i -> new HashSet<>()).add(id);
                });
                diets.accept(id, attributes);
            });
            var profiles = dietsCollector.prime();
            new DietProfileGenerator().generate((race, profile) -> {
                Identifier id = Race.REGISTRY.getKey(race);
                StringBuilder issues = new StringBuilder();
                profile.validate(issue -> {
                    issues.append(System.lineSeparator()).append(issue);
                }, categoriesCollector::isDefined);
                if (!issues.isEmpty()) {
                    throw new IllegalArgumentException("Diet profile " + id + " failed validation: " + issues.toString());
                }
                profiles.accept(id, profile);
            });
            keyToGroupId.forEach((tag, groups) -> {
               if (groups.size() > 1) {
                   throw new IllegalArgumentException("Multiple groups referenced the same tag " + tag + " held by "
                           + groups.stream().map(Identifier::toString).collect(Collectors.joining())
                   );
               }
            });

            return CompletableFuture.allOf(categoriesCollector.upload(writer), dietsCollector.upload(writer));
        });
    }
}
