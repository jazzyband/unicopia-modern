package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.item.component.Appearance;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemModelResolver.class)
abstract class MixinItemModels {
    @ModifyVariable(method = "appendItemLayers(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/ItemOwner;I)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private ItemStack modifyStack(ItemStack stack) {
        Appearance appearance = stack.get(UDataComponentTypes.APPEARANCE);
        if (appearance != null && appearance.replaceFully()) {
            return Appearance.upwrapAppearance(stack);
        }
        return stack;
    }
}
