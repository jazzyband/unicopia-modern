package com.minelittlepony.unicopia.entity.effect;

import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.LevelEvent;

public class CorruptInfluenceStatusEffect extends SimpleStatusEffect {
    static final Identifier CORRUPTION_MODIFIER_ID = Unicopia.id("corruption_modifier");

    CorruptInfluenceStatusEffect(int color) {
        super(MobEffectCategory.NEUTRAL, color, false);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, CORRUPTION_MODIFIER_ID, 15, AttributeModifier.Operation.ADD_VALUE);
        addAttributeModifier(Attributes.ATTACK_SPEED, CORRUPTION_MODIFIER_ID, 10, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {

        if (entity.level().isClientSide()) {
            return true;
        }

        if (entity instanceof Monster mob) {

            int nearby = entity.level().getEntities(entity, entity.getBoundingBox().inflate(40), i -> i.getType() == entity.getType()).size();

            if (nearby > 1) {
                if (Equine.of(entity).filter(eq -> eq instanceof Owned<?> o && o.getMaster() != null).isPresent()) {
                    return false;
                }

                if (entity.level().getRandom().nextInt(2000) != 0) {
                    return true;
                }
            } else if (entity.level().getRandom().nextInt(200) != 0) {
                return true;
            }

            reproduce(mob);


        } else if (entity.tickCount % 2000 == 0) {
            entity.hurtServer(world, Living.living(entity).damageOf(UDamageTypes.ALICORN_AMULET), 2);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0;
    }

    public static void reproduce(Monster mob) {
        Monster clone = (Monster)mob.getType().create(mob.level(), EntitySpawnReason.BREEDING);
        clone.copyPosition(mob);
        clone.knockback(0.1, 0.5, 0.5, mob.damageSources().magic(), 1);
        mob.knockback(0.1, -0.5, -0.5, clone.damageSources().magic(), 1);
        if (mob.getRandom().nextInt(4) != 0) {
            mob.removeAllEffects();
        } else {
            if (clone.getAttributes().hasAttribute(Attributes.MAX_HEALTH)) {
                float maxHealthDifference = mob.getMaxHealth() - clone.getMaxHealth();
                clone.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 900000, 2));
                clone.getAttribute(Attributes.MAX_HEALTH)
                    .addPermanentModifier(new AttributeModifier(CORRUPTION_MODIFIER_ID, maxHealthDifference + 1, AttributeModifier.Operation.ADD_VALUE));
            }
            if (clone.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
                clone.getAttribute(Attributes.ATTACK_DAMAGE)
                    .addPermanentModifier(new AttributeModifier(CORRUPTION_MODIFIER_ID, mob.getAttributeValue(Attributes.ATTACK_DAMAGE) + 1, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        if (mob.getRandom().nextInt(4) != 0) {
            clone.removeAllEffects();
        }
        mob.level().addFreshEntity(clone);

        if (!mob.isSilent()) {
            mob.level().levelEvent((Player)null, LevelEvent.SOUND_ZOMBIE_INFECTED, mob.blockPosition(), 0);
        }
    }
}
