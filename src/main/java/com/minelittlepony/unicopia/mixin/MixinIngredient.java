package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.minelittlepony.unicopia.recipe.URecipes;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.stream.Stream;

@Mixin(Ingredient.class)
abstract class MixinIngredient {
    @Shadow @Final @Mutable private HolderSet<Item> values;

    @Inject(method = "isEmpty", at = @At("HEAD"))
    private void unicopia$resolveStaticTag(CallbackInfoReturnable<Boolean> info) {
        unicopia$resolveStaticTag();
    }

    @Inject(method = "items", at = @At("HEAD"))
    private void unicopia$resolveStaticTagItems(CallbackInfoReturnable<Stream<Holder<Item>>> info) {
        unicopia$resolveStaticTag();
    }

    @Inject(method = "test", at = @At("HEAD"))
    private void unicopia$resolveStaticTagTest(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        unicopia$resolveStaticTag();
    }

    @Inject(method = "acceptsItem", at = @At("HEAD"))
    private void unicopia$resolveStaticTagAccepts(Holder<Item> item, CallbackInfoReturnable<Boolean> info) {
        unicopia$resolveStaticTag();
    }

    private void unicopia$resolveStaticTag() {
        values.unwrapKey().flatMap(BuiltInRegistries.ITEM::get).ifPresent(tag -> values = tag);
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"))
    private static HolderSet<Item> onInit(HolderSet<Item> entries) {
        return entries.unwrap().right().filter(stacks -> stacks.size() == 1).flatMap(stacks -> {
            return URecipes.getRecipeContentsReplacement(stacks.getFirst());
        }).orElse(entries);
    }
}
