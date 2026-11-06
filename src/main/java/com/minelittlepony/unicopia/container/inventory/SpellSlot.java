package com.minelittlepony.unicopia.container.inventory;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.EnchantableItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SpellSlot extends Slot implements SpellbookSlot {
    private final SpellbookScreenHandler handler;

    private final Pony pony;
    private final InteractionHand hand;

    public SpellSlot(SpellbookScreenHandler handler, Pony pony, InteractionHand hand, Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.handler = handler;
        this.pony = pony;
        this.hand = hand;
    }

    public CustomisedSpellType<?> getSpell() {
        return pony.getCharms().getEquippedSpell(hand);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isActive() {
       return handler.canShowSlots(SlotType.INVENTORY) && !handler.canShowSlots(SlotType.CRAFTING);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack getItem() {
        var spell = getSpell();
        return spell.isEmpty() ? Items.AIR.getDefaultInstance() : spell.getDefaultStack();
    }

    @Override
    public void set(ItemStack stack) {

    }

    public void swapSpells(ItemStack stack) {
        var result = EnchantableItem.consumeSpell(stack, pony.asEntity(), null, true);
        var spell = getSpell();
        spell.traits().applyTo(EnchantableItem.enchant(stack, spell.type()));
        pony.getCharms().equipSpell(hand, result.value());
    }

    @Override
    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setChanged() {
    }

    @Override
    @Nullable
    public Identifier getForegroundIdentifier() {
        return GEM;
    }

    @Override
    public boolean showTraits() {
        return false;
    }
}