package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.shape.Sphere;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class AttractiveSpell extends ShieldSpell implements HomingSpell, TimedSpell, ProjectileDelegate.EntityHitListener {
    static final SpellAttribute<Boolean> TARGET_FOCUSED_ENTITY = SpellAttribute.createConditional(SpellAttributeType.FOCUSED_ENTITY, Trait.ORDER, order -> order >= 20);
    static final SpellAttribute<Boolean> STICK_TO_TARGET = SpellAttribute.createConditional(SpellAttributeType.STICK_TO_TARGET, Trait.CHAOS, chaos -> chaos > 0);
    static final TooltipFactory TARGET = (type, tooltip) -> (TARGET_FOCUSED_ENTITY.get(type.traits()) ? TARGET_FOCUSED_ENTITY : ShieldSpell.TARGET).appendTooltip(type, tooltip);
    static final TooltipFactory TOOLTIP = TooltipFactory.of(TIME, RANGE, TARGET, STICK_TO_TARGET, CAST_ON);

    private final EntityReference<Entity> target = dataTracker.startTracking(new EntityReference<>());

    private final Timer timer = new Timer(TIME.get(getTraits()));

    protected AttractiveSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        timer.tick();

        if (timer.getTicksRemaining() <= 0) {
            return false;
        }

        target.getOrEmpty(caster.asWorld())
            .filter(entity -> entity.distanceTo(caster.asEntity()) > getDrawDropOffRange(caster))
            .ifPresent(entity -> {
                Vec3 pos = caster.getOriginVector();
                entity.teleportTo(pos.x, pos.y, pos.z);
            });

        return super.tick(caster, situation);
    }

    @Override
    public void generateParticles(Caster<?> source) {
        double range = getDrawDropOffRange(source);
        Vec3 origin = getOrigin(source);

        source.spawnParticles(origin, new Sphere(false, range), 7, p -> {
            source.addParticle(
                    new FollowingParticleEffect(UParticles.HEALTH_DRAIN, origin, 0.4F)
                        .withChild(net.minecraft.core.particles.SpellParticleOption.create(ParticleTypes.EFFECT, 0xFFFFFF, 1)),
                    p,
                    Vec3.ZERO
            );
        });
    }

    @Override
    protected boolean isValidTarget(Caster<?> source, Entity entity) {
        return target.referenceEquals(entity) || super.isValidTarget(source, entity);
    }

    @Override
    protected void applyRadialEffect(Caster<?> source, Entity target, double distance, double radius) {

        double force = 2.5F * distance;

        boolean isGood = isFriendlyTogether(source);

        if (isGood) {
            force *= AttractionUtils.getForceAdjustment(target);
        }

        if (!source.isClient() && !isGood && source.asWorld().getRandom().nextInt(4500) == 0) {
            source.asEntity().hurtServer(source.asServerWorld(), source.damageOf(UDamageTypes.GAVITY_WELL_RECOIL, source), 4);
        }

        AttractionUtils.applyForce(getOrigin(source), target, -force, 0, false);

        float maxVel = !isFriendlyTogether(source) ? 1 : 1.6f;

        Vec3 vel = target.getDeltaMovement();

        double x = Mth.clamp(vel.x, -maxVel, maxVel);
        double y = Mth.clamp(vel.y, -maxVel, maxVel);
        double z = Mth.clamp(vel.z, -maxVel, maxVel);

        if (distance < 0.5) {
            z += maxVel * 2;
        }
        if (distance < 2) {
            x = 0;
            z = 0;
        }

        if (this.target.referenceEquals(target)) {
            target.fallDistance = 0;

            if (target.onGround()) {
                target.setPos(target.position().add(0, 0.3, 0));
                target.setOnGround(false);
            }
        }
        target.setDeltaMovement(x, y, z);
        Living.updateVelocity(target);
    }

    @Override
    public boolean setTarget(Entity target) {
        if (TARGET_FOCUSED_ENTITY.get(getTraits())) {
            this.target.set(target);
            target.setGlowingTag(true);
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        target.getOrEmpty(caster.asWorld()).ifPresent(target -> target.setGlowingTag(false));
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        if (!isDead() && STICK_TO_TARGET.get(getTraits())) {
            setDead();
            Caster.of(hit.getEntity()).ifPresent(caster -> getTypeAndTraits().apply(caster, CastingMethod.INDIRECT));
        }
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        compound.store("target", EntityReference.CODEC, target);
        timer.toNBT(compound, lookup);
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        target.fromNBT(compound.getCompoundOrEmpty("target"), lookup);
        timer.fromNBT(compound, lookup);
    }
}
