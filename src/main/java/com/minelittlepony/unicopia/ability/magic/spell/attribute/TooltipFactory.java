package com.minelittlepony.unicopia.ability.magic.spell.attribute;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;

public interface TooltipFactory {
    TooltipFactory EMPTY = (type, tooltip) -> {};

    void appendTooltip(CustomisedSpellType<?> type, List<Component> tooltip);

    static TooltipFactory of(TooltipFactory...lines) {
        return (type, tooltip) -> {
            for (var line : lines) {
                line.appendTooltip(type, tooltip);
            }
        };
    }

    static TooltipFactory of(Component line) {
        return (type, tooltip) -> tooltip.add(line);
    }

    default TooltipFactory conditionally(Predicate<SpellTraits> condition) {
        TooltipFactory self = this;
        return (type, tooltip) -> {
            if (condition.test(type.traits())) {
                self.appendTooltip(type, tooltip);
            }
        };
    }
}
