package com.minelittlepony.unicopia.item;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.compat.trinkets.SlotKey;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.AmuletSelectors;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.component.Issuer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.level.Level;

public class FriendshipBraceletItem extends WearableItem {

    public FriendshipBraceletItem(Item.Properties settings) {
        super(settings, ArmorMaterials.LEATHER.equipSound());
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!Issuer.isSigned(stack) && (
                   EquinePredicates.PLAYER_UNICORN.test(player)
                || EquinePredicates.RACE_CAN_INFLUENCE_WEATHER.test(player)
                || AmuletSelectors.PEARL_NECKLACE.test(player)
        )) {
            player.startUsingItem(hand);

            ItemStack result = Issuer.set(stack.copyWithCount(1), player);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            player.awardStat(Stats.ITEM_USED.get(this));
            player.playSound(USounds.ITEM_BRACELET_SIGN, 1, 1);

            if (stack.isEmpty()) {
                return InteractionResult.CONSUME;
            }
            if (!player.addItem(result) && world instanceof ServerLevel sw) {
                player.spawnAtLocation(sw, result);
            }
            return InteractionResult.CONSUME;
        }

        return super.use(world, player, hand);
    }

    @Override
    public EquipmentSlot getSlotType(ItemStack stack) {
        return Issuer.isSigned(stack) ? EquipmentSlot.CHEST : super.getSlotType(stack);
    }

    @Nullable
    public static String getSignatorName(ItemStack stack) {
        return Issuer.getSignatorName(stack);
    }

    @Nullable
    public static UUID getSignatorId(ItemStack stack) {
        return Issuer.getSignatorId(stack);
    }

    @Deprecated
    public static boolean isSigned(ItemStack stack) {
        return Issuer.isSigned(stack);
    }

    @Deprecated
    public static boolean isSignedBy(ItemStack stack, Player player) {
        return Issuer.isSignedBy(stack, player);
    }

    @Deprecated
    public static boolean isSignedBy(ItemStack stack, UUID player) {
        return Issuer.isSignedBy(stack, player);
    }

    public static boolean isComrade(Owned<?> caster, Entity entity) {
        return entity instanceof LivingEntity l && caster.getMasterId()
                .filter(id -> getWornBangles(l).anyMatch(stack -> Issuer.isSignedBy(stack.stack(), id)))
                .isPresent();
    }

    public static boolean isComrade(UUID signator, Entity entity) {
        return entity instanceof LivingEntity l && getWornBangles(l, stack -> Issuer.isSignedBy(stack, signator)).findAny().isPresent();
    }

    public static Stream<Pony> getPartyMembers(Caster<?> caster, double radius) {
        return Pony.stream(caster.findAllEntitiesInRange(radius, entity -> isComrade(caster, entity)));
    }

    private static final Predicate<ItemStack> IS_BANGLE = stack -> stack.is(UItems.FRIENDSHIP_BRACELET);

    public static Stream<TrinketsDelegate.EquippedStack> getWornBangles(LivingEntity entity) {
        return Stream.concat(
                TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.MAIN_GLOVE, IS_BANGLE),
                TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.SECONDARY_GLOVE, IS_BANGLE)
        );
    }

    public static Stream<TrinketsDelegate.EquippedStack> getWornBangles(LivingEntity entity, @Nullable Predicate<ItemStack> predicate) {
        predicate = predicate == null ? IS_BANGLE : IS_BANGLE.and(predicate);
        return Stream.concat(
                TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.MAIN_GLOVE, predicate),
                TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.SECONDARY_GLOVE, predicate)
        );
    }

    public static Stream<TrinketsDelegate.EquippedStack> getWornBangles(LivingEntity entity, SlotKey slot) {
        return TrinketsDelegate.getInstance(entity).getEquipped(entity, slot, IS_BANGLE);
    }
}
