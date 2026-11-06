package com.minelittlepony.unicopia.compat.trinkets;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.container.inventory.InventorySlot;
import io.wispforest.accessories.api.menu.AccessoriesBasedSlot;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

class SpellbookTrinketSlot extends InventorySlot {
    private final AccessoriesBasedSlot slot;

    SpellbookTrinketSlot(SpellbookScreenHandler handler, AccessoriesBasedSlot accessoriesSlot) {
        super(handler, accessoriesSlot.container, accessoriesSlot.index, accessoriesSlot.x, accessoriesSlot.y);
        slot = accessoriesSlot;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        slot.onTake(player, stack);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return slot.mayPlace(stack);
    }

    @Override
    public ItemStack getItem() {
        return slot.getItem();
    }

    @Override
    public boolean hasItem() {
        return slot.hasItem();
    }

    @Override
    public void setByPlayer(ItemStack stack) {
        slot.setByPlayer(stack);
    }

    @Override
    public void set(ItemStack stack) {
        slot.set(stack);
    }

    @Override
    public void setChanged() {
        slot.setChanged();
    }

    @Deprecated
    @Override
    public int getMaxStackSize() {
        return slot.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return slot.getMaxStackSize(stack);
    }

    @Override
    public ItemStack remove(int amount) {
        return slot.remove(amount);
    }

    @Override
    public boolean isActive() {
        return super.isActive() && slot.isActive();
    }

    @Override
    public boolean mayPickup(Player playerEntity) {
        return slot.mayPickup(playerEntity);
    }

    @Override
    public Identifier getNoItemIcon() {
        return slot.getNoItemIcon();
    }

    @Override
    public Identifier getForegroundIdentifier() {
        return slot.getNoItemIcon();
    }

    @Override
    public boolean isTrinket() {
        return true;
    }
}
