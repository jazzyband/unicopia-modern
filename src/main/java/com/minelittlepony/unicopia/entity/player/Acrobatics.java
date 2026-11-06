package com.minelittlepony.unicopia.entity.player;

import java.util.Optional;

import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation.Recipient;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.effect.EffectUtils;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.Tickable;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Acrobatics implements Tickable, NbtSerialisable {
    private int ticksHanging;

    private Direction attachDirection;
    double distanceClimbed;

    private final Pony pony;
    private final Player entity;

    private final DataTracker.Entry<Optional<BlockPos>> hangingPos;

    public Acrobatics(Pony pony, DataTracker tracker) {
        this.pony = pony;
        this.entity = pony.asEntity();
        this.hangingPos = tracker.startTracking(TrackableDataType.OPTIONAL_POS, Optional.empty());
        pony.addTicker(this::checkDislodge);
    }

    public boolean isImmobile() {
        return isFloppy() && entity.onGround();
    }

    public boolean isParalized() {
        return EffectUtils.getAmplifier(entity, UEffects.PARALYSIS) > 1;
    }

    public boolean isFloppy() {
        if (entity.isCreative() && entity.getAbilities().flying) {
            return false;
        }
        return pony.getCompositeRace().any(Race::isFish) && !entity.isInWater() && !entity.level().isWaterAt(PosHelper.findNearestSurface(entity.level(), entity.blockPosition()));
    }

    @Override
    public void tick() {
        if (!pony.getPhysics().isFlying() && !entity.getAbilities().flying
                && entity.getLastClimbablePos().isPresent()
                && pony.getObservedSpecies() == Race.CHANGELING
                && !entity.getInBlockState().is(BlockTags.CLIMBABLE)) {
            Vec3 vel = entity.getDeltaMovement();
            if (entity.isShiftKeyDown()) {
                entity.setDeltaMovement(vel.x, 0, vel.z);
            }

            distanceClimbed += Math.abs(pony.getMotion().getTrackedVelocity().y);

            BlockPos hangingPos = pony.getPhysics().getHeadPosition();
            boolean canhangHere = canHangAt(hangingPos);

            if (distanceClimbed > 1.5) {
                if (vel.length() > 0.08F && entity.tickCount % (3 + entity.getRandom().nextInt(5)) == 0) {
                    entity.playSound(USounds.ENTITY_PLAYER_CHANGELING_CLIMB,
                            (float)entity.getRandom().triangle(0.5, 0.3),
                            entity.getVoicePitch()
                    );
                }

                boolean skipHangCheck = false;
                Direction newAttachDirection = entity.getDirection();
                if (isFaceClimbable(entity.level(), entity.blockPosition(), newAttachDirection) && (newAttachDirection != attachDirection)) {
                    attachDirection = newAttachDirection;
                    skipHangCheck = true;
                }

                if (!skipHangCheck && canhangHere) {
                    if (!isHanging()) {
                        startHanging(hangingPos);
                    } else {
                        if (((LivingEntityDuck)entity).isJumping()) {
                            // Jump to let go
                            return;
                        }
                        entity.setDeltaMovement(entity.getDeltaMovement().multiply(1, 0, 1));
                        entity.setShiftKeyDown(false);
                    }
                } else if (attachDirection != null) {
                    if (isFaceClimbable(entity.level(), entity.blockPosition(), attachDirection)) {
                        entity.setYBodyRot(attachDirection.toYRot());
                        entity.yBodyRotO = attachDirection.toYRot();
                    } else {
                        entity.setDeltaMovement(vel);
                        entity.onClimbable();
                    }
                }
            }

            if (canhangHere) {
                pony.setAnimation(Animation.HANG, Recipient.ANYONE);
            } else if (distanceClimbed > 1.5) {
                pony.setAnimation(Animation.CLIMB, Recipient.ANYONE);
            }
        } else {
            distanceClimbed = 0;
            attachDirection = null;
        }
    }

    private void checkDislodge() {
        if (isHanging()) {
            ((LivingEntityDuck)entity).setLeaningPitch(0);
            if (!pony.isClient() && !canKeepHanging()) {
                stopHanging();
            }
        } else {
            ticksHanging = 0;
        }


        if (pony.getCompositeRace().includes(Race.SEAPONY) && !entity.isUnderWater() && pony.landedChanged()) {
            entity.level().playSound(null, entity.blockPosition(), SoundEvents.GUARDIAN_FLOP, SoundSource.PLAYERS, 1, 1);
        }
    }

    boolean isFaceClimbable(Level world, BlockPos pos, Direction direction) {
        pos = pos.relative(direction);
        BlockState state = world.getBlockState(pos);
        return !state.is(Blocks.SCAFFOLDING) && state.isFaceSturdy(world, pos, direction, SupportType.CENTER);
    }

    public Optional<BlockPos> getHangingPosition() {
        return hangingPos.get();
    }

    public boolean isHanging() {
        return getHangingPosition().isPresent();
    }

    public void stopHanging() {
        hangingPos.set(Optional.empty());
        entity.refreshDimensions();
        ticksHanging = 0;
    }

    public void startHanging(BlockPos pos) {
        hangingPos.set(Optional.of(pos));
        entity.teleportTo(pos.getX() + 0.5, pos.getY() + pony.getPhysics().getGravitySignum(), pos.getZ() + 0.5);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setShiftKeyDown(false);
        entity.stopFallFlying();
        pony.getPhysics().cancelFlight(true);
    }

    public boolean canHangAt(BlockPos pos) {
        int gravity = pony.getPhysics().getGravitySignum() * (isHanging() && pony.getCompositeRace().includes(Race.BAT) ? -1 : 1);
        BlockState state = pony.asWorld().getBlockState(pos);

        if (!pony.asWorld().noBlockCollision(pony.asEntity(), pony.getPhysics().getBoxAtPosition(Vec3.atBottomCenterOf(pos), gravity > 0))) {
            return false;
        }

        pos = pos.above(gravity);
        state = pony.asWorld().getBlockState(pos);
        return state.entityCanStandOnFace(pony.asWorld(), pos, entity, gravity > 0 ? Direction.UP : Direction.DOWN);
    }

    private boolean canKeepHanging() {
        return pony.getCompositeRace().any(Race::canHang) && (ticksHanging++ <= 20 || getHangingPosition().filter(hangingPos -> {

            int y = (int)(pony.asEntity().getBoundingBox().maxY - 0.5);
            var pos = pony.asEntity().blockPosition().atY(y);
            Debug.drawBoxSelection(pony, ParticleTypes.PORTAL, hangingPos);
            return pony.getCompositeRace().includes(Race.CHANGELING) || hangingPos.closerThan(pos, 1.5) && (!pony.isPosLoaded(hangingPos) || canHangAt(hangingPos));
        }).isPresent());
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        compound.putInt("ticksHanging", ticksHanging);
        getHangingPosition().ifPresent(pos -> compound.store("hangingPosition", BlockPos.CODEC,
                lookup.createSerializationContext(NbtOps.INSTANCE), pos));
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        ticksHanging = compound.getIntOr("ticksHanging", 0);
        hangingPos.set(compound.read("hangingPosition", BlockPos.CODEC, lookup.createSerializationContext(NbtOps.INSTANCE)));
    }
}
