package com.minelittlepony.unicopia.entity.player;

import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.compat.ad_astra.OxygenApi;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.effect.EffectUtils;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;
import com.minelittlepony.unicopia.input.Heuristic;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.component.Charges;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgPlayerFlightControlsInput;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.particle.*;
import com.minelittlepony.unicopia.projectile.ProjectileUtil;
import com.minelittlepony.unicopia.server.world.BlockDestructionManager;
import com.minelittlepony.unicopia.server.world.ModificationType;
import com.minelittlepony.unicopia.server.world.UGameRules;
import com.minelittlepony.unicopia.server.world.WeatherConditions;
import com.minelittlepony.unicopia.util.*;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.DimensionTypes;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import net.minecraft.world.level.block.Blocks;

public class PlayerPhysics extends EntityPhysics<Player> implements Tickable, Motion, NbtSerialisable {
    private static final int MAX_WALL_HIT_CALLDOWN = 30;
    private static final int MAX_TICKS_TO_GLIDE = 20;
    private static final int MAX_TICKS_TO_WEATHER_EFFECTS = 100;
    private static final int IDLE_FLAP_INTERVAL = 20;
    private static final int GLIDING_SOUND_INTERVAL = 200;
    private static final int LANDING_COLLISION_TIME = 3;

    private int ticksInAir;
    private int ticksToGlide;
    private int ticksDiving;
    private int ticksFlyingLow;
    private int ticksColliding;

    private float thrustScale = 0;
    private float prevThrustScale;

    private boolean flapping;
    private boolean isCancelled;

    private int prevStrafe;
    private float strafe;

    private float descentRate;

    private FlightType lastFlightType = FlightType.NONE;

    public boolean isFlyingEither = false;
    public boolean isFlyingSurvival = false;

    private boolean soundPlaying;

    private int wallHitCooldown;

    @Nullable
    private DimensionType lastDimension;
    private Optional<Vec3> lastPos = Optional.empty();
    private final RollingDelta lastVel = new RollingDelta(30);
    private Vec3 lastAccelleration = Vec3.ZERO;

    private final PlayerDimensions dimensions;

    private final Pony pony;

    private Lerp updraft = new Lerp(0);
    private Lerp windStrength = new Lerp(0);

    public PlayerPhysics(Pony pony, DataTracker tracker) {
        super(pony.asEntity());
        this.pony = pony;
        dimensions = new PlayerDimensions(pony, this);
    }

    @Override
    public PlayerDimensions getDimensions() {
        return dimensions;
    }

    @Override
    public Vec3 getTrackedVelocity() {
        return lastVel.read();
    }

    @Override
    public final float getPersistantGravityModifier() {
        return super.getGravityModifier();
    }

    public float getFlapCooldown(float tickDelta) {
        float lerpedThrust = Mth.lerp(tickDelta, prevThrustScale, thrustScale);
        return lerpedThrust <= 0.000001F ? 0 : lerpedThrust;
    }

    @Override
    public float getGravityModifier() {
        float modifier = getPersistantGravityModifier();
        if (pony.getAcrobatics().isHanging() && pony.getObservedSpecies() == Race.BAT) {
            modifier *= -0.05F;
        }
        return modifier;
    }

    @Override
    public boolean isFlying() {
        return isFlyingSurvival
                && !entity.isFallFlying()
                && !entity.isPassenger()
                && !entity.getAbilities().instabuild
                && !entity.isSpectator();
    }

    @Override
    public boolean isGliding() {
        return ticksToGlide <= 0 && isFlying() && !pony.getJumpingHeuristic().getState();
    }

    @Override
    public boolean isDiving() {
        return ticksDiving > 0;
    }

    @Override
    public boolean isRainbooming() {
        return SpellType.RAINBOOM.isOn(pony);
    }

    @Override
    public float getWingAngle() {

        if (pony.getAnimation().isOf(Animation.SPREAD_WINGS)) {
            return AnimationUtil.seeSitSaw(pony.getAnimationProgress(1), 1.5F);
        }

        float spreadAmount = -0.5F;

        if (isFlying()) {
            if (lastFlightType == FlightType.INSECTOID) {
                spreadAmount += Math.sin(pony.asEntity().tickCount * 4F) * 8;
            } else {
                if (isGliding()) {
                    spreadAmount += MineLPDelegate.getInstance().getPlayerPonyRace(entity).isEquine() ? -0.8F : 2.5F;
                } else {
                    if (MineLPDelegate.getInstance().getPlayerPonyRace(entity).isEquine()) {
                        spreadAmount -= 1.8F;
                    }
                    spreadAmount += strafe * 10;
                    spreadAmount += thrustScale * 24;
                }
            }
        } else {
            spreadAmount += Mth.clamp(-getTrackedVelocity().y, -0.3F, 2);
            spreadAmount += Math.sin(entity.tickCount / 9F) / 9F;

            if (entity.isShiftKeyDown()) {
                spreadAmount += 2;
            }
        }

        spreadAmount = Mth.clamp(spreadAmount, -2, 6);

        return pony.getInterpolator().interpolate("wingSpreadAmount", spreadAmount, 10);
    }

    public FlightType getFlightType() {
        return lastFlightType;
    }

    private FlightType recalculateFlightType() {
        DimensionType dimension = entity.level().dimensionType();

        if ((RegistryUtils.isIn(entity.level(), dimension, Registries.DIMENSION_TYPE, UTags.DimensionTypes.HAS_NO_ATMOSPHERE)
                || InteractionManager.getInstance().getSyncedConfig().dimensionsWithoutAtmosphere().contains(RegistryUtils.getId(entity.level(), dimension, Registries.DIMENSION_TYPE).toString()))
                && !OxygenApi.API.get().hasOxygen(entity.level(), entity.blockPosition())) {
            return FlightType.NONE;
        }

        if (AmuletItem.isApplicable(entity, UItems.PEGASUS_AMULET)) {
            return FlightType.ARTIFICIAL;
        }

        return pony.getSpellSlot().get()
            .filter(effect -> !effect.isDead() && effect instanceof FlightType.Provider)
            .map(effect -> ((FlightType.Provider)effect).getFlightType())
            .filter(FlightType::isPresent)
            .orElse(pony.getCompositeRace().flightType());
    }

    public void cancelFlight(boolean force) {
        if (force) {
            isCancelled = true;
        }
        boolean wasFlying = isFlyingEither;
        entity.getAbilities().flying = false;
        isFlyingEither = false;
        isFlyingSurvival = false;
        strafe = 0;
        thrustScale = 0;

        if (wasFlying) {
            entity.refreshDimensions();
        }

        pony.sendUpdatePacket();
    }

    public double getHorizontalMotion() {
        return lastVel.horLengthSquared();
    }

    @Override
    public void tick() {
        super.tick();

        if (pony.isClientPlayer() && (pony.getJumpingHeuristic().hasChanged(Heuristic.ONCE) || pony.sneakingChanged())) {
            Channel.FLIGHT_CONTROLS_INPUT.sendToServer(new MsgPlayerFlightControlsInput(pony));
        }

        prevThrustScale = thrustScale;

        if (wallHitCooldown > 0) {
            wallHitCooldown--;
        }
        if (ticksToGlide > 0) {
            ticksToGlide--;
        }

        DimensionType dimension = entity.level().dimensionType();
        if (dimension != lastDimension) {
            lastDimension = dimension;
            lastPos = Optional.empty();
        }

        Vec3 prevVel = getTrackedVelocity();
        lastVel.update(lastPos.map(entity.position()::subtract).orElse(entity.getDeltaMovement()));
        lastAccelleration = prevVel.subtract(getTrackedVelocity());
        lastPos = Optional.of(entity.position());

        if (!pony.isClient() && lastAccelleration.lengthSqr() > 1) {
            Unicopia.LOGGER.info("Accelleration for {} Changed rapidly! Got {} > 1", pony.asEntity().getName().getString(), lastAccelleration.lengthSqr());
        }

        final MutableVector velocity = new MutableVector(pony.isClient() || Unicopia.getConfig().disableExperimentalServerVelocityFix.get() ? entity.getDeltaMovement() : getTrackedVelocity());

        FlightType type = recalculateFlightType();

        boolean typeChanged = type != lastFlightType;

        if (typeChanged && (lastFlightType.isArtifical() || type.isArtifical())) {
            pony.spawnParticles(ParticleTypes.CLOUD, 10);

            playSound(entity.level().dimension().equals(net.minecraft.world.level.Level.NETHER) ? USounds.ITEM_ICARUS_WINGS_CORRUPT : USounds.ITEM_ICARUS_WINGS_PURIFY, 0.1125F, 1.5F);
        }

        lastFlightType = type;

        boolean creative = FlightType.canFlyCreative(entity);
        boolean startedFlyingCreative = !creative && isFlyingEither != entity.getAbilities().flying;

        if (!creative) {
            if (entity.onGround() || isCancelled) {
                cancelFlight(false);
            }

            if (!pony.isClient()) {
                if (type.canFly(entity)
                        && isFlying()
                        && EffectUtils.hasBothBrokenWing(entity)
                        && ticksInAir > 90) {

                    entity.level().playSound(null, entity, USounds.Vanilla.PLAYER_BIG_FALL, SoundSource.PLAYERS, 2, 1F);
                    entity.hurtServer((ServerLevel)entity.level(), entity.damageSources().generic(), 3);
                    cancelFlight(true);
                }
            }

            if (entity.onGround()) {
                isCancelled = false;
            }

            setAbleToFly(entity.getAbilities().flying | lastFlightType.canFly(entity) && isFlyingEither);
            if (!lastFlightType.canFly(entity) && typeChanged) {
                setAbleToFly(false);
            }

            if (entity.horizontalCollision || entity.verticalCollision || entity.onGround()) {
                ticksColliding++;
            } else {
                ticksColliding = 0;
            }

            if ((entity.onGround() && entity.isShiftKeyDown() && ticksColliding > LANDING_COLLISION_TIME)
                    || (entity.isInWater() && ticksInAir > 20)
                    || (entity.horizontalCollision)
                    || (entity.verticalCollision && ticksColliding > LANDING_COLLISION_TIME && (pony.getObservedSpecies() != Race.BAT || velocity.y < 0))) {

                if (entity.getAbilities().flying && entity.horizontalCollision) {
                    handleWallCollission(velocity);
                    return;
                }

                if (ticksColliding > LANDING_COLLISION_TIME || entity.isInWater()) {
                    cancelFlight(false);
                }
            }
        }

        if (isGravityNegative()) {
            if (entity.onGround() || (!creative && entity.horizontalCollision && ticksColliding > LANDING_COLLISION_TIME)) {
                cancelFlight(false);
            }
        }

        isFlyingSurvival = entity.getAbilities().flying && !creative;
        isFlyingEither = isFlyingSurvival || (creative && entity.getAbilities().flying);

        if (typeChanged || startedFlyingCreative) {
            entity.refreshDimensions();
        }

        if (lastFlightType.canFly(entity)) {
            if (isFlying()) {
                ticksInAir++;
                tickFlight(lastFlightType, velocity);

                int strafing = (int)Math.signum(entity.xxa);
                if (strafing != prevStrafe) {
                    prevStrafe = strafing;
                    strafe = 1;
                    ticksToGlide = MAX_TICKS_TO_GLIDE;
                    if (!SpellPredicate.IS_DISGUISE.isOn(pony)) {
                        if (lastFlightType != FlightType.INSECTOID) {
                            playSound(lastFlightType.getWingFlapSound(), 0.25F, entity.getVoicePitch() * lastFlightType.getWingFlapSoundPitch());
                        }
                        entity.level().gameEvent(entity, GameEvent.ELYTRA_GLIDE, entity.position());
                    }
                } else {
                    strafe *= 0.28;
                }

                if (((LivingEntityDuck)entity).isJumping()) {
                    velocity.y -= 0.2F;
                    velocity.y /= 2F;
                }

                tickStunts(velocity);
            } else {
                tickGrounded();

                if (Abilities.RAINBOOM.canUse(pony.getCompositeRace()) && entity.onGround()) {
                    pony.getMagicalReserves().getCharge().set(0);
                }

                if (!creative && !pony.isClient()) {
                    checkAvianTakeoffConditions(velocity);
                }
            }
        } else {
            tickGrounded();
        }

        float maximum = 1.5F;
        velocity.x = Mth.clamp(velocity.x, -maximum, maximum);
        velocity.y = Mth.clamp(velocity.y, -maximum, maximum);
        velocity.z = Mth.clamp(velocity.z, -maximum, maximum);

        if (!entity.onGround()) {
            float heavyness = EnchantmentUtil.getAirResistance(entity);
            velocity.x /= heavyness;
            velocity.z /= heavyness;
        }

        Vec3 immutable = velocity.toImmutable();
        if (!immutable.equals(lastVel.read())) {
            entity.setDeltaMovement(immutable);
        }

        if (isFlying() && !entity.isFallFlying() && !pony.getAcrobatics().isHanging() && pony.isClient()) {
            if (!MineLPDelegate.getInstance().getPlayerPonyRace(entity).isEquine() && getHorizontalMotion() > 0.03) {
                float pitch = ((LivingEntityDuck)entity).getLeaningPitch();
                if (pitch < 1) {
                    if (pitch < 0.9F) {
                        pitch += 0.1F;
                    }
                    pitch += 0.09F;
                    ((LivingEntityDuck)entity).setLeaningPitch(Math.max(0, pitch));
                }
            }

            LimbAnimationUtil.resetToZero(entity.walkAnimation);
        }
    }

    private void tickGrounded() {
        prevStrafe = 0;
        strafe = 0;
        if (entity.onGround()) {
            ticksInAir = 0;
        }
        wallHitCooldown = MAX_WALL_HIT_CALLDOWN;
        soundPlaying = false;
        descentRate = 0;
        ticksDiving = 0;
        ticksFlyingLow = 0;
        updraft.update(0, 0);
        windStrength.update(0, 0);
    }

    private void tickStunts(MutableVector velocity) {
        boolean canPerformStunts = Abilities.RAINBOOM.canUse(pony.getCompositeRace());
        ticksDiving = canPerformStunts && FlightStuntUtil.isPerformingDive(pony, velocity) ? (ticksDiving + 1) : 0;
        ticksFlyingLow = canPerformStunts && FlightStuntUtil.isFlyingLow(pony, velocity) ? (ticksFlyingLow + 1) : 0;

        if (ticksDiving > 0) {
            float horDiveScale = Mth.clamp(ticksDiving / 100F, 0, 10);
            float verDiveScale = Mth.clamp(ticksDiving / 90F, 0, 5);
            velocity.multiply(1 + horDiveScale, 1 + verDiveScale, 1 + horDiveScale);
        }

        if (ticksFlyingLow > 0) {
            float horDiveScale = Mth.clamp(ticksFlyingLow / 1000F, 0, 0.9F);
            float verDiveScale = Mth.clamp(ticksFlyingLow / 900F, 0, 0.5F);
            velocity.multiply(1 + horDiveScale, 1 + verDiveScale, 1 + horDiveScale);
        }

        if (pony.asEntity().tickCount % 2 == 0 && !pony.isClient()) {
            if (ticksDiving > 0) {
                pony.getMagicalReserves().getCharge().addPercent(1F);
            }
            if (ticksFlyingLow > 0) {
                pony.getMagicalReserves().getCharge().addPercent(ticksFlyingLow / 200F);
            }
        }
    }

    private void tickFlight(FlightType type, MutableVector velocity) {
        if (type.isArtifical()) {
            tickArtificialFlight(velocity);
        } else {
            tickNaturalFlight(velocity);
        }

        entity.fallDistance = 0;

        if (!EffectUtils.hasABrokenWing(entity) || entity.tickCount % 50 < 25) {
            applyThrust(velocity);
        } else if (entity.level().getRandom().nextInt(40) == 0) {
            entity.level().playSound(null, entity, USounds.Vanilla.PLAYER_BIG_FALL, SoundSource.PLAYERS, 2, 1.5F);
            if (!entity.level().isClientSide()) {
                entity.hurtServer((ServerLevel)entity.level(), entity.damageSources().generic(), 0.5F);
            }
        }

        if (type.isAvian() && !pony.isClient()) {
            if (pony.getObservedSpecies() != Race.BAT && entity.level().getRandom().nextInt(9000) == 0) {
                if (!entity.level().isClientSide()) {
                    entity.drop((pony.getObservedSpecies() == Race.HIPPOGRIFF ? UItems.GRYPHON_FEATHER : UItems.PEGASUS_FEATHER).getDefaultInstance(), false, net.minecraft.util.Prediction.SERVER_ONLY);
                }
                playSound(USounds.ENTITY_PLAYER_PEGASUS_MOLT, 0.3F, 1);
                UCriteria.SHED_FEATHER.trigger(entity);
            }
        }

        moveFlying(velocity);

        if (ticksInAir % IDLE_FLAP_INTERVAL == 0 && entity.getDeltaMovement().length() < 0.29) {
            flapping = true;
            ticksToGlide = MAX_TICKS_TO_GLIDE;
        }

        if (type.isAvian()) {
            if (!SpellPredicate.IS_DISGUISE.isOn(pony) && pony.isClient()) {
                if (ticksInAir % GLIDING_SOUND_INTERVAL == 5) {
                    InteractionManager.getInstance().playLoopingSound(entity, InteractionManager.SOUND_GLIDING, entity.getId());
                }
            }
        } else if (type == FlightType.INSECTOID && !SpellPredicate.IS_DISGUISE.isOn(pony)) {
            if (pony.isClient() && !soundPlaying) {
                soundPlaying = true;
                InteractionManager.getInstance().playLoopingSound(entity, InteractionManager.SOUND_CHANGELING_BUZZ, entity.getId());
            }
        }

        velocity.y -= 0.02;
        velocity.x *= 0.9896;
        velocity.z *= 0.9896;
    }

    private void tickArtificialFlight(MutableVector velocity) {
        if (ticksInAir % 10 == 0 && !entity.level().isClientSide()) {
            TrinketsDelegate.EquippedStack stack = AmuletItem.get(entity);
            Charges charges = Charges.of(stack.stack());
            if (charges.canHoldCharge() && charges.getPercentage() < 0.1F) {
                playSound(USounds.ITEM_ICARUS_WINGS_WARN, 0.13F, 0.5F);
            }

            int damageInterval = 20;
            int minDamage = 1;

            float energyConsumed = 2 + (float)getHorizontalMotion() / 10F;
            if (entity.level().isRainingAt(entity.blockPosition())) {
                energyConsumed *= 3;
            }
            if (entity.level().dimension().equals(net.minecraft.world.level.Level.NETHER)) {
                energyConsumed *= 4;
                damageInterval /= 2;
                minDamage *= 3;
            }

            if (charges.canHoldCharge()) {
                Charges.discharge(stack.stack(), (int)energyConsumed);
            }

            if (entity.level().getRandom().nextInt(damageInterval) == 0) {
                stack.stack().hurtAndBreak(minDamage + entity.level().getRandom().nextInt(50), (ServerLevel)entity.level(), (ServerPlayer)entity,
                        broken -> stack.breakStatusSender().accept(broken));
            }

            if (!lastFlightType.canFly(entity)) {
                playSound(USounds.ITEM_ICARUS_WINGS_EXHAUSTED, 1, 2);
                cancelFlight(false);
            }
        }
    }

    private void playSound(SoundEvent sound, float volume, float pitch) {
        entity.level().playSound(entity.level().isClientSide() ? entity : null, entity, sound, SoundSource.PLAYERS, volume, pitch);
    }

    private void tickNaturalFlight(MutableVector velocity) {
        float level = pony.getLevel().getScaled(5) + 1;

        if (ticksInAir > (level * 100)) {
            Bar mana = pony.getMagicalReserves().getMana();

            float cost = (float)-getHorizontalMotion() / 2F;
            if (((LivingEntityDuck)entity).isJumping()) {
                cost /= 10;
            }

            mana.add(Mth.clamp(cost, -100, 0));

            boolean overVoid = PosHelper.isOverVoid(pony.asWorld(), pony.getOrigin(), getGravitySignum());

            if (overVoid) {
                mana.addPercent(-2);
            }

            if (mana.getPercentFill() < (overVoid ? 0.4F : 0.2F)) {
                pony.getMagicalReserves().getExertion().addPercent(overVoid ? 4 : 2);
                pony.getMagicalReserves().getExhaustion().add((overVoid ? 4 : 0) + 2 + (int)(getHorizontalMotion() * 50));

                if (mana.getPercentFill() < 0.1 && ticksInAir % 10 == 0) {
                    float exhaustion = (0.3F * ticksInAir) / 70;
                    if (entity.isSprinting()) {
                        exhaustion *= 3.11F;
                    }

                    entity.causeFoodExhaustion(exhaustion);
                }

                if (pony.getMagicalReserves().getExhaustion().getPercentFill() > 0.99F && ticksInAir % 25 == 0 && !pony.isClient()) {
                    entity.hurtServer((ServerLevel)entity.level(), pony.damageOf(UDamageTypes.EXHAUSTION), entity.level().getRandom().nextIntBetweenInclusive(2, 4));

                    if (entity.level().getRandom().nextInt(110) == 1) {
                        pony.getLevel().add(1);
                        if (Abilities.RAINBOOM.canUse(pony.getCompositeRace())) {
                            pony.getMagicalReserves().getCharge().addPercent(4);
                        }
                        pony.getMagicalReserves().getExertion().set(0);
                        pony.getMagicalReserves().getExhaustion().set(0);
                        mana.set(mana.getMax() * 100);
                        UCriteria.SECOND_WIND.trigger(entity);
                    }
                }
            }
        }
    }

    private void checkAvianTakeoffConditions(MutableVector velocity) {
        double horMotion = getHorizontalMotion();
        double motion = lastVel.lengthSquared();

        boolean takeOffCondition =
                   (horMotion > 0.05 || motion > 0.05)
                && pony.getJumpingHeuristic().hasChanged(Heuristic.TWICE);
        boolean fallingTakeOffCondition = !entity.onGround() && velocity.y < -1.6 && entity.fallDistance > 1;

        if ((takeOffCondition || fallingTakeOffCondition) && !pony.getAcrobatics().isHanging() && !isCancelled) {
            initiateTakeoff(velocity);
        }
    }

    private void initiateTakeoff(MutableVector velocity) {
        startFlying(false);

        velocity.y += getHorizontalMotion() + 0.3;
        applyThrust(velocity);

        velocity.x *= 0.2;
        velocity.z *= 0.2;
    }

    public void startFlying(boolean force) {
        if (force) {
            isCancelled = false;
        }
        setAbleToFly(true);
        isFlyingEither = true;
        isFlyingSurvival = true;
        thrustScale = 0;
        descentRate = 0;
        entity.refreshDimensions();
        pony.sendUpdatePacket();

        if (entity.onGround() || !force) {
            //BlockState steppingState = pony.asEntity().getSteppingBlockState();
            /*if (steppingState.isIn(UTags.Blocks.KICKS_UP_DUST)) {
                pony.addParticle(new BlockStateParticleEffect(UParticles.DUST_CLOUD, steppingState), pony.getOrigin().toCenterPos(), Vec3d.ZERO);
            } else*/ {
                Supplier<Vec3> pos = VecHelper.sphere(pony.asWorld().getRandom(), 0.5D);
                Supplier<Vec3> vel = VecHelper.sphere(pony.asWorld().getRandom(), 0.015D);
                pony.spawnParticles(ParticleTypes.CLOUD, pos, vel, 5);
            }
        }
    }

    private void setAbleToFly(boolean allowFlight) {
        entity.getAbilities().flying = allowFlight;
        BuiltInRegistries.ATTRIBUTE.get(PlayerAttributes.CREATIVE_FLIGHT_ATTRIBUTE).ifPresent(key -> {
            pony.applyAttributeModifier(key, PlayerAttributes.ALLOW_FLIGHT, false, allowFlight);
        });
    }

    private void handleWallCollission(MutableVector velocity) {
        if (wallHitCooldown > 0) {
            return;
        }

        BlockPos pos = BlockPos.containing(entity.getEyePosition(1).add(entity.getViewVector(1).normalize().scale(2)));

        BlockState state = entity.level().getBlockState(pos);

        if (!entity.level().isEmptyBlock(pos) && Block.isFaceFull(state.getCollisionShape(entity.level(), pos), entity.getDirection().getOpposite())) {
            double motion = Math.sqrt(getHorizontalMotion());

            float distance = (float)(motion * 20 - 3);

            if (distance > 0) {
                wallHitCooldown = MAX_WALL_HIT_CALLDOWN;

                float bouncyness = EnchantmentUtil.getBouncyness(entity);
                if (bouncyness > 0) {
                    playSound(USounds.ENTITY_PLAYER_REBOUND, 1, entity.getVoicePitch());
                    ProjectileUtil.ricochet(entity, Vec3.atLowerCornerOf(pos), 0.4F + Math.min(2, bouncyness / 18F));
                    velocity.fromImmutable(entity.getDeltaMovement());
                    distance /= bouncyness;
                } else {
                    LivingEntity.Fallsounds fallSounds = entity.getFallSounds();
                    playSound(distance > 4 ? fallSounds.big() : fallSounds.small(), 1, entity.getVoicePitch());
                }

                if (!entity.level().isClientSide()) {
                    entity.hurtServer((ServerLevel)entity.level(), entity.damageSources().flyIntoWall(), (float)Math.ceil(distance));
                }
            }
        }

        entity.setDeltaMovement(velocity.toImmutable());
        if (ticksColliding > LANDING_COLLISION_TIME) {
            cancelFlight(false);
        }
    }

    private void moveFlying(MutableVector velocity) {
        double motion = getHorizontalMotion();

        float forward = 0.000015F * (1 + (pony.getLevel().getScaled(10) / 10F)) * (float)Math.sqrt(motion);

        // vertical drop due to gravity
        forward += 0.005F;

        velocity.x += - forward * Mth.sin(entity.getYRot() * 0.017453292F);
        velocity.z += forward * Mth.cos(entity.getYRot() * 0.017453292F);

        if (pony.isClient()) {
            float effectChance = 1F - (float)(Mth.clamp(velocity.horizontalLengthSquared(), 0, 1));

            if (entity.level().getRandom().nextInt(1 + (int)(120 * effectChance)) == 0) {
                pony.spawnParticles(new TargetBoundParticleEffect(UParticles.WIND, pony.asEntity()), 3);
            }
        }

        if (entity.level().isRainingAt(entity.blockPosition())) {
            if (!entity.level().isClientSide()) {
                applyTurbulance((ServerLevel)entity.level(), velocity);
            }
        } else {
            float targetUpdraft;
            if (pony.isPosLoaded(entity.blockPosition())) {
                targetUpdraft = WeatherConditions.THERMAL_FIELD.getValue(entity.level(), new BlockPos.MutableBlockPos().set(entity.blockPosition())) / 3F;
                targetUpdraft *= 1 + motion;
                if (isGravityNegative()) {
                    targetUpdraft *= -1;
                }

            } else {
                targetUpdraft = this.updraft.getValue();
            }
            this.updraft.update(targetUpdraft, targetUpdraft > this.updraft.getTarget() ? 30_000 : 3000);
            double updraft = this.updraft.getValue();
            velocity.y += updraft;
            descentRate -= updraft;
        }

        descentRate += 0.001F;
        descentRate = Math.min(1.5F, descentRate);
        if (descentRate < 0) {
            descentRate *= 0.8F;
        }

        velocity.y -= descentRate;
    }

    private void applyThrust(MutableVector velocity) {
        boolean manualFlap = pony.getJumpingHeuristic().hasChanged(Heuristic.ONCE) && pony.getJumpingHeuristic().getState();
        if (manualFlap) {
            flapping = true;
            ticksToGlide = MAX_TICKS_TO_GLIDE;
        }

        thrustScale *= 0.2889F;

        boolean hovering = entity.getDeltaMovement().horizontalDistance() < 0.1;

        if (lastFlightType == FlightType.INSECTOID) {
            descentRate = pony.getJumpingHeuristic().getState() ? -0.5F : 0;
        } else {
            if (thrustScale <= 0.000001F & flapping) {
                flapping = false;
                if (!SpellPredicate.IS_DISGUISE.isOn(pony)) {
                    playSound(lastFlightType.getWingFlapSound(), 0.25F, entity.getVoicePitch() * lastFlightType.getWingFlapSoundPitch());
                    entity.level().gameEvent(entity, GameEvent.ELYTRA_GLIDE, entity.position());
                }
                thrustScale = 1;
                if (manualFlap) {
                    descentRate -= 0.5;
                } else {
                    descentRate *= 0.25F;
                    if (velocity.y < 0) {
                        velocity.y *= 0.6F;
                    }
                }
            }
        }

        float thrustStrength = (0.235F * thrustScale) / EnchantmentUtil.getWeight(entity);
        Vec3 direction = entity.getViewVector(1).normalize().scale(thrustStrength);

        if (hovering || !manualFlap) {
            if (entity.isShiftKeyDown()) {
                velocity.y -= 0.006F;
            } else {
                velocity.y *= 1 - thrustScale;
            }
        } else {
            velocity.x += direction.x * 1.3F;
            velocity.z += direction.z * 1.3F;
            velocity.y += ((direction.y * 2.45 + Math.abs(direction.y) * 10));
        }

        if (velocity.y < 0 && hovering) {
            velocity.y *= 0.01;
        }
    }

    private void applyTurbulance(ServerLevel world, MutableVector velocity) {
        int globalEffectStrength = world.getGameRules().get(UGameRules.WEATHER_EFFECTS_STRENGTH);
        float effectStrength = Math.min(1, (float)ticksInAir / MAX_TICKS_TO_WEATHER_EFFECTS) * (globalEffectStrength / 100F);
        Vec3 gust = WeatherConditions.getGustStrength(entity.level(), entity.blockPosition())
                .scale(globalEffectStrength / 100D)
                .scale(1 / (1 + Math.floor(pony.getLevel().get() / 10F)));

        if (effectStrength * gust.x() >= 1) {
            SoundEmitter.playSoundAt(entity, USounds.AMBIENT_WIND_GUST, SoundSource.AMBIENT, 3, 1);
        }

        float weight = EnchantmentUtil.getWindBuffetResistance(entity);

        Vec3 airflow = WeatherConditions.getAirflow(entity.blockPosition(), entity.level())
                .scale(0.04F * effectStrength)
                .add(Vec3.directionFromRotation(
                    (entity.getXRot() + (float)gust.y()),
                    (entity.getYRot() + (float)gust.z())
                ).scale(effectStrength * (float)gust.x() / weight));

        windStrength.update((float)airflow.length(), airflow.length() > windStrength.getValue() ? 1000 : 500);
        velocity.add(airflow.normalize(), windStrength.getValue());

        if (!entity.level().isClientSide() && effectStrength > 0.9F && entity.level().isThundering() && entity.level().getRandom().nextInt(9000) == 0) {
            LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(entity.level(), EntitySpawnReason.EVENT);
            lightning.snapTo(entity.getX(), entity.getY(), entity.getZ());

            entity.level().addFreshEntity(lightning);
            UCriteria.LIGHTNING_STRUCK.trigger(entity);
        }
    }

    /**
     * Called when a player's species changes to update whether they can fly or not
     */
    public void updateFlightState() {
        FlightType type = recalculateFlightType();
        setAbleToFly(entity.getAbilities().flying & type.canFly(entity));
        isFlyingSurvival = entity.getAbilities().flying;
        lastFlightType = type;
    }

    public void dashForward(float speed) {
        if (pony.isClient()) {
            return;
        }

        Vec3 orientation = entity.getViewVector(1).scale(speed);
        entity.push(orientation.x, orientation.y, orientation.z);

        boolean isEarthPonySmash = pony.getObservedSpecies().canUseEarth() && !isFlying();
        int damage = TraceHelper.findBlocks(entity, speed + (isEarthPonySmash ? 2 : 4), 1, state -> (isEarthPonySmash && !state.isAir()) || state.is(ConventionalBlockTags.GLASS_PANES)).stream()
            .flatMap(pos -> com.minelittlepony.unicopia.util.PosHelper.withinManhattanStream(pos, 2, 2, 2))
            .filter(pos -> (isEarthPonySmash && !entity.level().isEmptyBlock(pos) && pos.getY() >= entity.getBlockY()) || entity.level().getBlockState(pos).is(ConventionalBlockTags.GLASS_PANES))
            .reduce(0, (u, pos) -> {
                if (pony.canModifyAt(pos, ModificationType.PHYSICAL)) {
                    if (isEarthPonySmash) {
                        float destruction = BlockDestructionManager.of(entity.level()).damageBlock(pos, (int)entity.level().getRandom().triangle(5, 3));
                        if (destruction >= BlockDestructionManager.MAX_DAMAGE - 1 && pony.canModifyAt(pos, ModificationType.PHYSICAL)) {
                            entity.level().destroyBlock(pos, true);
                        } else {
                            entity.level().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(entity.level().getBlockState(pos)));
                        }
                    } else {
                        entity.level().destroyBlock(pos, true);
                    }
                } else {
                    ParticleUtils.spawnParticles(new MagicParticleEffect(0x00AAFF), entity.level(), Vec3.atCenterOf(pos), 15);
                }
                return 1;
            }, Integer::sum);

        if (damage > 0) {
            pony.subtractEnergyCost(damage / 5F);
            if (!entity.level().isClientSide()) {
                entity.hurtServer((ServerLevel)entity.level(), entity.damageSources().flyIntoWall(), Math.min(damage, entity.getHealth() - 1));
            }
            if (!isEarthPonySmash) {
                UCriteria.BREAK_WINDOW.trigger(entity);
            }
        }

        if (isEarthPonySmash && !entity.level().isClientSide()) {
            DamageSource damageSource = pony.damageOf(UDamageTypes.STEAMROLLER);
            pony.findAllEntitiesInRange(speed + 4, EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE)).forEach(e -> e.hurtServer((ServerLevel)entity.level(), damageSource, 50));
        }

        pony.updateVelocity();

        pony.spawnParticles(new TargetBoundParticleEffect(UParticles.WIND, pony.asEntity()), 4);

        if (isFlying()) {
            playSound(USounds.ENTITY_PLAYER_PEGASUS_DASH, 1, 1);
        } else {
            playSound(USounds.ENTITY_PLAYER_EARTHPONY_DASH, 2, 1.3F);
        }
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        compound.putBoolean("isFlying", isFlyingSurvival);
        compound.putBoolean("isCancelled", isCancelled);
        compound.putBoolean("isFlyingEither", isFlyingEither);
        compound.putInt("ticksInAir", ticksInAir);
        compound.putFloat("descentRate", descentRate);
        compound.putFloat("updraft", updraft.getValue());
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        isFlyingSurvival = compound.getBooleanOr("isFlying", false);
        isCancelled = compound.getBooleanOr("isCancelled", false);
        isFlyingEither = compound.getBooleanOr("isFlyingEither", false);
        ticksInAir = compound.getIntOr("ticksInAir", 0);
        descentRate = compound.getFloatOr("descentRate", 0);
        updraft.update(compound.getFloatOr("updraft", 0), 0);

        entity.refreshDimensions();
    }
}
