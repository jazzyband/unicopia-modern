package com.minelittlepony.unicopia.item;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.level.ItemLike;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.item.component.Charges;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;

public class AmuletItem extends WearableItem {
    public static final Identifier AMULET_MODIFIERS_ID = Unicopia.id("amulet_modifiers");

    public AmuletItem(Item.Properties settings, int maxEnergy) {
        super(settings.component(UDataComponentTypes.CHARGES, Charges.of(maxEnergy, maxEnergy)), ArmorMaterials.IRON.equipSound());
    }

    public AmuletItem(Item.Properties settings) {
        super(settings, ArmorMaterials.IRON.equipSound());
    }

    public AmuletItem(Item.Properties settings, int maxEnergy, ItemAttributeModifiers modifiers) {
        this(settings.attributes(modifiers), maxEnergy);
    }

    @Deprecated
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        for (FormattedText line : Minecraft.getInstance().font.getSplitter().splitLines(
                Component.translatable(getDescriptionId() + ".lore"), 150, Style.EMPTY)) {
            MutableComponent compiled = Component.literal("").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
            line.visit(s -> {
                compiled.append(s);
                return Optional.empty();
            });
            textConsumer.accept(compiled);
        }
        super.appendHoverText(stack, context, displayComponent, textConsumer, type);
    }

    @Override
    public EquipmentSlot getSlotType(ItemStack stack) {
        return TrinketsDelegate.hasTrinkets() ? EquipmentSlot.OFFHAND : EquipmentSlot.CHEST;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.isEnchanted() || Charges.of(stack).maximum() == 0 || Charges.of(stack).energy() > 0;
    }

    public static boolean isApplicable(LivingEntity entity, ItemLike amulet) {
        return !getForEntity(entity, amulet).stack().isEmpty();
    }

    public static boolean isApplicable(ItemStack stack, ItemLike amulet) {
        return stack.is(amulet.asItem()) && (Charges.of(stack).maximum() == 0 || Charges.of(stack).energy() > 0);
    }

    public static TrinketsDelegate.EquippedStack getForEntity(LivingEntity entity, ItemLike amulet) {
        return TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.NECKLACE, stack -> isApplicable(stack, amulet))
                .findFirst()
                .orElse(TrinketsDelegate.EquippedStack.EMPTY);
    }

    // TODO: AmuletArmorComponent
    public static TrinketsDelegate.EquippedStack get(LivingEntity entity) {
        return TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.NECKLACE, stack -> stack.getItem() instanceof AmuletItem)
                .findFirst()
                .orElse(TrinketsDelegate.EquippedStack.EMPTY);
    }
}
