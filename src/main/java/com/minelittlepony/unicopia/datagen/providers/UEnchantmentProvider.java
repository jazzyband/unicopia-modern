package com.minelittlepony.unicopia.datagen.providers;

import java.util.Optional;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.datagen.DataGenRegistryProvider;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.item.enchantment.AmbientSoundsEnchantmentEffect;
import com.minelittlepony.unicopia.item.enchantment.DangerSensingEnchantmentEffect;
import com.minelittlepony.unicopia.item.enchantment.GroupBasedAttributeEnchantmentEffect;
import com.minelittlepony.unicopia.item.enchantment.ParticleTrailEnchantmentEntityEffect;
import com.minelittlepony.unicopia.item.enchantment.Rarity;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;

public class UEnchantmentProvider extends DataGenRegistryProvider<Enchantment> {
    public UEnchantmentProvider() {
        super(Registries.ENCHANTMENT);
    }

    @Override
    public void run(BootstrapContext<Enchantment> registry) {
        var items = registry.lookup(Registries.ITEM);
        var enchantments = registry.lookup(Registries.ENCHANTMENT);

        register(registry, UEnchantments.GEM_FINDER, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.MINING_ENCHANTABLE),
                Rarity.RARE,
                3,
                Enchantment.dynamicCost(1, 11), Enchantment.constantCost(41),
                4,
                EquipmentSlotGroup.HAND
        )));

        register(registry, UEnchantments.PADDED, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.HEAD_ARMOR_ENCHANTABLE),
                Rarity.UNCOMMON,
                3,
                Enchantment.dynamicCost(1, 11), Enchantment.dynamicCost(12, 11),
                4,
                EquipmentSlotGroup.ARMOR
        )));
        register(registry, UEnchantments.FEATHER_TOUCH, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.MINING_ENCHANTABLE),
                Rarity.UNCOMMON,
                1,
                Enchantment.dynamicCost(12, 20), Enchantment.constantCost(31),
                3,
                EquipmentSlotGroup.HAND
        )));
        register(registry, UEnchantments.HEAVY, Enchantment.enchantment(
                Enchantment.definition(
                    items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    Rarity.RARE,
                    4,
                    Enchantment.dynamicCost(7, 11), Enchantment.constantCost(23),
                    2,
                    EquipmentSlotGroup.ARMOR
            )
        ).exclusiveWith(enchantments.getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE))
            .withEffect(EnchantmentEffectComponents.ATTRIBUTES, new EnchantmentAttributeEffect(
                Unicopia.id("enchantment.heaviness"),
                Attributes.MOVEMENT_SPEED,
                LevelBasedValue.perLevel(-0.1F, -0.1F),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            )
        ));
        register(registry, UEnchantments.HERDS, Enchantment.enchantment(
                Enchantment.definition(
                    items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                    Rarity.RARE,
                    3,
                    Enchantment.dynamicCost(8, 8), Enchantment.constantCost(20),
                    1,
                    EquipmentSlotGroup.MAINHAND
            )
        ).withEffect(EnchantmentEffectComponents.TICK, new GroupBasedAttributeEnchantmentEffect(new EnchantmentAttributeEffect(
                Unicopia.id("enchantment.team.strength"),
                Attributes.ATTACK_DAMAGE,
                LevelBasedValue.perLevel(0, 1),
                Operation.ADD_VALUE
        ), LevelBasedValue.perLevel(2, 2), UTags.Enchantments.HERDING)));

        register(registry, UEnchantments.REPULSION, Enchantment.enchantment(
                Enchantment.definition(
                    items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                    Rarity.VERY_RARE,
                    3,
                    Enchantment.dynamicCost(9, 9), Enchantment.dynamicCost(28, 8),
                    3,
                    EquipmentSlotGroup.FEET
            )
        ).exclusiveWith(enchantments.getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE))
            .withEffect(EnchantmentEffectComponents.ATTRIBUTES, new EnchantmentAttributeEffect(
                Unicopia.id("enchantment.repulsion"),
                UEntityAttributes.ENTITY_GRAVITY_MODIFIER,
                LevelBasedValue.perLevel(-0.5F, -0.375F),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            )
        ));

        register(registry, UEnchantments.WANT_IT_NEED_IT, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.VANISHING_ENCHANTABLE),
                Rarity.VERY_RARE,
                1,
                Enchantment.dynamicCost(9, 9), Enchantment.constantCost(10),
                4,
                EquipmentSlotGroup.ANY
        )).withEffect(EnchantmentEffectComponents.TICK, new ParticleTrailEnchantmentEntityEffect(Optional.empty(), 0.2F, 1, 10)));

        register(registry, UEnchantments.POISONED_JOKE, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.VANISHING_ENCHANTABLE),
                Rarity.VERY_RARE,
                1,
                Enchantment.dynamicCost(9, 9), Enchantment.constantCost(10),
                4,
                EquipmentSlotGroup.ANY
        )).withEffect(EnchantmentEffectComponents.TICK, new AmbientSoundsEnchantmentEffect(Unicopia.id("poisoned_joke_level"), UTags.Sounds.POISON_JOKE_EVENTS)));

        register(registry, UEnchantments.STRESSED, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.VANISHING_ENCHANTABLE),
                Rarity.VERY_RARE,
                3,
                Enchantment.dynamicCost(9, 9), Enchantment.dynamicCost(12, 3),
                4,
                EquipmentSlotGroup.ANY
        )).withEffect(EnchantmentEffectComponents.TICK, DangerSensingEnchantmentEffect.INSTANCE));

        register(registry, UEnchantments.CLINGY, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.EQUIPPABLE_ENCHANTABLE),
                Rarity.VERY_RARE,
                6,
                Enchantment.dynamicCost(9, 9), Enchantment.dynamicCost(12, 3),
                1,
                EquipmentSlotGroup.ANY
        )));

        register(registry, UEnchantments.HEART_BOUND, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.VANISHING_ENCHANTABLE),
                Rarity.UNCOMMON,
                5,
                Enchantment.dynamicCost(6, 9), Enchantment.dynamicCost(41, 7),
                3,
                EquipmentSlotGroup.ANY
        )));

        register(registry, UEnchantments.CONSUMPTION, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.MINING_ENCHANTABLE),
                Rarity.VERY_RARE,
                1,
                Enchantment.dynamicCost(10, 9), Enchantment.dynamicCost(71, 4),
                5,
                EquipmentSlotGroup.MAINHAND
        )));
    }

    static void register(BootstrapContext<Enchantment> registry, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        registry.register(key, builder.build(key.identifier()));
    }

    @Override
    protected void configureTags(TagProvider tagProvider, Provider lookup) {
        tagProvider.tag(EnchantmentTags.IN_ENCHANTING_TABLE).add(
                UEnchantments.GEM_FINDER, UEnchantments.PADDED, UEnchantments.FEATHER_TOUCH,
                UEnchantments.HEAVY, UEnchantments.HERDS, UEnchantments.CLINGY, UEnchantments.HEART_BOUND,
                UEnchantments.CONSUMPTION
        );
        tagProvider.tag(EnchantmentTags.TRADEABLE).add(
                UEnchantments.GEM_FINDER, UEnchantments.PADDED, UEnchantments.FEATHER_TOUCH,
                UEnchantments.HEAVY, UEnchantments.HERDS, UEnchantments.REPULSION, UEnchantments.WANT_IT_NEED_IT,
                UEnchantments.POISONED_JOKE, UEnchantments.STRESSED, UEnchantments.CLINGY, UEnchantments.CONSUMPTION
        );
        tagProvider.tag(EnchantmentTags.NON_TREASURE).add(
                UEnchantments.FEATHER_TOUCH, UEnchantments.HEAVY, UEnchantments.POISONED_JOKE
        );
        tagProvider.tag(EnchantmentTags.CURSE).add(
                UEnchantments.WANT_IT_NEED_IT, UEnchantments.POISONED_JOKE, UEnchantments.STRESSED
        );
        tagProvider.tag(UTags.Enchantments.HERDING).add(UEnchantments.HERDS);
        tagProvider.tag(UTags.Enchantments.CAN_BREAK_JARS_SAFELY).add(Enchantments.SILK_TOUCH);
        tagProvider.tag(UTags.Enchantments.CONVERTS_DROPS_TO_XP).add(UEnchantments.CONSUMPTION);
        tagProvider.tag(UTags.Enchantments.PERMITS_CLOUD_INTERACTION).add(UEnchantments.FEATHER_TOUCH);
    }
}
