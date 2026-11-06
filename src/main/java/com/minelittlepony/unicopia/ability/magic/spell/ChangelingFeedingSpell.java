package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.AbstractSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ChangelingFeedingSpell extends AbstractSpell {
    private List<EntityReference<LivingEntity>> targets = new ArrayList<>();
    private int nextTargetIndex;

    private float healthToDrain;
    private int foodToDrain;

    private float damageThisTick;

    public ChangelingFeedingSpell(CustomisedSpellType<?> type) {
        super(type);
        setHidden(true);
    }

    public ChangelingFeedingSpell(List<LivingEntity> feedTarget, float healthToDrain, int foodToDrain) {
        this(SpellType.FEED.withTraits());
        this.targets = feedTarget.stream().map(EntityReference::new).collect(Collectors.toList() /* make mutable */);
        this.healthToDrain = healthToDrain;
        this.foodToDrain = foodToDrain;
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (!(source instanceof Pony changeling) || situation != Situation.BODY || !source.canUse(Abilities.FEED)) {
            return false;
        }

        Player player = changeling.asEntity();
        if (!canFeed(changeling)) {
            changeling.playSound(USounds.Vanilla.PLAYER_BURP, 1, player.level().getRandom().triangle(1F, 0.2F));
            return false;
        }

        float tickDrain = Math.min(0.05F, healthToDrain);
        damageThisTick += tickDrain;

        if (damageThisTick > 1) {
            damageThisTick--;

            float healAmount = drain(changeling, 1);
            float foodAmount = Math.min(healAmount / 3F, foodToDrain);
            if (foodAmount > 0) {
                healAmount -= foodAmount;
            }

            foodAmount = Mth.clamp(foodAmount, 0, foodToDrain);
            healAmount = Mth.clamp(healAmount, 0, healthToDrain);

            int shanks = Mth.floor(foodAmount);
            player.getFoodData().eat(shanks, foodAmount - shanks);
            player.heal(healAmount);

            if (!canFeed(changeling)) {
                changeling.playSound(USounds.Vanilla.PLAYER_BURP, 1, player.level().getRandom().triangle(1F, 0.2F));
            } else {
                changeling.playSound(USounds.ENTITY_PLAYER_CHANGELING_FEED.value(), 0.1F, changeling.getRandomPitch());
            }

            foodToDrain -= foodAmount;
            healthToDrain -= healAmount;
        }

        return !targets.isEmpty() && (healthToDrain > 0 || foodToDrain > 0);
    }

    private float drain(Pony changeling, float max) {
        while (!targets.isEmpty()) {
            int index = Mth.clamp(nextTargetIndex, 0, targets.size());
            LivingEntity l = targets.get(index).getOrEmpty(changeling.asWorld()).orElse(null);
            if (l != null && !l.isRemoved() && l.distanceTo(changeling.asEntity()) < 4) {
                nextTargetIndex = (nextTargetIndex + 1) % targets.size();
                return drainFrom(changeling, l, max);
            } else {
                targets.remove(index);
            }
        }
        return 0;
    }

    public float drainFrom(Pony changeling, LivingEntity living, float damage) {
        DamageSource d = changeling.damageOf(UDamageTypes.LOVE_DRAINING, changeling);

        if (damage > 0 && living.level() instanceof ServerLevel sw) {
            living.hurtServer(sw, d, damage);
        }

        ParticleUtils.spawnParticles(UParticles.CHANGELING_MAGIC, living, 7);
        ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, changeling.asEntity(), 0.2F), living, 1);

        if (changeling.asEntity().hasEffect(MobEffects.NAUSEA)) {
            MobEffectInstance effect = changeling.asEntity().getEffect(MobEffects.NAUSEA);
            changeling.asEntity().removeEffect(MobEffects.NAUSEA);
            living.addEffect(effect);
        } else if (changeling.asWorld().getRandom().nextInt(2300) == 0) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 20, 1));
        }

        if (living instanceof Player) {
            damage ++;
            damage *= 1.6F;

            if (!changeling.asEntity().hasEffect(MobEffects.HEALTH_BOOST)) {
                changeling.asEntity().addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 13000, 1));
            }
        }

        return damage;
    }

    public static boolean canFeed(Pony player) {
        return player.asEntity().getHealth() < player.asEntity().getMaxHealth()
            || player.asEntity().canEat(false);
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        compound.putFloat("healthToDrain", healthToDrain);
        compound.putInt("foodToDrain", foodToDrain);
        compound.putFloat("damageThisTick", damageThisTick);
        compound.store("targets", EntityReference.listCodec(), lookup.createSerializationContext(NbtOps.INSTANCE), targets);
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        healthToDrain = compound.getFloatOr("healthToDrain", 0);
        foodToDrain = compound.getIntOr("foodToDrain", 0);
        damageThisTick = compound.getFloatOr("damageThisTick", 0);
        targets = compound.read("targets", EntityReference.<LivingEntity>listCodec(), lookup.createSerializationContext(NbtOps.INSTANCE)).map(ArrayList::new).orElseGet(ArrayList::new);
    }
}
