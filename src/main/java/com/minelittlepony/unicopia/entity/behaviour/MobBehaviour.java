package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.TraceHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.item.Items;

public class MobBehaviour<T extends Mob> extends EntityBehaviour<T> {

    private T dummy;

    @Override
    public void onDestroy(T entity) {
        entity.setNoAi(false);
        super.onDestroy(entity);
    }

    @Override
    public void update(Pony player, T entity, Disguise spell) {
        if (player.sneakingChanged() && isSneakingOnGround(player)) {
            LivingEntity target = findTarget(player, entity);
            entity.doHurtTarget(player.asServerWorld(), target);
            target.setLastHurtByMob(player.asEntity());
        }

        if (entity instanceof IronGolem i) {
            boolean hasPoppy = player.asEntity().getItemInHand(InteractionHand.MAIN_HAND).is(Items.POPPY);
            if (hasPoppy != i.getOfferFlowerTick() > 0) {
                i.offerFlower(hasPoppy);
            }
        }
    }

    protected LivingEntity findTarget(Pony player, T entity) {
        return TraceHelper.<LivingEntity>findEntity(player.asEntity(), 6, 1,
                e -> e instanceof LivingEntity && e != entity && !player.isOwnedBy(e))
                .orElseGet(() -> getDummy(entity));
    }

    @SuppressWarnings("unchecked")
    protected T getDummy(T entity) {
        if (dummy == null) {
            dummy = (T)entity.getType().create(entity.level(), EntitySpawnReason.CONVERSION);
        }

        return dummy;
    }
}
