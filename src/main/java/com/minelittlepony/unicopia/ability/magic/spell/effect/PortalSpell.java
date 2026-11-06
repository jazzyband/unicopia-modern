package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgCasterLookRequest;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.particle.*;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.minelittlepony.unicopia.util.shape.*;

public class PortalSpell extends AbstractSpell implements PlacementControlSpell.PlacementDelegate, OrientedSpell {
    public static final SpellTraits DEFAULT_TRAITS = SpellTraits.builder()
            .with(Trait.LIFE, 10)
            .with(Trait.KNOWLEDGE, 1)
            .with(Trait.ORDER, 25)
            .build();
    private static final Shape PARTICLE_AREA = new Sphere(true, 2, 1, 1, 0);

    private final DataTracker.Entry<Optional<UUID>> targetPortalId = dataTracker.startTracking(TrackableDataType.UUID, Optional.empty());
    private final DataTracker.Entry<Float> targetPortalPitch = dataTracker.startTracking(TrackableDataType.FLOAT, 0F);
    private final DataTracker.Entry<Float> targetPortalYaw = dataTracker.startTracking(TrackableDataType.FLOAT, 0F);
    private final EntityReference<Entity> teleportationTarget = dataTracker.startTracking(new EntityReference<>());

    private final DataTracker.Entry<Float> pitch = dataTracker.startTracking(TrackableDataType.FLOAT, 0F);
    private final DataTracker.Entry<Float> yaw = dataTracker.startTracking(TrackableDataType.FLOAT, 0F);

    private Shape particleArea = PARTICLE_AREA;

    protected PortalSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    public EntityReference<Entity> getDestinationReference() {
        return teleportationTarget;
    }

    public float getPitch() {
        return pitch.get();
    }

    public float getYaw() {
        return yaw.get();
    }

    public float getTargetPitch() {
        return targetPortalPitch.get();
    }

    public float getTargetYaw() {
        return targetPortalYaw.get();
    }

    public float getYawDifference() {
        return Mth.wrapDegrees(180 + getTargetYaw() - getYaw());
    }

    @SuppressWarnings("unchecked")
    private Ether.MutableEntry<PortalSpell> getDestination(Caster<?> source) {
        return targetPortalId.get().flatMap(id -> getDestinationReference()
                .getTarget()
                .map(target -> Ether.get(source.asWorld()).get((SpellType<PortalSpell>)getType(), target.uuid(), id))
                .filter(destination -> destination.isClaimedBy(getUuid()))
        ).orElse(null);
    }

    @Override
    public boolean apply(Caster<?> caster) {
        return toPlaceable().apply(caster);
    }

    protected void setDestination(@Nullable Ether.MutableEntry<?> destination) {
        if (destination == null) {
            teleportationTarget.set(null);
            targetPortalId.set(Optional.empty());
        } else {
            teleportationTarget.copyFrom(destination.entity);
            targetPortalId.set(Optional.of(destination.spellId()));
            targetPortalPitch.set(destination.pitch());
            targetPortalYaw.set(destination.yaw());
        }
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (situation == Situation.GROUND) {
            if (source.isClient()) {
                source.spawnParticles(particleArea, 5, pos -> {
                    source.addParticle(ParticleTypes.ELECTRIC_SPARK, pos, Vec3.ZERO);
                });
            } else {
                var ownEntry = Ether.get(source.asWorld()).get(this, source);
                synchronized (ownEntry) {
                    var targetEntry = getDestination(source);

                    if (targetEntry == null) {
                        if (teleportationTarget.isSet()) {
                            setDestination(null);
                            source.asWorld().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, source.getOrigin(), Block.getId(Blocks.GLASS.defaultBlockState()));
                        } else {
                            Ether.get(source.asWorld()).anyMatch(getType(), entry -> {
                                if (entry.isAlive() && !entry.hasClaimant() && !entry.entityMatches(source.asEntity().getUUID())) {
                                    entry.claim(getUuid());
                                    ownEntry.claim(entry.spellId());
                                    synchronized (entry) {
                                        if (entry.getSpell() instanceof PortalSpell portal) {
                                            portal.setDestination(ownEntry);
                                        }
                                    }
                                    setDestination(entry);
                                }
                                return false;
                            });
                        }
                    } else {
                        targetPortalPitch.set(targetEntry.pitch());
                        targetPortalYaw.set(targetEntry.yaw());

                        tickActive(source, targetEntry);
                    }
                }

                ownEntry.setPitch(getPitch());
                ownEntry.setYaw(getYaw());
            }
        }

        return !isDead();
    }

    private void tickActive(Caster<?> source, Ether.MutableEntry<?> destination) {
        destination.entity.getTarget().ifPresent(target -> {
            Quaternionf rotationChange = getOrientationChange();
            var matrix = getPositionMatrix(source, source.asEntity().position(), rotationChange, new Matrix4f());

            float yawDifference = getYawDifference();

            source.findAllEntitiesInRange(1).forEach(entity -> {
                if (!entity.isOnPortalCooldown()) {

                    float approachYaw = Math.abs(Mth.wrapDegrees(entity.getYRot() - this.yaw.get()));
                    if (approachYaw > 80) {
                        return;
                    }

                    var dest4f = matrix.transform(new Vector4f(entity.position().toVector3f(), 1));
                    Vec3 dest = new Vec3(dest4f.x, dest4f.y - 0.5, dest4f.z).add(new Vec3(0, 0, -0.7F).yRot(-getTargetYaw() * Mth.DEG_TO_RAD));

                    for (int i = 0; i < 2; i++) {
                        BlockPos destBlock = BlockPos.containing(dest);
                        BlockState state = entity.level().getBlockState(destBlock);
                        if (entity.level().loadedAndEntityCanStandOn(destBlock, entity)) {
                            double maxY = state.getCollisionShape(entity.level(), destBlock).max(Axis.Y);
                            dest = new Vec3(dest.x, destBlock.getY() + maxY, dest.z);
                        }
                    }

                    entity.setPortalCooldown();

                    float yaw = Mth.wrapDegrees(entity.getYRot() + yawDifference);

                    entity.setDeltaMovement(entity.getDeltaMovement().yRot(yawDifference * Mth.DEG_TO_RAD));

                    entity.level().playSound(null, entity, USounds.ENTITY_PLAYER_UNICORN_TELEPORT, entity.getSoundSource(), 1, 1);
                    entity.teleportTo((ServerLevel)entity.level(), dest.x, dest.y, dest.z, Relative.DELTA, yaw, entity.getXRot(), false);
                    entity.level().playSound(null, entity, USounds.ENTITY_PLAYER_UNICORN_TELEPORT, entity.getSoundSource(), 1, 1);

                    Living.updateVelocity(entity);

                    if (!source.subtractEnergyCost(Math.sqrt(entity.position().subtract(dest).length()))) {
                        setDead();
                    }
                }

                ParticleUtils.spawnParticles(new MagicParticleEffect(getType().getColor()), entity, 7);
            });
        });
    }

    public Matrix4f getPositionMatrix(Caster<?> source, Vec3 pos, Quaternionf orientationChange, Matrix4f matrix) {
        getDestinationReference().getTarget().ifPresent(destEntity -> {
            Vector3f destPos = destEntity.pos().toVector3f();
            Vector3f sourcePos = pos.toVector3f();

            matrix.rotateAround(orientationChange.conjugate(), destPos.x, destPos.y, destPos.z);
            matrix.translate(destPos.sub(sourcePos));
        });
        return matrix;
    }

    public Quaternionf getOrientationChange() {
        return new Quaternionf().rotateTo(
                Vec3.directionFromRotation(getPitch(), getYaw()).toVector3f(),
                Vec3.directionFromRotation(getTargetPitch(), getTargetYaw()).toVector3f()
        );
    }

    @Override
    public void setOrientation(Caster<?> caster, float pitch, float yaw) {
        this.pitch.set(90 - pitch);
        this.yaw.set(-yaw);
        particleArea = PARTICLE_AREA.rotate(
            this.pitch.get() * Mth.DEG_TO_RAD,
            (180 - this.yaw.get()) * Mth.DEG_TO_RAD
        );
    }

    @Override
    public void onPlaced(Caster<?> source, PlacementControlSpell parent) {
        Entity caster = source.asEntity();
        Vec3 targetPos = caster.getLookAngle().scale(3).add(caster.getEyePosition());
        parent.setOrientation(source, -90 - source.asEntity().getXRot(), -source.asEntity().getYRot());
        parent.setPosition(new Vec3(targetPos.x, caster.position().y, targetPos.z));
        if (source instanceof Pony pony) {
            Channel.SERVER_REQUEST_PLAYER_LOOK.sendToPlayer(new MsgCasterLookRequest(parent.getUuid()), (ServerPlayer)pony.asEntity());
        }
    }

    @Override
    protected void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        if (!caster.isClient()) {
            var destination = getDestination(caster);
            if (destination != null) {
                destination.release(getUuid());
            }
        }
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        compound.store("targetPortalId", CodecUtils.OPTIONAL_UUID, targetPortalId.get());
        compound.put("teleportationTarget", teleportationTarget.toNBT(lookup));
        compound.putFloat("pitch", getPitch());
        compound.putFloat("yaw", getYaw());
        compound.putFloat("targetPortalPitch", getTargetPitch());
        compound.putFloat("targetPortalYaw", getTargetYaw());
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        targetPortalId.set(compound.read("targetPortalId", CodecUtils.OPTIONAL_UUID).orElse(Optional.empty()));
        teleportationTarget.fromNBT(compound.getCompoundOrEmpty("teleportationTarget"), lookup);
        pitch.set(compound.getFloatOr("pitch", 0));
        yaw.set(compound.getFloatOr("yaw", 0));
        targetPortalPitch.set(compound.getFloatOr("targetPortalPitch", 0));
        targetPortalYaw.set(compound.getFloatOr("targetPortalYaw", 0));
        particleArea = PARTICLE_AREA.rotate(
            pitch.get() * Mth.DEG_TO_RAD,
            (180 - yaw.get()) * Mth.DEG_TO_RAD
        );
    }
}
