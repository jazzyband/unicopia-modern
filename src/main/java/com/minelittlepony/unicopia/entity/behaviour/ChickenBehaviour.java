package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ChickenBehaviour extends EntityBehaviour<Chicken> {

    @Override
    protected boolean skipSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.OFFHAND;
    }

    @Override
    public void update(Living<?> source, Chicken entity, Disguise spell) {
        entity.eggTime = Integer.MAX_VALUE;

        if (source instanceof Pony player) {
            if (player.sneakingChanged()) {
                ItemStack egg = entity.getItemBySlot(EquipmentSlot.OFFHAND);

                if (player.asEntity().isShiftKeyDown()) {
                    if (egg.isEmpty()) {
                        egg = new ItemStack(Items.EGG);

                        int slot = player.asEntity().getInventory().findSlotMatchingCraftingIngredient(egg.getItem().builtInRegistryHolder(), egg);
                        if (slot > -1) {
                            player.asEntity().getInventory().removeItem(slot, 1);
                            entity.playSound(USounds.Vanilla.CHICKEN_EGG,
                                    1,
                                    (entity.level().getRandom().nextFloat() - entity.level().getRandom().nextFloat()) * 0.2F + 4
                            );
                            entity.setItemSlot(EquipmentSlot.OFFHAND, egg);
                        }
                    }
                } else if (egg.is(Items.EGG)) {
                    entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    entity.eggTime = 0;
                }
            }
        }

        Entity src = source.asEntity();

        if (src.onGround() || src instanceof Player player && player.getAbilities().flying) {
            return;
        }

        Vec3 vel = src.getDeltaMovement();

        if (vel.y < 0) {
            src.setDeltaMovement(vel.multiply(1, 0.8, 1));
        }

    }
}
