package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.entity.Living;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record GroupBasedAttributeEnchantmentEffect (
        EnchantmentAttributeEffect attribute,
        LevelBasedValue range,
        TagKey<Enchantment> validTeamMateEnchantmentKey
    ) implements EnchantmentEntityEffect {
    public static final MapCodec<GroupBasedAttributeEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EnchantmentAttributeEffect.MAP_CODEC.codec().fieldOf("attribute").forGetter(GroupBasedAttributeEnchantmentEffect::attribute),
            LevelBasedValue.CODEC.fieldOf("range").forGetter(GroupBasedAttributeEnchantmentEffect::range),
            TagKey.hashedCodec(Registries.ENCHANTMENT).fieldOf("valid_teammate_enchantment_key").forGetter(GroupBasedAttributeEnchantmentEffect::validTeamMateEnchantmentKey)
    ).apply(instance, GroupBasedAttributeEnchantmentEffect::new));

    @Override
    public MapCodec<GroupBasedAttributeEnchantmentEffect> codec() {
        return CODEC;
    }

    @Override
    public void apply(ServerLevel world, int level, EnchantedItemInUse context, Entity user, Vec3 pos) {
        Living.getOrEmpty(user).ifPresent(living -> {
            int collectiveLevels = living.findAllEntitiesInRange(range.calculate(level), e -> e instanceof LivingEntity)
                    .mapToInt(e -> EnchantmentUtil.getLevel((LivingEntity)e, validTeamMateEnchantmentKey))
                    .reduce(Integer::sum)
                    .orElse(0);

            living.updateAttributeModifier(attribute.id(), attribute.attribute(), attribute.amount().calculate(collectiveLevels), value -> {
                return new AttributeModifier(attribute.id(), value, attribute.operation());
            }, false);
        });
    }

    @Override
    public void onDeactivated(EnchantedItemInUse context, Entity user, Vec3 pos, int level) {
        Living.getOrEmpty(user).ifPresent(living -> {
            living.updateAttributeModifier(attribute.id(), attribute.attribute(), 0, value -> {
                return new AttributeModifier(attribute.id(), value, attribute.operation());
            }, false);
        });
    }
}
