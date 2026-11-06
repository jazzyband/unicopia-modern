package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.entity.mob.MimicEntity;
import com.minelittlepony.unicopia.item.component.MimicComponent;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.server.world.WaterLoggingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockItem.class)
abstract class MixinBlockItem extends Item {
    MixinBlockItem() {super(null); }

    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    private BlockState onGetPlacementState(BlockState state, BlockPlaceContext context) {
        return WaterLoggingManager.getInstance().getPlacementState(state, context);
    }

    @ModifyReturnValue(method = "updateCustomBlockEntityTag(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"))
    private static boolean onWriteNbtToBlockEntity(boolean returnValue, Level world, @Nullable Player player, BlockPos pos, ItemStack stack) {
        MimicComponent mimic = stack.get(UDataComponentTypes.MIMIC);
        if (mimic != null) {
            if (world.getBlockEntity(pos) instanceof MimicEntity.MimicGeneratable generatable) {
                generatable.setMimic(mimic.mimic());
            }
        }
        return returnValue;
    }
}
