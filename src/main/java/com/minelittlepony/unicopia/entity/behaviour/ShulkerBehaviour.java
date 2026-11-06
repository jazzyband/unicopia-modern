package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.mixin.MixinShulkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.phys.Vec3;

public class ShulkerBehaviour extends EntityBehaviour<Shulker> {
    @Override
    public void update(Living<?> source, Shulker shulker, Disguise spell) {
        shulker.setYRot(0);
        shulker.yBodyRotO = 0;
        shulker.setYBodyRot(0);

        super.update(source, shulker, spell);

        Direction attachmentFace = shulker.getAttachFace();
        BlockPos pos = shulker.blockPosition().relative(attachmentFace);

        boolean noGravity = !shulker.onGround() && !shulker.level().isEmptyBlock(pos)
                && (attachmentFace == Direction.UP || attachmentFace.getAxis() != Axis.Y);

        source.asEntity().setNoGravity(noGravity);
        if (noGravity && source.asEntity().isShiftKeyDown()) {
            Vec3 vel = source.asEntity().getDeltaMovement();
            if (vel.y > 0) {
                source.asEntity().setDeltaMovement(vel.multiply(1, 0.8, 1));
            }
        }
    }

    @Override
    protected void update(Pony player, Shulker shulker, Disguise spell) {
        float peekAmount = 30;

        double speed = !player.asEntity().isShiftKeyDown() ? 0.29 : 0;
        speed += Math.sqrt(player.asEntity().getDeltaMovement().horizontalDistanceSqr()) * 2;

        peekAmount = (float)Mth.clamp(speed, 0, 1);
        peekAmount = player.getInterpolator().interpolate("peek", peekAmount, 5);

        MixinShulkerEntity mx = (MixinShulkerEntity)shulker;

        mx.setPrevOpenProgress(mx.getCurrentPeekAmount());
        mx.setCurrentPeekAmount(peekAmount);

        if (player.sneakingChanged()) {
            mx.callSetPeekAmount((int)(peekAmount / 0.01F));
        } else if (peekAmount > 0.2 && mx.callGetPeekAmount() == 0) {
            if (shulker.isAlive() && shulker.level().getRandom().nextInt(1000) < shulker.ambientSoundTime++) {
                shulker.ambientSoundTime = -shulker.getAmbientSoundInterval();
                shulker.playSound(USounds.Vanilla.SHULKER_AMBIENT, 1, 1);
             }
        }
    }
}
