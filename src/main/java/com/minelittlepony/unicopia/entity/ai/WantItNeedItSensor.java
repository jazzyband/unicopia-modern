package com.minelittlepony.unicopia.entity.ai;

import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import com.google.common.collect.ImmutableSet;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;

public class WantItNeedItSensor extends Sensor<LivingEntity> {

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }

    @Override
    protected void doTick(ServerLevel world, LivingEntity entity) {
        entity.getBrain().getMemoryInternal(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent(targets -> {
            entity.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, targets
                    .findClosest(e -> (EnchantmentUtil.getWantItNeedItLevel(e) * 10) >= entity.distanceTo(e)));
        });
    }

}
