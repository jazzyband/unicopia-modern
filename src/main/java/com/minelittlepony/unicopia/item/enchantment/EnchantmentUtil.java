package com.minelittlepony.unicopia.item.enchantment;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.util.RegistryUtils;

public interface EnchantmentUtil {
    static boolean consumeEnchantment(Holder<Enchantment> enchantment, int levels, ItemStack stack) {
        return consumeEnchantment(enchantment, levels, stack, null, 0);
    }

    static boolean consumeEnchantment(Holder<Enchantment> enchantment, int levels, ItemStack stack, @Nullable RandomSource random, int chance) {
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(stack));
        int level = enchantments.getLevel(enchantment);
        if (level <= 0) {
            return false;
        }

        if (random == null || chance <= 1 || random.nextInt(chance) == 0) {
            enchantments.set(enchantment, Math.max(0, level - levels));
            EnchantmentHelper.setEnchantments(stack, enchantments.toImmutable());
        }
        return true;
    }


    static boolean prefersEquipment(ItemStack newStack, ItemStack oldStack) {
        int newLevel = getWantItNeedItLevel(newStack);
        int oldLevel = getWantItNeedItLevel(oldStack);
        return newLevel > oldLevel;
    }

    static int getWantItNeedItLevel(Entity entity) {
        return entity instanceof LivingEntity l ? getWantItNeedItLevel(l)
             : entity instanceof ItemEntity i ? getWantItNeedItLevel(i)
             : entity instanceof ItemSupplier p ? getWantItNeedItLevel(p.getItem())
             : 0;
    }

    static int getWantItNeedItLevel(ItemEntity entity) {
        return getLevel(UEnchantments.WANT_IT_NEED_IT, entity.getItem());
    }

    static int getWantItNeedItLevel(LivingEntity entity) {
        return getLevel(UEnchantments.WANT_IT_NEED_IT, entity);
    }

    static int getWantItNeedItLevel(ItemStack stack) {
        return getLevel(UEnchantments.WANT_IT_NEED_IT, stack);
    }


    static int getLuck(int baseline, LivingEntity entity) {
        boolean naturallyLucky = Living.getOrEmpty(entity).filter(c -> c.getCompositeRace().canUseEarth()).isPresent();
        if (naturallyLucky) {
            baseline += 15;
        }
        float luckAmplifier = getEffectAmplifier(entity, MobEffects.LUCK) * (naturallyLucky ? 2 : 1);
        float dolphinsGraceAmplifier = getEffectAmplifier(entity, MobEffects.DOLPHINS_GRACE) * 0.5F;
        float unluckAmplifier = getEffectAmplifier(entity, MobEffects.UNLUCK) * (naturallyLucky ? 0.5F : 1);
        float badOmenAmplifier = getEffectAmplifier(entity, MobEffects.BAD_OMEN) * (naturallyLucky ? 1 : 2);
        return (int)Mth.clamp(baseline + luckAmplifier + dolphinsGraceAmplifier - unluckAmplifier - badOmenAmplifier, -10, 10);
    }

    static int getLevel(ResourceKey<Enchantment> enchantment, ItemStack stack) {
        var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        return enchantments.keySet().stream()
                .filter(entry -> entry.is(enchantment)).map(enchantments::getLevel).findFirst().orElse(0);
    }

    private static boolean forEachEnchantment(ItemStack stack, EquipmentSlot slot, LivingEntity entity, Predicate<Holder<Enchantment>> consumer) {
        if (!stack.isEmpty()) {
            ItemEnchantments component = stack.get(DataComponents.ENCHANTMENTS);
            if (component != null && !component.isEmpty()) {
                for (var entry : component.entrySet()) {
                    Holder<Enchantment> enchantment = entry.getKey();
                    if (enchantment.value().matchingSlot(slot)) {
                        if (consumer.test(enchantment)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private static boolean forEachEnchantment(LivingEntity entity, Predicate<Holder<Enchantment>> predicate) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (forEachEnchantment(entity.getItemBySlot(slot), slot, entity, predicate)) {
                return true;
            }
        }
        return false;
    }

    static boolean hasAnyEnchantmentsIn(LivingEntity user, TagKey<Enchantment> tag) {
        return forEachEnchantment(user, enchantment -> enchantment.is(tag));
    }

    static boolean hasAnyEnchantmentsWith(LivingEntity user, DataComponentType<?> componentType) {
        return forEachEnchantment(user, enchantment -> enchantment.value().effects().has(componentType));
    }

    static int getLevel(LivingEntity user, TagKey<Enchantment> tag) {
        return RegistryUtils.entriesForTag(user.level(), tag)
                .stream()
                .mapToInt(entry -> EnchantmentHelper.getEnchantmentLevel(entry, user)).sum();
    }

    static int getGemfinderLevel(LivingEntity user) {
        return getLevel(UEnchantments.GEM_FINDER, user);
    }

    @Deprecated
    static int getLevel(ResourceKey<Enchantment> enchantment, LivingEntity entity) {
        return entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(enchantment.identifier())
                .map(entry -> EnchantmentHelper.getEnchantmentLevel(entry, entity))
                .orElse(0);
    }

    private static int getTotalLevel(ResourceKey<Enchantment> enchantment, LivingEntity entity) {
        return entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(enchantment.identifier())
                .map(entry -> getTotalEquipmentLevel(entry, entity))
                .orElse(0);
    }

    static int getTotalEquipmentLevel(Holder<Enchantment> enchantment, LivingEntity entity) {
        int level = 0;
        for (ItemStack stack : enchantment.value().getSlotItems(entity).values()) {
            level += EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
        }
        return level;
    }

    static int getEffectAmplifier(LivingEntity entity, Holder<MobEffect> effect) {
        return entity.hasEffect(effect) ? entity.getEffect(effect).getAmplifier() : 0;
    }

    static float getWeight(LivingEntity entity) {
        return 1 + getTotalLevel(UEnchantments.HEAVY, entity);
    }

    static float getWindBuffetResistance(LivingEntity entity) {
        return 1 + (getTotalLevel(UEnchantments.HEAVY, entity) * 0.8F) + (EquinePredicates.PLAYER_EARTH.test(entity) ? 1 : 0);
    }

    static float getImpactReduction(LivingEntity entity) {
        return 1 + (getTotalLevel(UEnchantments.PADDED, entity) / 6F);
    }

    static float getBouncyness(LivingEntity entity) {
        return getTotalLevel(UEnchantments.PADDED, entity) * 6;
    }

    static float getAirResistance(LivingEntity entity) {
        return 1 + getTotalLevel(UEnchantments.HEAVY, entity) * 0.009F;
    }

    @FunctionalInterface
    interface ContextAwareConsumer {
        boolean accept(Holder<Enchantment> enchantment, int level, EnchantedItemInUse context);
    }
}
