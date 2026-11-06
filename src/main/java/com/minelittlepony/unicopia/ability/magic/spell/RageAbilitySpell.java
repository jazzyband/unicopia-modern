package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation.Recipient;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Internal.
 * <p>
 * Used by the Rage ability.
 */
public class RageAbilitySpell extends AbstractSpell {
    private int age;
    private int ticksExtenguishing;

    private int ticksToExtenguish;

    public RageAbilitySpell(CustomisedSpellType<?> type) {
        super(type);
        setHidden(true);
    }

    public void setExtenguishing() {
        ticksToExtenguish += 15;
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation != Situation.BODY || source.asEntity().isRemoved() || !source.canUse(Abilities.RAGE)) {
            return false;
        }

        if (source.asEntity().isInWaterOrRain() || source.asEntity().isFullyFrozen() || ticksToExtenguish > 0) {
            ticksExtenguishing++;
            source.playSound(USounds.Vanilla.GENERIC_EXTINGUISH_FIRE, 1);
            source.spawnParticles(ParticleTypes.CLOUD, 12);
        } else {
            ticksExtenguishing = 0;
        }

        if (ticksToExtenguish > 0) {
            ticksToExtenguish--;
        }

        if (ticksExtenguishing > 10) {
            return false;
        }

        BlockPos pos = source.getOrigin();

        if (!source.isClient()) {
            if (age == 0) {
                source.asWorld().explode(source.asEntity(), source.asWorld().damageSources().source(DamageTypes.FIREBALL, source.asEntity()), new ExplosionDamageCalculator(){
                    @Override
                    public boolean shouldBlockExplode(Explosion explosion, BlockGetter world, BlockPos pos, BlockState state, float power) {
                        return false;
                    }
                }, source.getOriginVector(), 3, true, ExplosionInteraction.MOB);

                if (source instanceof Pony pony) {
                    pony.setAnimation(Animation.ARMS_UP, Recipient.ANYONE, 12);
                }
                source.playSound(USounds.ENTITY_PLAYER_KIRIN_RAGE, 2F, 0.1F);
                source.asEntity().gameEvent(GameEvent.EXPLODE);
            }

            if (source.asEntity().onGround() && source.asWorld().isEmptyBlock(pos) && age % 10 == 0) {
                source.asWorld().setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
            }

            if (source instanceof Pony pony) {
                if (pony.asEntity().getAttackStrengthScale(0) == 0) {
                    LivingEntity adversary = pony.asEntity().getKillCredit();
                    if (adversary != null) {
                        adversary.igniteForSeconds(10);
                    }
                }
            }
        } else {
            if (age % 5 == 0) {
                source.spawnParticles(ParticleTypes.LAVA, 4);
                if (source instanceof Pony pony && !pony.asEntity().isSwinging()) {
                    source.subtractEnergyCost(Math.min(12, 3 + source.asEntity().getDeltaMovement().length() * 0.1));
                }
            }
        }

        if (source instanceof Pony pony) {
            if (pony.isClientPlayer() && (age == 0 || pony.asEntity().getAttackStrengthScale(0) == 0)) {
                InteractionManager.getInstance().playLoopingSound(source.asEntity(), InteractionManager.SOUND_KIRIN_RAGE, source.asWorld().getRandom().nextLong());
            }

            Bar energyBar = pony.getMagicalReserves().getEnergy();
            var energy = Math.min(1.01F, 0.5F + (age / 1000F));
            float newEnergy = energyBar.get() + energy;
            energyBar.set(Math.min(1.9F + Mth.sin(age / 3F) * 1.7F, newEnergy));

            float charge = pony.getMagicalReserves().getCharge().get();
            float cost = 1 + age / 1000F;
            if (charge > cost) {
                pony.getMagicalReserves().getCharge().add(-cost);
            } else {
                pony.getMagicalReserves().getMana().add(-cost);
            }
            if (pony.getMagicalReserves().getMana().get() <= 0) {
                return false;
            }
        }

        if (source.asWorld().isRainingAt(pos.above()) && source.asWorld().getRandom().nextInt(15) == 0) {
            source.playSound(USounds.Vanilla.GENERIC_EXTINGUISH_FIRE, 0.3F);
            source.spawnParticles(ParticleTypes.CLOUD, 3);
        }

        if (++age < 2 && source instanceof Living l) {
            l.setInvulnerabilityTicks(25);
        }
        return true;
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        compound.putInt("age", age);
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        age = compound.getIntOr("age", 0);
    }
}
