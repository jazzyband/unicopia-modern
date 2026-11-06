package com.minelittlepony.unicopia.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.EquipmentUtil;

public class CuringJokeItem extends BlockItem {
    static final List<Predicate<LivingEntity>> EFFECTS = List.of(
            CuringJokeItem::restoreAir,
            CuringJokeItem::restoreHunger,
            CuringJokeItem::restoreHealth,
            CuringJokeItem::removeEffect,
            CuringJokeItem::uncurseItem,
            CuringJokeItem::repairItem
    );

    public CuringJokeItem(Block block, Properties settings) {
        super(block, settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        var items = new ArrayList<>(EFFECTS);
        while (!items.isEmpty()) {
            if (items.remove(world.getRandom().nextInt(items.size())).test(user)) {
                ParticleUtils.spawnParticles(new MagicParticleEffect(0x3388EE), user, 25);
                world.playSound(null, user.blockPosition(), USounds.ITEM_CURING_JOKE_CURE, user.getSoundSource(), 1, 1);
                break;
            }
        }

        return super.finishUsingItem(stack, world, user);
    }

    static boolean restoreAir(LivingEntity user) {
        if (user.getAirSupply() < user.getMaxAirSupply()) {
            user.setAirSupply(user.getMaxAirSupply());
            return true;
        }
        return false;
    }

    static boolean restoreHunger(LivingEntity user) {
        if (user instanceof Player player && player.getFoodData().getFoodLevel() < 20) {
            player.getFoodData().eat(20, 0);
            return true;
        }
        return false;
    }

    static boolean restoreHealth(LivingEntity user) {
        if (user.getHealth() < user.getMaxHealth()) {
            user.setHealth(user.getMaxHealth());
            return true;
        }
        return false;
    }

    static boolean removeEffect(LivingEntity user) {
        return user.getActiveEffects().stream().filter(effect -> {
            return !effect.getEffect().value().isBeneficial();
        }).findAny().filter(effect -> {
            user.removeEffect(effect.getEffect());
            return true;
        }).isPresent();
    }

    static boolean uncurseItem(LivingEntity user) {
        return EquipmentUtil.getInventory(user)
                .filter(s -> {
                    var enchantments = s.get(DataComponents.ENCHANTMENTS);
                    return enchantments != null && enchantments.keySet().stream().anyMatch(entry -> entry.is(EnchantmentTags.CURSE));
                })
                .findAny()
                .map(s -> {
                    EnchantmentHelper.updateEnchantments(s, builder -> builder.removeIf(entry -> entry.is(EnchantmentTags.CURSE)));
                    return s;
                }).isPresent();
    }

    static boolean repairItem(LivingEntity user) {
        return EquipmentUtil.getInventory(user)
                .filter(s -> s.getDamageValue() < s.getMaxDamage())
                .findAny().filter(s -> {
                    s.setDamageValue(0);
                    return true;
                }).isPresent();
    }
}
