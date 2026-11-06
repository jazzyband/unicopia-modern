package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.projectile.MagicBeamEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class DispellEvilSpell extends AbstractSpell implements ProjectileDelegate.HitListener {
    public static final SpellTraits DEFAULT_TRAITS = SpellTraits.builder()
            .with(Trait.POWER, 1)
            .build();

    private static final SpellAttribute<Double> RANGE = SpellAttribute.create(SpellAttributeType.RANGE, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.POWER, power -> (1 + power) * 10D);

    static final TooltipFactory TOOLTIP = RANGE;

    protected DispellEvilSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (situation == Situation.PROJECTILE) {
            return !isDead();
        }

        source.findAllEntitiesInRange(RANGE.get(getTraits()), e -> e.getType() == EntityTypes.PHANTOM).forEach(entity -> {
            if (!source.isClient()) {
                entity.hurtServer(source.asServerWorld(), entity.damageSources().magic(), 50);
            }
            if (entity instanceof LivingEntity l) {
                double d = source.getOriginVector().x() - entity.getX();
                double e = source.getOriginVector().z() - entity.getZ();
                while (d * d + e * e < 1.0E-4) {
                    d = (Math.random() - Math.random()) * 0.01;
                    e = (Math.random() - Math.random()) * 0.01;
                }
                l.knockback(1, d, e, source.asWorld().damageSources().magic(), 0);
            }

            source.addParticle(LightningBoltParticleEffect.DEFAULT, entity.position(), Vec3.ZERO);
        });
        source.subtractEnergyCost(100);

        return false;
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile) {
        if (projectile instanceof MagicBeamEntity source) {
            tick(source, Situation.GROUND);
        }
    }
}
