package com.minelittlepony.unicopia.container.inventory;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.client.sound.BufferedExecutor;
import com.minelittlepony.unicopia.container.*;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.InventoryUtil;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

public class OutputSlot extends ResultSlot implements SpellbookSlot {
    private final SpellbookScreenHandler handler;
    private final Player player;
    private final SpellbookInventory input;

    private final float weight;

    public OutputSlot(SpellbookScreenHandler spellbookScreenHandler, Player player, SpellbookInventory input, Container inventory, int index, HexagonalCraftingGrid.Slot params) {
        super(player, input, inventory, index, params.left(), params.top());
        handler = spellbookScreenHandler;
        this.player = player;
        this.input = input;
        this.weight = params.weight();
    }

    @Override
    public void setByPlayer(ItemStack stack) {
        if (!stack.isEmpty() && !ItemStack.matches(stack, getItem())) {
            BufferedExecutor.bufferExecution(player, () -> {
                player.level().playSound(null, player.blockPosition(), stack.getItem() == UItems.BOTCHED_GEM ? USounds.GUI_ABILITY_FAIL : USounds.GUI_SPELL_CRAFT_SUCCESS, SoundSource.MASTER, 1, 0.3F);
            });
        }
        super.setByPlayer(stack);
    }

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public boolean isActive() {
       return handler.canShowSlots(SlotType.CRAFTING) && hasItem();
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        Pony pony = Pony.of(player);
        InventoryUtil.stream(input).forEach(s -> {
            pony.getDiscoveries().unlock(s);
        });
        pony.getMagicalReserves().getXp().add(Mth.clamp(player.level().getRandom().nextFloat() / 10F, 0.001F, 0.3F));
        super.onTake(player, stack);
    }
}