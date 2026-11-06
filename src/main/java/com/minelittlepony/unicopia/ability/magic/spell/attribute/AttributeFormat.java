package com.minelittlepony.unicopia.ability.magic.spell.attribute;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.minelittlepony.unicopia.client.gui.ItemTraitsTooltipRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public enum AttributeFormat {
    REGULAR {
        @Override
        public String formatValue(float value) {
            return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value);
        }
    },
    TIME {
        @Override
        public String formatValue(float value) {
            return StringUtil.formatTickDuration((int)Math.abs(value), InteractionManager.getInstance().getTickRate());
        }
    },
    PERCENTAGE {
        @Override
        public String formatValue(float value) {
            return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format((int)(Math.abs(value) * 100)) + "%";
        }
    };

    public abstract String formatValue(float value);

    public MutableComponent getBase(Component attributeName, float value, float max, String comparison, ChatFormatting color) {
        return formatAttributeLine(Component.translatable(
                "attribute.modifier." + comparison + ".0",
                Component.translatable("item.unicopia.magic_staff.enchanted", formatValue(value), formatValue(max)),
                attributeName
        ).withStyle(color));
    }

    public MutableComponent getBase(Component attributeName, float value, String comparison, ChatFormatting color) {
        return formatAttributeLine(Component.translatable("attribute.modifier." + comparison + ".0", formatValue(value), attributeName).withStyle(color));
    }

    public Component get(Component attributeName, float value) {
        return getBase(attributeName, value, "equals", ChatFormatting.LIGHT_PURPLE);
    }

    public Component getRelative(float baseValue, float currentValue, boolean detrimental) {
        float difference = currentValue - baseValue;
        return Component.literal(" (" + (difference > 0 ? "+" : "-") + formatValue(this == PERCENTAGE ? difference / baseValue : difference) + ")").withStyle((detrimental ? difference : -difference) < 0 ? ChatFormatting.DARK_GREEN : ChatFormatting.RED);
    }

    static Component formatTraitDifference(Trait trait, float value) {
        boolean known = ItemTraitsTooltipRenderer.isKnown(trait);
        boolean canCast = UnicopiaClient.getClientPony() != null && UnicopiaClient.getClientPony().getObservedSpecies().canCast();
        Component name = canCast ? known
                ? trait.getName()
                : Component.translatable("spell_attribute.unicopia.added_trait.unknown").withStyle(ChatFormatting.YELLOW)
                : trait.getName().copy().withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.YELLOW);
        Component count = Component.literal(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value));
        return Component.translatable("spell_attribute.unicopia.added_trait." + ((value > 0) ? "plus" : "take"), name, count).withStyle(ChatFormatting.DARK_AQUA);
    }

    public static MutableComponent formatAttributeLine(Component attributeName) {
        return Component.literal(" ").append(attributeName).withStyle(ChatFormatting.LIGHT_PURPLE);
    }

}
