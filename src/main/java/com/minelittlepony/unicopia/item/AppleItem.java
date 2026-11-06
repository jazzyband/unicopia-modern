package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.IItemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface AppleItem {
    static <T extends Item> T registerTickCallback(T item) {
        return TickableItem.registerTickCallback(item, AppleItem::onGroundTick);
    }

    private static InteractionResult onGroundTick(IItemEntity item) {
        ItemEntity entity = item.get().asEntity();

        if (!entity.isRemoved() && item.getPickupDelay() == 0 && item.getAge() > 2030 && entity.level().getRandom().nextInt(150) < 10) {

            if (!entity.level().isClientSide()) {
                EntityTypes.ITEM.spawn((ServerLevel)entity.level(), neu -> {
                    neu.copyPosition(entity);
                    neu.setItem(new ItemStack(UItems.ROTTEN_APPLE));
                }, BlockPos.ZERO, EntitySpawnReason.CONVERSION, false, false);

                entity.tickCount = 0;
                entity.setNoPickUpDelay();
                entity.getItem().shrink(1);
            }

            float bob = Mth.sin(((float)item.getAge() + 1) / 10F + entity.bobOffs) * 0.1F + 0.1F;

            for (int i = 0; i < 3; i++) {
                entity.level().addParticle(net.minecraft.core.particles.SpellParticleOption.create(ParticleTypes.EFFECT, 0xFFFFFF, 1), entity.getX(), entity.getY() + bob, entity.getZ(),
                        entity.level().getRandom().nextGaussian() - 0.5F,
                        entity.level().getRandom().nextGaussian() - 0.5F,
                        entity.level().getRandom().nextGaussian() - 0.5F);
            }
            entity.playSound(USounds.ITEM_APPLE_ROT, 0.5F, 1.5F);
        }

        return InteractionResult.PASS;
    }
}
