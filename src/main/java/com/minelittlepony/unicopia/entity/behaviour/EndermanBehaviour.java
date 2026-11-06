package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.Living;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class EndermanBehaviour extends EntityBehaviour<Enderman> {
    @Override
    public void update(Living<?> source, Enderman entity, Disguise spell) {
        entity.setPermanentlyInvulnerable(!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(source.asEntity()));
        if (source.asEntity().isShiftKeyDown() || source.asEntity().isSprinting()) {
            entity.setTarget(entity);
        } else {
            entity.setTarget(null);
        }

        ItemStack stack = source.asEntity().getItemInHand(InteractionHand.MAIN_HAND);
        if (stack.getItem() instanceof BlockItem bi) {
            entity.setCarriedBlock(bi.getBlock().defaultBlockState());
        } else {
            entity.setCarriedBlock(null);
        }

        //if (entity.hurtTime > 0) {
        /*    Vec3d teleportedPos = entity.getPos();

            if (!teleportedPos.equals(source.asEntity().getPos())) {
                source.asEntity().refreshPositionAfterTeleport(teleportedPos.x, teleportedPos.y, teleportedPos.z);
            }*/
        //}
    }
}
