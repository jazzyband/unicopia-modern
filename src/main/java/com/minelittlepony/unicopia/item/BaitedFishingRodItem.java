package com.minelittlepony.unicopia.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BaitedFishingRodItem extends FishingRodItem {

    public BaitedFishingRodItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        @Nullable
        FishingHook oldFishHook = user.fishing;
        InteractionResult result = super.use(world, user, hand);
        if (!world.isClientSide()) {
            if (user.fishing != null) {
                user.fishing.discard();
                ItemStack stack = user.getItemInHand(hand);
                int lure = (int)((EnchantmentHelper.getFishingTimeReduction((ServerLevel)world, stack, user) + 1) * 20F);
                int luck = (EnchantmentHelper.getFishingLuckBonus((ServerLevel)world, stack, user) + 1) * 2;
                FishingHook bobber = new FishingHook(user, world, luck, lure);
                ((BaitedFishingBobber)bobber).setRodType(this);
                world.addFreshEntity(bobber);
            }

            if (!user.isCreative()) {
                ItemStack stack = user.getItemInHand(hand);
                if (oldFishHook != null
                        && oldFishHook.getHookedIn() == null
                        && oldFishHook.getEntityData().get(FishingHook.DATA_BITING)
                        && stack.is(this)) {
                    user.setItemInHand(hand, stack.transmuteCopy(Items.FISHING_ROD));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return result;
    }

    public interface BaitedFishingBobber {
        Item getRodType();

        void setRodType(Item rodType);
    }
}
