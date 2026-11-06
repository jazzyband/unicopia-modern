package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.MagicBeamEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.server.world.UGameRules;
import com.minelittlepony.unicopia.util.Lerp;
import com.minelittlepony.unicopia.util.shape.Sphere;

/**
 * More powerful version of the vortex spell which creates a black hole.
 */
public class DarkVortexSpell extends AbstractSpell implements ProjectileDelegate.BlockHitListener {
    public static final SpellTraits DEFAULT_TRAITS = SpellTraits.builder()
            .with(Trait.CHAOS, 5)
            .with(Trait.KNOWLEDGE, 1)
            .with(Trait.STRENGTH, 70)
            .with(Trait.DARKNESS, 100)
            .build();

    private final DataTracker.Entry<Float> accumulatedMass = this.dataTracker.startTracking(TrackableDataType.FLOAT, 0F);

    private final TargetSelecter targetSelecter = new TargetSelecter(this).setFilter(this::isValidTarget).setTargetowner(true).setTargetAllies(true);

    private final Lerp radius = new Lerp(0);

    protected DarkVortexSpell(CustomisedSpellType<?> type) {
        super(type);
    }
    // 1. force decreases with distance: distance scale 1 -> 0
    // 2. max force (at dist 0) is taken from accumulated mass
    // 3. force reaches 0 at distance of drawDropOffRange

    private double getMass() {
        return 0.1F + accumulatedMass.get() / 10F;
    }

    public double getEventHorizonRadius() {
        return radius.getValue();
    }

    public double getDrawDropOffRange() {
        return getEventHorizonRadius() * 20;
    }

    private double getAttractiveForce(Caster<?> source, Entity target) {
        return AttractionUtils.getAttractiveForce(getMass(), getOrigin(source), target);
    }

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return method == CastingMethod.STAFF ? toThrowable() : toPlaceable();
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation == Situation.PROJECTILE) {
            return false;
        }

        if (situation == Situation.BODY) {
            return true;
        }

        Vec3 origin = getOrigin(source);
        double mass = getMass() * 0.1;
        double logarithm = 1 - (1D / (1 + (mass * mass)));
        radius.update((float)Math.max(0.01, logarithm * source.asWorld().getServer().getGameRules().get(UGameRules.MAX_DARK_VORTEX_SIZE)), 200L);

        if (source.asEntity().tickCount % 20 == 0) {
            source.asWorld().playSound(null, source.getOrigin(), USounds.AMBIENT_DARK_VORTEX_ADDITIONS, SoundSource.AMBIENT, 1, 1);
        }

        double eventHorizon = getEventHorizonRadius();

        if (source.isClient()) {
            if (eventHorizon > 0.3) {
                double range = eventHorizon * 2;
                source.spawnParticles(origin, new Sphere(false, range), 50, p -> {
                    source.addParticle(
                            new FollowingParticleEffect(UParticles.HEALTH_DRAIN, origin, 0.4F)
                            .withChild(source.asWorld().isEmptyBlock(BlockPos.containing(p)) ? ParticleTypes.SMOKE : ParticleTypes.CAMPFIRE_SIGNAL_SMOKE),
                            p,
                            Vec3.ZERO
                    );
                });
            }

            if (source.asWorld().getRandom().nextInt(300) == 0) {
                ParticleUtils.spawnParticle(source.asWorld(), LightningBoltParticleEffect.DEFAULT, origin, Vec3.ZERO);
            }
        } else {
            if (eventHorizon > 2) {
                new Sphere(false, eventHorizon + 3).translate(origin).randomPoints(10, source.asWorld().getRandom()).forEach(i -> {
                    BlockPos pos = BlockPos.containing(i);
                    if (!source.asWorld().isEmptyBlock(pos)) {
                        new Sphere(false, 3).translate(i).getBlockPositions().forEach(p -> {
                            affectBlock(source, p, origin);
                        });
                        ParticleUtils.spawnParticle(source.asWorld(), new LightningBoltParticleEffect(true, 10, 6, 3, Optional.of(i)), getOrigin(source), Vec3.ZERO);
                    }
                });
            }
        }


        for (Entity insideEntity : source.findAllEntitiesInRange(eventHorizon * 0.5F).toList()) {
            insideEntity.setDeltaMovement(Vec3.ZERO);
            Living.updateVelocity(insideEntity);

            if (insideEntity instanceof CastSpellEntity s && getType().isOn(insideEntity)) {
                setDead();
                s.getSpellSlot().clear();
                source.asWorld().explode(source.asEntity(), origin.x, origin.y, origin.z, 12, ExplosionInteraction.NONE);
                source.asWorld().explode(source.asEntity(), insideEntity.getX(), insideEntity.getY(), insideEntity.getZ(), 12, ExplosionInteraction.NONE);
                return false;
            }
        }
        targetSelecter.getEntities(source, getDrawDropOffRange()).forEach(i -> {
            try {
                affectEntity(source, i, origin);
            } catch (Throwable e) {
                Unicopia.LOGGER.error("Error updating radial effect", e);
            }
        });

        if (!source.subtractEnergyCost(0.01)) {
            setDead();
            source.asWorld().explode(source.asEntity(), origin.x, origin.y, origin.z, 3, ExplosionInteraction.NONE);
        }

        return true;
    }


    @Override
    public void tickDying(Caster<?> source) {
        float m = accumulatedMass.get() - 0.8F;
        accumulatedMass.set(m);
        double mass = getMass() * 0.1;
        double logarithm = 1 - (1D / (1 + (mass * mass)));
        radius.update((float)Math.max(0.1, logarithm * source.asWorld().getServer().getGameRules().get(UGameRules.MAX_DARK_VORTEX_SIZE)), 200L);
        if (m < 1) {
            super.tickDying(source);
        }

        Vec3 origin = getOrigin(source);
        ParticleUtils.spawnParticle(source.asWorld(), ParticleTypes.SMOKE, origin, new Vec3(0, 0.2F, 0));
        ParticleUtils.spawnParticle(source.asWorld(), ParticleTypes.SMOKE, origin, new Vec3(0, -0.2F, 0));

        if (!source.isClient() && source.asWorld().getRandom().nextInt(10) == 0) {
            Block.popResource(source.asWorld(), BlockPos.containing(origin), (source.asWorld().getRandom().nextInt(75) == 0 ? Items.ANCIENT_DEBRIS : Items.IRON_NUGGET).getDefaultInstance());
        }
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockHitResult hit) {
        if (!projectile.isClient() && projectile instanceof MagicBeamEntity source) {
            Vec3 pos = hit.getLocation();
            projectile.level().explode(projectile, pos.x, pos.y, pos.z, 12, ExplosionInteraction.NONE);
            toPlaceable().tick(source, Situation.BODY);
        }
    }

    @Override
    public boolean isFriendlyTogether(Affine other) {
        return accumulatedMass.get() < 4;
    }

    private boolean isValidTarget(Caster<?> source, Entity entity) {
        return EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && getAttractiveForce(source, entity) > 0;
    }

    public Vec3 getOrigin(Caster<?> source) {
        return source.asEntity().position().add(0, getYOffset(), 0);
    }

    public double getYOffset() {
        return 2;
    }

    private boolean canAffect(Caster<?> source, BlockPos pos) {
        return source.canModifyAt(pos)
            && source.asWorld().getBlockState(pos).getDestroySpeed(source.asWorld(), pos) >= 0
            && !source.asWorld().getBlockState(pos).is(UTags.Blocks.CATAPULT_IMMUNE);
    }

    private void affectBlock(Caster<?> source, BlockPos pos, Vec3 origin) {
        if (!canAffect(source, pos)) {
            if (source.asWorld().getBlockState(pos).is(Blocks.BEDROCK)) {
                source.asWorld().setBlockAndUpdate(pos, Blocks.BARRIER.defaultBlockState());
            }
            return;
        }
        if (pos.closerToCenterThan(origin, getEventHorizonRadius())) {
            source.asWorld().destroyBlock(pos, false);
            updateStatePostRemoval(source, pos);
        } else {
            CatapultSpell.createBlockEntity(source.asWorld(), pos, e -> {
                updateStatePostRemoval(source, pos);
                e.push(0, 0.1, 0);
            });
        }
    }

    private void updateStatePostRemoval(Caster<?> source, BlockPos pos) {
        if (!source.asWorld().getFluidState(pos).isEmpty()) {
            source.asWorld().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    private void affectEntity(Caster<?> source, Entity target, Vec3 origin) {
        double distance = target.position().distanceTo(origin);
        double eventHorizonRadius = getEventHorizonRadius();

        if (distance <= eventHorizonRadius + 0.5) {
            target.setDeltaMovement(target.getDeltaMovement().scale(distance < 1 ? distance : distance / (2 * eventHorizonRadius)));
            Living.updateVelocity(target);

            @Nullable
            Entity master = source.getMaster();

            if (target instanceof MagicProjectileEntity projectile) {
                Item item = projectile.getItem().getItem();
                if (item instanceof ProjectileDelegate.EntityHitListener p && master != null) {
                    p.onImpact(projectile, new EntityHitResult(master));
                }
            } else if (target instanceof AbstractArrow) {
                if (master != null && !source.isClient()) {
                    master.hurtServer(source.asServerWorld(), master.damageSources().thrown(target, ((AbstractArrow)target).getOwner()), 4);
                }
                target.discard();
                return;
            }

            double massOfTarget = AttractionUtils.getMass(target);

            if (!source.isClient()) {
                if (massOfTarget != 0) {
                    accumulatedMass.set((float)(accumulatedMass.get() + massOfTarget));
                }

                target.hurtServer(source.asServerWorld(), source.damageOf(UDamageTypes.GAVITY_WELL_RECOIL, source), Integer.MAX_VALUE);
            }
            if (!(target instanceof Player)) {
                target.discard();
                source.asWorld().playSound(null, target.blockPosition(), USounds.AMBIENT_DARK_VORTEX_MOOD, SoundSource.AMBIENT, 2, 0.002F);
            }
            if (!source.isClient() && target.isAlive()) {
                target.hurtServer(source.asServerWorld(), source.asEntity().damageSources().fellOutOfWorld(), Integer.MAX_VALUE);
            }

            source.subtractEnergyCost(-massOfTarget * 10);

            if (target instanceof Player && distance < eventHorizonRadius + 5) {
                source.asWorld().playSound(null, target.blockPosition(), USounds.AMBIENT_DARK_VORTEX_MOOD, SoundSource.AMBIENT, 2, 0.02F);
            }

        } else {
            double force = getAttractiveForce(source, target);

            AttractionUtils.applyForce(origin, target, -force, 0, true);
        }
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        compound.putFloat("accumulatedMass", accumulatedMass.get());
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        accumulatedMass.set(compound.getFloatOr("accumulatedMass", 0));
    }
}
