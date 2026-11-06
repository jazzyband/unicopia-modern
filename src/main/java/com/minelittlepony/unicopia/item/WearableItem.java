package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

public abstract class WearableItem extends Item {

    public WearableItem(Item.Properties settings, Holder<SoundEvent> equipSound) {
        super(configureEquipmentSlotSupplier(settings.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.OFFHAND)
                .setEquipSound(equipSound)
                .build())));
        DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
        TrinketsDelegate.getInstance(null).registerTrinket(this);
    }

    private static Item.Properties configureEquipmentSlotSupplier(Item.Properties settings) {
        if (TrinketsDelegate.hasTrinkets()) {
            return settings;
        }
        return settings.equipmentSlot((e, s) -> ((WearableItem)s.getItem()).getSlotType(s));
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        return TrinketsDelegate.getInstance(player).equipStack(player, player.getItemInHand(hand)) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public EquipmentSlot getSlotType(ItemStack stack) {
        return EquipmentSlot.OFFHAND;
    }

    public static boolean dispenseArmor(BlockSource pointer, ItemStack armor) {
        return pointer.level().getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(pointer.pos().relative(pointer.state().getValue(DispenserBlock.FACING))),
                    EntitySelector.NO_SPECTATORS
                )
                .stream()
                .filter(entity -> TrinketsDelegate.getInstance(entity).equipStack(entity, armor))
                .findFirst()
                .isPresent();
    }

    private static final DispenseItemBehavior DISPENSER_BEHAVIOR = new DefaultDispenseItemBehavior(){
        @Override
        protected ItemStack execute(BlockSource pointer, ItemStack stack) {
            return dispenseArmor(pointer, stack) ? stack : super.execute(pointer, stack);
        }
    };
}
