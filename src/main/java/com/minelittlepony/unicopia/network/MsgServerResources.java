package com.minelittlepony.unicopia.network;

import java.util.*;

import com.minelittlepony.unicopia.ability.data.tree.TreeTypeLoader;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.container.spellbook.SpellbookChapter;
import com.minelittlepony.unicopia.container.spellbook.SpellbookChapterLoader;
import com.minelittlepony.unicopia.diet.PonyDiets;
import com.minelittlepony.unicopia.recipe.CloudShapingRecipe;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.minelittlepony.unicopia.util.Untyped;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.SelectableRecipe;

public record MsgServerResources (
        Map<ResourceKey<Item>, SpellTraits> traits,
        Map<Identifier, SpellbookChapter> chapters,
        Map<Identifier, TreeTypeLoader.TreeTypeDef> treeTypes,
        SelectableRecipe.SingleInputSet<CloudShapingRecipe> cloudCuttingRecipes,
        PonyDiets diets
    ) {
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgServerResources> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceKey.streamCodec(Registries.ITEM), SpellTraits.PACKET_CODEC), MsgServerResources::traits,
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, SpellbookChapter.PACKET_CODEC), MsgServerResources::chapters,
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, TreeTypeLoader.TreeTypeDef.PACKET_CODEC), MsgServerResources::treeTypes,
            SelectableRecipe.SingleInputSet.noRecipeCodec(), MsgServerResources::cloudCuttingRecipes,
            PonyDiets.PACKET_CODEC, MsgServerResources::diets,
            MsgServerResources::new
    );

    public MsgServerResources(MinecraftServer server) {
        this(
            SpellTraits.all(),
            SpellbookChapterLoader.INSTANCE.getChapters(),
            TreeTypeLoader.INSTANCE.getEntries(),
            getCloudCuttingRecipes(server),
            PonyDiets.getInstance()
        );
    }

    static SelectableRecipe.SingleInputSet<CloudShapingRecipe> getCloudCuttingRecipes(MinecraftServer server) {
        return new SelectableRecipe.SingleInputSet<>(server.getRecipeManager().getRecipes().stream()
                .filter(recipe -> recipe.value().getType() == URecipes.CLOUD_SHAPING && recipe.value() instanceof CloudShapingRecipe)
                .map(recipe -> new SelectableRecipe.SingleInputEntry<CloudShapingRecipe>(
                    ((CloudShapingRecipe)recipe.value()).input(),
                    new SelectableRecipe<>(((CloudShapingRecipe)recipe.value()).resultDisplay(), Untyped.cast(Optional.of(recipe)))
            )).toList());
    }
}
