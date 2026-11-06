package com.minelittlepony.unicopia.client;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.diet.PonyDiets;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.GlowableItem;
import com.minelittlepony.unicopia.item.component.MimicComponent;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;

public class ModifierTooltipRenderer {
    public static final ModifierTooltipRenderer INSTANCE = new ModifierTooltipRenderer();

    public void getTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, @Nullable Player player, TooltipFlag type, Consumer<Component> textConsumer) {
        @Nullable
        Pony pony = Pony.of(player);

        if (pony != null && displayComponent.shows(UDataComponentTypes.SPELL_TRAITS)) {
            pony.getDiscoveries().appendTooltip(stack, textConsumer);
        }

        appendTooltip(stack, UDataComponentTypes.CHARGES, context, displayComponent, textConsumer, type);
        appendTooltip(stack, UDataComponentTypes.ISSUER, context, displayComponent, textConsumer, type);
        appendTooltip(stack, UDataComponentTypes.BUTTERFLY_VARIANT, context, displayComponent, textConsumer, type);
        appendTooltip(stack, UDataComponentTypes.BALLOON_DESIGN, context, displayComponent, textConsumer, type);
        if (displayComponent.shows(UDataComponentTypes.MIMIC)) {
            MimicComponent.appendTooltip(stack, context, textConsumer, type);
        }
        if (displayComponent.shows(UDataComponentTypes.STORED_SPELL)) {
            EnchantableItem.getSpellEffect(stack).addToTooltip(context, textConsumer, type, stack);
        }
        if (displayComponent.shows(UDataComponentTypes.GLOWING) && GlowableItem.isGlowing(stack)) {
            textConsumer.accept(Component.translatable("item.unicopia.friendship_bracelet.glowing").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        }

        if (pony != null && displayComponent.shows(DataComponents.FOOD)) {
            PonyDiets.getInstance().getDiet(pony).appendTooltip(stack, pony, textConsumer, type);
        }
    }

    private void appendTooltip(ItemStack stack, DataComponentType<? extends TooltipProvider> componentType, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        appendTooltip(stack, componentType, context, displayComponent, textConsumer, type, null);
    }

    private void appendTooltip(ItemStack stack, DataComponentType<? extends TooltipProvider> componentType, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type, TooltipProvider fallback) {
        if (displayComponent.shows(componentType)) {
            TooltipProvider tooltipAppender = stack.getOrDefault(componentType, fallback);
            if (tooltipAppender != null) {
                tooltipAppender.addToTooltip(context, textConsumer, type, stack);
            }
        }
    }
}
