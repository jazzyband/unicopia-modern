package com.minelittlepony.unicopia.item;

import java.util.List;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.parsing.packrat.Rule;

public class PolearmItem extends Item implements ItemDuck {
    public PolearmItem(ToolMaterial material, int damage, float speed, int range, Properties settings) {
        super(settings.durability(material.durability()).repairable(material.repairItems()).enchantable(material.enchantmentValue())
                .attributes(createToolAttributeModifiers(material, damage, speed).withModifierAdded(
                UEntityAttributes.EXTENDED_REACH_DISTANCE, new AttributeModifier(UItemModifierIds.ATTACK_RANGE_MODIFIER_ID, range, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
        ).withModifierAdded(
                UEntityAttributes.EXTENDED_ATTACK_DISTANCE, new AttributeModifier(UItemModifierIds.ATTACK_RANGE_MODIFIER_ID, range, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
        )).component(DataComponents.TOOL, createToolComponent()));
    }

    private static ItemAttributeModifiers createToolAttributeModifiers(ToolMaterial material, float attackDamage, float attackSpeed) {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage + material.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,
                new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .build();
    }

    @SuppressWarnings("deprecation")
    private static Tool createToolComponent() {
        return new Tool(
            List.of(
                Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15),
                Tool.Rule.overrideSpeed(BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK).getOrThrow(UTags.Blocks.POLEARM_MINEABLE), 1.5F)
            ), 1, 2, false
        );
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean tooNear = target.distanceTo(attacker) <= 2;
        target.knockback(0.15, attacker.getX() - target.getX(), attacker.getZ() - target.getZ(), attacker.damageSources().mobAttack(attacker), 0);
        Living.updateVelocity(target);
        if (tooNear) {
            attacker.knockback(attacker.getRandom().triangle(0.4, 0.2), target.getX() - attacker.getX(), target.getZ() - attacker.getZ(), attacker.damageSources().mobAttack(attacker), 0);
            Living.updateVelocity(attacker);
        }
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean tooNear = target.distanceTo(attacker) <= 2;
        stack.hurtAndBreak(tooNear ? 4 : 1, attacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public boolean canBeEnchantedWith(ItemStack stack, Holder<Enchantment> enchantment, EnchantingContext context) {
        return !enchantment.is(Enchantments.SWEEPING_EDGE.identifier()) && super.canBeEnchantedWith(stack, enchantment, context);
    }
}
