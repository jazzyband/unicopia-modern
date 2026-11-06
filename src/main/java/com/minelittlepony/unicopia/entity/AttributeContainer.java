package com.minelittlepony.unicopia.entity;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;

public interface AttributeContainer {
    @Nullable
    AttributeInstance getAttributeInstance(Holder<Attribute> attribute);

    default void updateAttributeModifier(Identifier id, Holder<Attribute> attribute, float desiredValue, Float2ObjectFunction<AttributeModifier> modifierSupplier, boolean permanent) {
        @Nullable
        AttributeInstance instance = getAttributeInstance(attribute);
        if (instance == null) {
            return;
        }

        @Nullable
        AttributeModifier modifier = instance.getModifier(id);

        if (!Mth.equal(desiredValue, modifier == null ? 0 : modifier.amount())) {
            instance.removeModifier(id);

            if (desiredValue != 0) {
                if (permanent) {
                    instance.addPermanentModifier(modifierSupplier.get(desiredValue));
                } else {
                    instance.addTransientModifier(modifierSupplier.get(desiredValue));
                }
            }
        }
    }

    default void applyAttributeModifiers(Map<Holder<Attribute>, AttributeModifier> modifiers, boolean permanent, boolean apply) {
        modifiers.forEach((attribute, modifier) -> {
            applyAttributeModifier(attribute, modifier, permanent, apply);
        });
    }

    default void applyAttributeModifier(Holder<Attribute> attribute, AttributeModifier modifier, boolean permanent, boolean apply) {
        @Nullable
        AttributeInstance instance = getAttributeInstance(attribute);
        if (instance == null) {
            return;
        }

        @Nullable
        boolean present = instance.hasModifier(modifier.id());

        if (present != apply) {
            if (apply) {
                if (permanent) {
                    instance.addPermanentModifier(modifier);
                } else {
                    instance.addTransientModifier(modifier);
                }
            } else {
                instance.removeModifier(modifier.id());
            }
        }
    }
}
