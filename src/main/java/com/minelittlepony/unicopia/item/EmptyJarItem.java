package com.minelittlepony.unicopia.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class EmptyJarItem extends BlockItem {
    public EmptyJarItem(Block block, Properties settings) {
        super(block, settings);
        TickableItem.registerTickCallback(this, item -> {
            ItemEntity entity = item.get().asEntity();

            entity.setPermanentlyInvulnerable(true);

            if (!entity.level().isClientSide()
                    && !entity.isRemoved()
                    && entity.getAge() > 100
                    && entity.level().isThundering()
                    && entity.level().canSeeSky(entity.blockPosition())
                    && entity.level().getRandom().nextInt(130) == 0) {
                LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(entity.level(), EntitySpawnReason.EVENT);
                lightning.snapTo(entity.getX(), entity.getY(), entity.getZ());

                entity.remove(RemovalReason.DISCARDED);
                entity.level().addFreshEntity(lightning);

                ItemEntity neu = EntityTypes.ITEM.create(entity.level(), EntitySpawnReason.TRIGGERED);
                neu.copyPosition(entity);
                neu.setItem(new ItemStack(this == UItems.RAIN_CLOUD_JAR ? UItems.STORM_CLOUD_JAR : UItems.LIGHTNING_JAR));
                neu.setPermanentlyInvulnerable(true);

                entity.level().addFreshEntity(neu);

                ItemEntity copy = EntityTypes.ITEM.create(entity.level(), EntitySpawnReason.TRIGGERED);
                copy.copyPosition(entity);
                copy.setPermanentlyInvulnerable(true);
                copy.setItem(entity.getItem());
                copy.getItem().shrink(1);

                entity.level().addFreshEntity(copy);
            }
            return InteractionResult.PASS;
        });
    }
}
