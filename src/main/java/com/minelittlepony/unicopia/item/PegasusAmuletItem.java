package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.item.component.Charges;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PegasusAmuletItem extends AmuletItem {
    public PegasusAmuletItem(Item.Properties settings, int maxEnergy) {
        super(settings.component(UDataComponentTypes.CHARGES, Charges.of(maxEnergy / 2, maxEnergy)));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot slot) {
        if (entity.level().getGameTime() % 6 == 0 && entity instanceof LivingEntity living && isApplicable(living, this)) {
            ParticleUtils.spawnParticles(entity.level().dimension().equals(net.minecraft.world.level.Level.NETHER) ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.COMPOSTER, entity, 1);
        }
    }
}
