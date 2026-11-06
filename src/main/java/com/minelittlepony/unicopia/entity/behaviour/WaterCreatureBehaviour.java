package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.Living;
import net.minecraft.world.entity.animal.fish.WaterAnimal;

public class WaterCreatureBehaviour extends EntityBehaviour<WaterAnimal> {
    @Override
    public void update(Living<?> source, WaterAnimal entity, Disguise spell) {

        if (source.asEntity().isInWaterOrRain()) {
            source.asEntity().setAirSupply(source.asEntity().getAirSupply() - 1);
            if (source.asEntity().getAirSupply() == -20) {
                source.asEntity().setAirSupply(0);
                source.asEntity().hurtServer(source.asServerWorld(), source.asEntity().damageSources().dryOut(), 2);
            }
        } else {
            source.asEntity().setAirSupply(300);
        }

    }
}
