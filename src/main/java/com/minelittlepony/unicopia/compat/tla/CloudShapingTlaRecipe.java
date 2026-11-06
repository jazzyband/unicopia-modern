package com.minelittlepony.unicopia.compat.tla;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import com.minelittlepony.unicopia.recipe.CloudShapingRecipe;
import com.minelittlepony.unicopia.recipe.URecipes;

import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaRecipe;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;

public class CloudShapingTlaRecipe implements TlaRecipe {
    private final Identifier id;
    private final RecipeCategory category;
    private final TlaIngredient input;
    private final TlaStack output;

    static void generate(RecipeCategory category, PluginContext context) {
        context.addRecipeGenerator(URecipes.CLOUD_SHAPING, recipe -> new CloudShapingTlaRecipe(category, recipe));
    }

    public CloudShapingTlaRecipe(RecipeCategory category, RecipeHolder<CloudShapingRecipe> recipe) {
        this.id = recipe.id().identifier();
        this.category = category;
        input = TlaIngredient.ofIngredient(recipe.value().input());
        output = TlaStack.of(recipe.value().result());
    }

    @Override
    public RecipeCategory getCategory() {
        return category;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public List<TlaIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<TlaStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public List<TlaIngredient> getCatalysts() {
        return List.of();
    }

    @Override
    public void buildGui(GuiBuilder builder) {
        builder.addArrow(26, 1, false);
        builder.addSlot(input, 0, 0).markInput();
        builder.addSlot(output, 58, 0).markOutput();
    }
}