package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.entity.ItemTracker;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.Tickable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class CorruptionHandler implements Tickable {

    private final Pony pony;

    public CorruptionHandler(Pony pony) {
        this.pony = pony;
    }

    public boolean hasCorruptingMagic() {
        return pony.getSpellSlot().get(SpellPredicate.IS_CORRUPTING).isPresent() || AmuletItem.isApplicable(pony.asEntity(), UItems.ALICORN_AMULET);
    }

    @Override
    public void tick() {
        if (pony.isClient() || pony.asEntity().tickCount % 5 != 0) {
            return;
        }

        Player entity = pony.asEntity();
        RandomSource random = pony.asEntity().getRandom();

        if (!AmuletItem.isApplicable(entity, UItems.ALICORN_AMULET)) {
            if (entity.tickCount % (10 * ItemTracker.SECONDS) == 0) {
                if (random.nextInt(100) == 0) {
                    pony.getCorruption().add(-1);
                }

                if (entity.getHealth() >= entity.getMaxHealth() - 1 && !entity.getFoodData().needsFood()) {
                    pony.getCorruption().add(-random.nextInt(4));
                }
            }
        }

        if (pony.asEntity().tickCount % 100 == 0 && hasCorruptingMagic()) {
            pony.getCorruption().add(random.nextInt(4));
        }

        float corruptionPercentage = pony.getCorruption().getScaled(1);

        if (corruptionPercentage > 0.5F && random.nextFloat() < corruptionPercentage - 0.25F) {
            pony.findAllEntitiesInRange(10, e -> e instanceof LivingEntity && !((LivingEntity)e).hasEffect(UEffects.CORRUPT_INFLUENCE)).forEach(e -> {
                ((LivingEntity)e).addEffect(new MobEffectInstance(UEffects.CORRUPT_INFLUENCE, 100, 1));
                recover(0.10F);
            });
        }

        if (corruptionPercentage > 0.25F && random.nextInt(200) == 0) {
            if (!pony.asEntity().hasEffect(UEffects.BUTTER_FINGERS)) {
                pony.asEntity().addEffect(new MobEffectInstance(UEffects.BUTTER_FINGERS, 2100, 1));
                recover(0.25F);
            }
        }

        if (random.nextFloat() < corruptionPercentage) {
            pony.spawnParticles(ParticleTypes.ASH, 10);
        }
    }

    private void recover(float percentage) {
        pony.getCorruption().set((int)(pony.getCorruption().get() * (1 - percentage)));
        InteractionManager.getInstance().playLoopingSound(pony.asEntity(), InteractionManager.SOUND_HEART_BEAT, 0);
        MagicReserves reserves = pony.getMagicalReserves();
        reserves.getExertion().addPercent(10);
        reserves.getEnergy().add(10);
    }
}
