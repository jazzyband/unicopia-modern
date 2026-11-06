package com.minelittlepony.unicopia.container;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.container.inventory.*;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.URecipes;

public class SpellbookScreenHandler extends RecipeBookMenu {
    private static final Identifier[] EMPTY_ARMOR_SLOT_TEXTURES = new Identifier[]{
            InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS,
            InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
            InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
            InventoryMenu.EMPTY_ARMOR_SLOT_HELMET
    };

    private final int MAX_INGREDIENTS;

    public final int GEM_SLOT_INDEX;
    private final int HOTBAR_START;
    private final int HOTBAR_END;

    private final SpellbookInventory input;

    public InputSlot gemSlot;
    public OutputSlot outputSlot;
    private final ResultContainer result = new ResultContainer();

    private final Inventory inventory;

    private final ContainerLevelAccess context;

    private final SpellbookState state;

    @Nullable
    public UUID entityId;

    protected SpellbookScreenHandler(int syncId, Inventory inv, SpellbookState state) {
        this(syncId, inv, ContainerLevelAccess.NULL, state, null);
    }

    public SpellbookScreenHandler(int syncId, Inventory inv, ContainerLevelAccess context, SpellbookState state, UUID entityId) {
        super(UScreenHandlers.SPELL_BOOK, syncId);
        this.entityId = entityId;
        this.state = state;
        inventory = inv;
        this.context = context;

        List<HexagonalCraftingGrid.Slot> grid = new ArrayList<>();
        List<HexagonalCraftingGrid.Slot> gemPos = new ArrayList<>();
        HexagonalCraftingGrid.create(34, 65, 3, grid, gemPos);

        MAX_INGREDIENTS = grid.size();
        GEM_SLOT_INDEX = MAX_INGREDIENTS;
        HOTBAR_START = GEM_SLOT_INDEX + 1;
        HOTBAR_END = HOTBAR_START + 9;

        input = new SpellbookInventory(this, MAX_INGREDIENTS + 1, 1);

        for (int i = 0; i < MAX_INGREDIENTS; i++) {
            var pos = grid.get(i);
            addSlot(new IngredientSlot(this, input, i, pos));
        }

        addSlot(gemSlot = new InputSlot(this, input, MAX_INGREDIENTS, gemPos.get(0)));

        final int slotSpacing = 18;
        final int halfSpacing = slotSpacing / 2;

        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventory, i, 121 + i * slotSpacing, 195));
        }

        final int inventoryX = 225;
        final int inventoryY = 45;

        for (int i = 0; i < Inventory.INVENTORY_SIZE - 9; ++i) {
            int x = i % 4;
            int y = i / 4;
            addSlot(new InventorySlot(this, inventory, i + 9, inventoryX + x * slotSpacing, inventoryY + y * slotSpacing));
        }


        final int armorX = 330;
        final int armorY = inventoryY + slotSpacing;
        final int equipmentY = armorY + halfSpacing;
        final int leftHandX = armorX - slotSpacing;
        final int rightHandX = armorX + slotSpacing;

        for (int i = 0; i < 4; i++) {
            final EquipmentSlot eq = EquipmentSlot.values()[5 - i];
            addSlot(new InventorySlot(this, inventory, Inventory.SLOT_OFFHAND - i - 1, armorX, armorY + (i * slotSpacing)) {
                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    Equippable equipment = stack.get(DataComponents.EQUIPPABLE);
                    return equipment != null && eq == equipment.slot();
                }

                @Override
                public boolean mayPickup(Player playerEntity) {
                    ItemStack stack = getItem();
                    if (!stack.isEmpty() && !playerEntity.isCreative() && EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
                        return false;
                    }
                    return super.mayPickup(playerEntity);
                }

                @Override
                public Identifier getNoItemIcon() {
                    return EMPTY_ARMOR_SLOT_TEXTURES[eq.getIndex()];
                }
            });
        }
        addSlot(new InventorySlot(this, inventory, Inventory.SLOT_OFFHAND, rightHandX, equipmentY + slotSpacing) {
            @Override
            public Identifier getNoItemIcon() {
                return InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
            }
        });

        TrinketsDelegate.getInstance(inv.player).createSlot(this, inv.player, TrinketsDelegate.FACE, 0, rightHandX, inventoryY + slotSpacing * 6).ifPresent(this::addSlot);
        TrinketsDelegate.getInstance(inv.player).createSlot(this, inv.player, TrinketsDelegate.NECKLACE, 0, leftHandX, equipmentY + slotSpacing).ifPresent(this::addSlot);
        TrinketsDelegate.getInstance(inv.player).createSlot(this, inv.player, TrinketsDelegate.MAIN_GLOVE, 0, leftHandX, equipmentY).ifPresent(this::addSlot);
        TrinketsDelegate.getInstance(inv.player).createSlot(this, inv.player, TrinketsDelegate.SECONDARY_GLOVE, 0, rightHandX, equipmentY).ifPresent(this::addSlot);

        addSlot(outputSlot = new OutputSlot(this, inventory.player, input, result, 0, gemPos.get(0)));

        addSlot(new SpellSlot(this, Pony.of(inventory.player), InteractionHand.MAIN_HAND, inventory, 0, leftHandX, equipmentY - slotSpacing));
        addSlot(new SpellSlot(this, Pony.of(inventory.player), InteractionHand.OFF_HAND, inventory, 0, rightHandX, equipmentY - slotSpacing));

        slotsChanged(input);
    }

    public SpellbookState getSpellbookState() {
        return state;
    }

    public boolean canShowSlots(SlotType type) {
        Identifier pageId = state.getCurrentPageId().orElse(null);
        boolean isCraftingPage = SpellbookState.CRAFTING_ID.equals(pageId);
        return switch (type) {
            case INVENTORY -> isCraftingPage ? state.getState(pageId).getOffset() == 0 : SpellbookState.PROFILE_ID.equals(pageId);
            case CRAFTING -> isCraftingPage;
        };
    }

    public int getOutputSlotId() {
        return outputSlot.index;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }

    @Override
    public void slotsChanged(Container inventory) {
        super.slotsChanged(inventory);
        context.execute((world, pos) -> {
            if (!world.isClientSide() && !gemSlot.getItem().isEmpty()) {
                Comparator<RecipeHolder<SpellbookRecipe>> comparator = Comparator.comparing(e -> e.value().getPriority());
                SpellbookRecipe.Input input = this.input.createInput();
                @SuppressWarnings("unchecked")
                ItemStack resultStack = input.hasIngredients() ? world.getServer().getRecipeManager()
                        .getRecipes().stream()
                        .filter(recipe -> recipe.value().getType() == URecipes.SPELLBOOK)
                        .map(recipe -> (RecipeHolder<SpellbookRecipe>)recipe)
                        .filter(recipe -> recipe.value().matches(input, world))
                        .sorted(comparator)
                        .findFirst()
                        .filter(recipe -> result.setRecipeUsed((ServerPlayer)this.inventory.player, recipe))
                        .map(recipe -> recipe.value().assemble(input))
                        .orElseGet(input::getFallbackStack) : ItemStack.EMPTY;
                outputSlot.setByPlayer(resultStack);

                setRemoteSlot(outputSlot.index, resultStack);
                ((ServerPlayer)this.inventory.player).connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), outputSlot.index, outputSlot.getItem()));
            }
        });
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot != null && slot.mayPlace(stack) && slot.isActive();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = slots.get(index);

        if (sourceSlot == null || !sourceSlot.hasItem() || (sourceSlot instanceof SpellSlot)) {
            return ItemStack.EMPTY;
        }

        ItemStack transferredStack = sourceSlot.getItem();
        ItemStack stack = transferredStack.copy();

        if (sourceSlot instanceof OutputSlot result) {
            result.onTake(player, stack);
        }

        if (index >= HOTBAR_START && !(sourceSlot instanceof OutputSlot || sourceSlot instanceof InputSlot)) {
            // hotbar or inventory -> crafting grid
            if (canShowSlots(SlotType.CRAFTING)) {
                if (!gemSlot.hasItem() && gemSlot.mayPlace(stack)) {
                    if (moveItemStackTo(transferredStack, GEM_SLOT_INDEX, GEM_SLOT_INDEX + 1, false)) {
                        slotsChanged(input);
                        return ItemStack.EMPTY;
                    }
                }

                if (moveItemStackTo(transferredStack, 0, GEM_SLOT_INDEX, false)) {
                    sourceSlot.onQuickCraft(transferredStack, stack);
                    slotsChanged(input);
                    return ItemStack.EMPTY;
                }
            }

            if (index < HOTBAR_END) {
                if (canShowSlots(SlotType.INVENTORY)) {
                    // hotbar -> inventory
                    // insert into inventory - armor
                    if (moveItemStackTo(transferredStack, HOTBAR_END + 27, HOTBAR_END + 27 + 4, false)) {
                        sourceSlot.onQuickCraft(transferredStack, stack);
                        slotsChanged(input);
                        return ItemStack.EMPTY;
                    }

                    // insert into inventory - inventory
                    if (moveItemStackTo(transferredStack, HOTBAR_END, HOTBAR_END + 27, false)) {
                        sourceSlot.onQuickCraft(transferredStack, stack);
                        slotsChanged(input);
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                // inventory -> hotbar
                if (moveItemStackTo(transferredStack, HOTBAR_START, HOTBAR_END, true)) {
                    sourceSlot.onQuickCraft(transferredStack, stack);
                    slotsChanged(input);
                    return ItemStack.EMPTY;
                }
            }
        } else {
            // crafting grid -> hotbar
            if (moveItemStackTo(transferredStack, HOTBAR_START, HOTBAR_END, true)) {
                sourceSlot.onQuickCraft(transferredStack, stack);
                slotsChanged(input);
                return ItemStack.EMPTY;
            }

            if (canShowSlots(SlotType.INVENTORY)) {
                // crafting grid -> armor
                if (moveItemStackTo(transferredStack, HOTBAR_END + 27, HOTBAR_END + 27 + 4, false)) {
                    sourceSlot.onQuickCraft(transferredStack, stack);
                    slotsChanged(input);
                    return ItemStack.EMPTY;
                }

                // crafting grid -> inventory
                if (moveItemStackTo(transferredStack, HOTBAR_END, HOTBAR_END + 27, false)) {
                    sourceSlot.onQuickCraft(transferredStack, stack);
                    slotsChanged(input);
                    return ItemStack.EMPTY;
                }
            }
        }

        if (transferredStack.getCount() == stack.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(player, transferredStack);

        return stack;
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput actionType, Player player) {
        if (slotIndex >= 0 && slotIndex < slots.size() && getSlot(slotIndex) instanceof SpellSlot slot) {
            ItemStack cursorStack = getCarried();
            if (actionType == ContainerInput.PICKUP && cursorStack.is(UItems.GEMSTONE) && cursorStack.getCount() == 1) {
                slot.swapSpells(cursorStack);
            }
        } else {
            super.clicked(slotIndex, button, actionType, player);
        }
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        boolean success = false;

        int i = fromLast ? endIndex - 1 : startIndex;

        while (true) {
            if (i < startIndex || i >= endIndex) {
                break;
            }

            Slot slot = getSlot(i);
            ItemStack current = slot.getItem();

            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(stack, current)) {
                // abide by the slot's max item count when trying to insert stacks
                int available = Math.min(Math.min(current.getMaxStackSize(), slot.getMaxStackSize(stack)) - current.getCount(), stack.getCount());

                if (available > 0) {
                    current.grow(available);
                    stack.shrink(available);
                    slot.setChanged();
                    success = true;
                }
            }

            i += fromLast ? -1 : 1;
        }

        i = fromLast ? endIndex - 1 : startIndex;

        while (true) {
            if (i < startIndex || i >= endIndex) {
                break;
            }

            Slot slot = getSlot(i);
            ItemStack current = slot.getItem();

            if (current.isEmpty() && slot.mayPlace(stack)) {
                if (stack.getCount() > slot.getMaxStackSize(stack)) {
                    slot.setByPlayer(stack.split(slot.getMaxStackSize(stack)));
                } else {
                    slot.setByPlayer(stack.split(stack.getCount()));
                }
                slot.setChanged();
                success = true;
                break;
            }

            i += fromLast ? -1 : 1;
        }

        return success;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        context.execute((world, pos) -> {
            clearContainer(player, input);
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public PostPlaceAction handlePlacement(boolean craftAll, boolean creative, RecipeHolder<?> recipe, ServerLevel world, Inventory inventory) {
        List<Slot> inputSlots = slots.stream().filter(slot -> slot instanceof IngredientSlot).toList();
        return ServerPlaceRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<SpellbookRecipe>() {
            @Override
            public void fillCraftSlotsStackedContents(StackedItemContents finder) {
                SpellbookScreenHandler.this.fillCraftSlotsStackedContents(finder);
            }

            @Override
            public void clearCraftingContent() {
                input.clearContent();
            }

            @Override
            public boolean recipeMatches(RecipeHolder<SpellbookRecipe> entry) {
                return entry.value().matches(SpellbookScreenHandler.this.input.createInput(), inventory.player.level());
            }
        }, inputSlots.size(), 1, inputSlots, inputSlots, inventory, (RecipeHolder<SpellbookRecipe>)recipe, craftAll, creative);
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents finder) {
        input.fillStackedContents(finder);
        for (ItemStack stack : inventory.getNonEquipmentItems()) {
            finder.accountStack(stack);
        }
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }
}
