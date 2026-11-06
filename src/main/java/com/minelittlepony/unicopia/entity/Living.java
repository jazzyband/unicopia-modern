package com.minelittlepony.unicopia.entity;

import java.util.*;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellInventory;
import com.minelittlepony.unicopia.ability.magic.SpellSlots;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.behaviour.Guest;
import com.minelittlepony.unicopia.entity.damage.MagicalDamageSource;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.effect.CorruptInfluenceStatusEffect;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.input.Heuristic;
import com.minelittlepony.unicopia.input.Interactable;
import com.minelittlepony.unicopia.item.GlassesItem;
import com.minelittlepony.unicopia.item.TotemOfDying;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.component.BreaksIntoItemComponent;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.DataTrackerManager;
import com.minelittlepony.unicopia.network.track.Trackable;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.projectile.ProjectileImpactListener;
import com.minelittlepony.unicopia.server.world.DragonBreathStore;
import com.minelittlepony.unicopia.util.*;
import com.minelittlepony.unicopia.util.serialization.NbtMap;
import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public abstract class Living<T extends LivingEntity> implements Equine<T>, Caster<T>, AttributeContainer {
    protected final T entity;

    private final SpellInventory spells;

    private final Interactable sneakingHeuristic;
    private final Interactable landedHeuristic;
    private final Interactable jumpingHeuristic;

    private boolean invisible = false;

    @Nullable
    private Caster<?> attacker;
    @Nullable
    private transient Caster<?> host;

    private Optional<Living<?>> target = Optional.empty();


    private int invinsibilityTicks;

    private final List<Tickable> tickers = new ArrayList<>();

    private final LandingEventHandler landEvent = addTicker(new LandingEventHandler(this));
    private final NbtMap<Identifier, Float> enchants = NbtMap.of(Identifier.CODEC, Codec.FLOAT);
    private final ItemTracker armour = addTicker(new ItemTracker(this));
    private final Transportation<T> transportation = new Transportation<>(this);

    protected final DataTrackerManager trackers;
    protected final DataTracker tracker;

    private final DataTracker.Entry<Integer> invulnerableTicks;
    protected final DataTracker.Entry<Optional<UUID>> carrierId;

    protected Living(T entity) {
        this.entity = entity;
        this.trackers = Trackable.of(entity).getDataTrackers();
        this.tracker = trackers.getPrimaryTracker();
        this.spells = SpellSlots.ofUnbounded(this);
        this.sneakingHeuristic = addTicker(new Interactable(entity::isShiftKeyDown));
        this.landedHeuristic = addTicker(new Interactable(entity::onGround));
        this.jumpingHeuristic = addTicker(new Interactable(((LivingEntityDuck)entity)::isJumping));

        carrierId = tracker.startTracking(TrackableDataType.UUID, Optional.empty());
        invulnerableTicks = tracker.startTracking(TrackableDataType.INT, -1);
    }

    public <Q extends Tickable> Q addTicker(Q tickable) {
        tickers.add(Objects.requireNonNull(tickable, "tickable cannot be null"));
        return tickable;
    }

    public int getInvulnerabilityTicks() {
        return invulnerableTicks.get();
    }

    public void setInvulnerabilityTicks(int ticks) {
        invulnerableTicks.set(entity.tickCount + Math.max(0, ticks));
    }

    public boolean isInvulnerable() {
        return invulnerableTicks.get() > entity.tickCount;
    }

    public boolean isInvisible() {
        return invisible && SpellPredicate.IS_DISGUISE.isOn(this);
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    public void waitForFall(LandingEventHandler.Callback callback) {
        landEvent.setCallback(callback);
    }

    public boolean sneakingChanged() {
        return sneakingHeuristic.hasChanged(Heuristic.ONCE);
    }

    public boolean landedChanged() {
        return landedHeuristic.hasChanged(Heuristic.ONCE);
    }

    public Interactable getJumpingHeuristic() {
        return jumpingHeuristic;
    }

    @Override
    public SpellSlots getSpellSlot() {
        return spells.getSlots();
    }

    public NbtMap<Identifier, Float> getEnchants() {
        return enchants;
    }

    public ItemTracker getArmour() {
        return armour;
    }

    public Transportation<T> getTransportation() {
        return transportation;
    }

    @Override
    public final T asEntity() {
        return entity;
    }

    public Optional<UUID> getCarrierId() {
        return carrierId.get();
    }

    public void setCarrier(UUID carrier) {
        carrierId.set(Optional.ofNullable(carrier));
    }

    public void setCarrier(Entity carrier) {
        setCarrier(carrier == null ? null : carrier.getUUID());
    }

    @Nullable
    public Optional<Living<?>> getTarget() {
        return target;
    }

    public void setTarget(Living<?> target) {
        this.target = Optional.ofNullable(target);
    }

    public boolean isBeingCarried() {
        Entity vehicle = entity.getVehicle();
        return vehicle != null && getCarrierId().filter(vehicle.getUUID()::equals).isPresent();
    }

    @Override
    public boolean hasFeatherTouch() {
        return EnchantmentUtil.hasAnyEnchantmentsIn(entity, UTags.Enchantments.PERMITS_CLOUD_INTERACTION);
    }

    @Override
    public boolean beforeUpdate() {
        landEvent.beforeTick();
        if (entity.hasEffect(UEffects.PARALYSIS) && entity.getDeltaMovement().horizontalDistanceSqr() > 0) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0, 1, 0));
            updateVelocity();
        }

        return false;
    }

    @Override
    public void tick() {
        tickers.forEach(Tickable::tick);
        spells.tick(Situation.BODY);

        if (!(entity instanceof Player)) {
            if (!entity.isPassenger() && getCarrierId().isPresent() && !asWorld().isClientSide() && entity.tickCount % 10 == 0) {
                UUID carrierId = getCarrierId().get();
                Entity carrier = asWorld().getEntity(carrierId);
                if (carrier != null) {
                    asEntity().startRiding(carrier, true, true);
                    Living.transmitPassengers(carrier);
                } else {
                    Unicopia.LOGGER.warn("No passenger with id {]", carrierId);
                }
            }
        }

        if (invinsibilityTicks > 0) {
            invinsibilityTicks--;
        }

        if (entity.hasEffect(UEffects.PARALYSIS) && entity.getDeltaMovement().horizontalDistanceSqr() > 0) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0, 1, 0));
            updateVelocity();
        }

        if (isBeingCarried()) {
            Pony carrier = Pony.of(entity.getVehicle()).orElse(null);
            if (!Abilities.CARRY.canUse(carrier.getCompositeRace()) && !Abilities.HUG.canUse(carrier.getCompositeRace())) {
                entity.stopRiding();
                entity.snapTo(carrier.getOriginVector());
                Living.transmitPassengers(carrier.asEntity());
            }
            entity.setYRot(carrier.asEntity().getYRot());
        }

        updateDragonBreath();

        if (EnchantmentUtil.getGemfinderLevel(entity) > 0) {
            InteractionManager.getInstance().playLoopingSound(entity, InteractionManager.SOUND_GEM_FINDING_MAGIC_HUM, 0);
        }

        transportation.tick();
    }

    @Override
    public final @Nullable AttributeInstance getAttributeInstance(Holder<Attribute> attribute) {
        return asEntity().getAttribute(attribute);
    }

    public boolean canBeSeenBy(Entity entity) {
        return !isInvisible()
            && getSpellSlot()
            .get(SpellPredicate.IS_DISGUISE)
            .filter(spell -> spell.getAppearance().getEntity() == entity)
            .isEmpty();
    }

    public Optional<Vec3> adjustMovementSpeedInWater(Vec3 speed) {
        return Optional.empty();
    }

    private void updateDragonBreath() {
        if (!entity.level().isClientSide() && (entity instanceof Player || entity.hasCustomName())) {

            Vec3 targetPos = entity.getLookAngle().scale(2).add(entity.getEyePosition());

            if (entity.level().isEmptyBlock(BlockPos.containing(targetPos))) {
                String name = entity.getDisplayName().getString();

                DragonBreathStore.popAll(entity.level().getServer(), name).forEach(pair -> {
                    var store = pair.getFirst();
                    pair.getSecond().forEach(stack -> {
                        ItemStack payload = stack.payload();
                        Item item = payload.getItem();

                        boolean deliverAggressively = payload.is(UTags.Items.IS_DELIVERED_AGGRESSIVELY);

                        Vec3 randomPos = deliverAggressively ? targetPos.add(0, 2, 0) : targetPos.add(VecHelper.triangular(entity.getRandom(), 0.1, 0.5));

                        if (deliverAggressively && item instanceof BlockItem blockItem) {
                            do {
                                ItemStack instance = payload.split(1);
                                BlockPos pos = BlockPos.containing(randomPos);
                                if (!entity.level().isEmptyBlock(pos)) {
                                    store.put(name, instance);
                                } else {

                                    for (int i = 0; i < 10; i++) {
                                        ParticleUtils.spawnParticle(entity.level(), ParticleTypes.FLAME, randomPos.add(
                                                VecHelper.triangular(entity.getRandom(), 0.1, 0.5)
                                        ), Vec3.ZERO);
                                    }

                                    BlockPlaceContext context = new BlockPlaceContext(entity.level(), (Player)null, InteractionHand.MAIN_HAND, instance,
                                            BlockHitResult.miss(Vec3.ZERO, Direction.UP, pos)
                                    );

                                    BlockState state = blockItem.getBlock().getStateForPlacement(context);
                                    if (state == null) {
                                        state = blockItem.getBlock().defaultBlockState();
                                    }

                                    entity.level().setBlockAndUpdate(pos, state);
                                    SoundType sound = state.getSoundType();
                                    entity.level().playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1) * 0.5F, sound.getPitch() * 0.8F);
                                }
                                randomPos = targetPos.add(VecHelper.triangular(entity.getRandom(), 0.1, 0.5));
                            } while (!payload.isEmpty());
                        } else {
                            if (!entity.level().isEmptyBlock(BlockPos.containing(randomPos))) {
                                store.put(name, stack.payload());
                            } else {
                                for (int i = 0; i < 10; i++) {
                                    ParticleUtils.spawnParticle(entity.level(), ParticleTypes.FLAME, randomPos.add(VecHelper.triangular(entity.getRandom(), 0.1, 0.5)), Vec3.ZERO);
                                }

                                ItemEntity itemEntity = EntityTypes.ITEM.create(entity.level(), EntitySpawnReason.EVENT);
                                itemEntity.setItem(payload);
                                itemEntity.setPos(randomPos);
                                itemEntity.level().addFreshEntity(itemEntity);
                                entity.level().playSound(null, entity, USounds.ITEM_DRAGON_BREATH_ARRIVE, entity.getSoundSource(), 1, 1);
                                UCriteria.SEND_DRAGON_BREATH.triggerReceived(entity, payload.copy());
                            }
                        }
                    });
                });
            }
        }
    }

    public boolean onUpdatePassengerPosition(Entity passender, Entity.MoveFunction positionUpdater) {
        return false;
    }

    @Nullable
    public final Caster<?> getAttacker() {
        return attacker;
    }

    public Optional<Boolean> onDamage(ServerLevel world, DamageSource source, float amount) {

        if (entity.getRootVehicle() instanceof LevitatingItemEntity && (source.is(DamageTypes.IN_WALL) || source.is(DamageTypeTags.IS_FALL))) {
            return Optional.of(false);
        }

        if (Guest.of(source.getEntity()).hostIs(this)
            || Guest.of(source.getDirectEntity()).hostIs(this)) {
            var type = source.typeHolder();
            return Optional.of(entity.hurtServer(world,
                    type.is(DamageTypes.FIREBALL) ? entity.damageSources().source(DamageTypes.UNATTRIBUTED_FIREBALL) :
                    type.is(DamageTypes.PLAYER_EXPLOSION) ? entity.damageSources().source(DamageTypes.EXPLOSION) :
                    new DamageSource(type, entity, entity), amount));
        }

        if (Guest.of(entity).getHost() instanceof Living l) {
            l.asEntity().hurtServer(world, source, amount);
        }

        if (source.is(DamageTypeTags.IS_LIGHTNING) && (invinsibilityTicks > 0 || tryCaptureLightning(world))) {
            return Optional.of(false);
        }

        if (source instanceof MagicalDamageSource magical) {
            Caster<?> attacker = magical.getSpell();
            if (attacker != null) {
                this.attacker = attacker;
            }

            TrinketsDelegate.EquippedStack glasses = GlassesItem.getForEntity(entity);
            BreaksIntoItemComponent afterBroken = glasses.stack().get(UDataComponentTypes.ITEM_AFTER_BREAKING);

            if (afterBroken != null && magical.is(afterBroken.damageType())) {
                if (afterBroken != null) {
                    afterBroken.getItemAfterBreaking().ifPresent(b -> {
                        glasses.swap(glasses.stack().transmuteCopy(b));
                        afterBroken.getBreakingSound().ifPresent(sound -> {
                            playSound(sound, 1, 1);
                        });
                    });
                }
            }
        }

        if (entity instanceof Monster mob && mob.hasEffect(UEffects.CORRUPT_INFLUENCE) && mob.getRandom().nextInt(4000) == 0) {
            CorruptInfluenceStatusEffect.reproduce(mob);
        }

        return Optional.empty();
    }

    public void onDeath(DamageSource damage, boolean usedTotem) {
        TotemOfDying.tryUseTotem(damage, entity);
    }

    public void onAttacking(Entity target) {

    }

    public TriState canBeHurtByWater() {
        return TriState.DEFAULT;
    }

    public Optional<BlockPos> chooseClimbingPos() {
        return getSpellSlot().get(SpellPredicate.IS_DISGUISE)
                .map(AbstractDisguiseSpell::getAppearance)
                .filter(EntityAppearance::canClimbWalls)
                .map(v -> entity.blockPosition());
    }

    private boolean tryCaptureLightning(ServerLevel world) {
        return getInventoryStacks().filter(stack -> stack.is(UItems.EMPTY_JAR)).findFirst().map(stack -> {
            invinsibilityTicks = 20;
            stack.split(1);
            giveBackItem(world, UItems.LIGHTNING_JAR.getDefaultInstance());
            return stack;
        }).isPresent();
    }

    protected Stream<ItemStack> getInventoryStacks() {
        return Stream.of(entity.getItemInHand(InteractionHand.MAIN_HAND), entity.getItemInHand(InteractionHand.OFF_HAND));
    }

    public Stream<ItemStack> getArmourStacks() {
        return EquipmentUtil.getArmor(entity);
    }

    protected void giveBackItem(ServerLevel world, ItemStack stack) {
        entity.spawnAtLocation(world, stack);
    }

    @Override
    public boolean onProjectileImpact(Projectile projectile) {
        return getSpellSlot().get()
                .filter(effect -> !effect.isDead()
                        && effect instanceof ProjectileImpactListener
                        && ((ProjectileImpactListener)effect).onProjectileImpact(projectile))
                .isPresent();
    }

    public double onImpact(double distance, float damageMultiplier, DamageSource cause) {
        double fallDistance = landEvent.fire(getEffectiveFallDistance(distance));

        getSpellSlot().get(SpellPredicate.IS_DISGUISE).ifPresent(spell -> {
            spell.getAppearance().onImpact(this, fallDistance, damageMultiplier, cause);
        });
        return fallDistance;
    }

    public void onDismounted(Entity vehicle) {
        setCarrier((UUID)null);
    }

    protected double getEffectiveFallDistance(double distance) {
        return distance;
    }

    public void onDropItem(ItemEntity itemDropped) {
        Equine.of(itemDropped).ifPresent(eq -> {
            eq.setSpecies(getSpecies());
            eq.getPhysics().setBaseGravityModifier(getPhysics().getPersistantGravityModifier());
            if (eq.getPhysics().isGravityNegative()) {
                eq.asEntity().setDeltaMovement(eq.asEntity().getDeltaMovement().multiply(1, -1, 1));
            }
        });
    }

    @Override
    public float getCloudWalkingStrength() {
        return asWorld().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(net.minecraft.world.item.enchantment.Enchantments.FEATHER_FALLING.identifier()).map(featherFalling -> {
            int maxLevel = featherFalling.value().getMaxLevel();
            int level = EnchantmentHelper.getEnchantmentLevel(featherFalling, entity);
            return Mth.clamp(level / (float)maxLevel, 0, 1);
        }).orElse(0F);
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        enchants.toNBT(compound, lookup);
        spells.getSlots().toNBT(compound, lookup);
        compound.storeNullable("carrier", UUIDUtil.AUTHLIB_CODEC, getCarrierId().orElse(null));
        compound.putInt("ticksInvulnerable", getInvulnerabilityTicks());
        toSyncronisedNbt(compound, lookup);
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        enchants.fromNBT(compound, lookup);
        spells.getSlots().fromNBT(compound, lookup);
        setCarrier(compound.read("carrier", UUIDUtil.AUTHLIB_CODEC).orElse(null));
        setInvulnerabilityTicks(compound.getIntOr("ticksInvulnerable", 0));
        fromSynchronizedNbt(compound, lookup);
    }

    public void toSyncronisedNbt(CompoundTag compound, Provider lookup) {
        compound.put("armour", armour.toNBT(lookup));
    }

    public void fromSynchronizedNbt(CompoundTag compound, Provider lookup) {
        armour.fromNBT(compound.getCompoundOrEmpty("armour"), lookup);
    }

    public void updateVelocity() {
        updateVelocity(entity);
    }

    @Override
    public boolean canUse(Ability<?> ability) {
        return ability.canUse(getCompositeRace());
    }

    public static Optional<Living<?>> getOrEmpty(Entity entity) {
        return Equine.of(entity, a -> a instanceof Living);
    }

    public static Living<?> living(Entity entity) {
        return getOrEmpty(entity).orElse(null);
    }

    public static <E extends LivingEntity> Living<E> living(E entity) {
        return Equine.<E, Living<E>>of(entity, e -> e instanceof Living<?>).orElse(null);
    }

    public static void updateVelocity(@Nullable Entity entity) {
        if (entity != null) {
            entity.syncVelocity = true;
            //if (entity instanceof ServerPlayerEntity ply) {
            //    ply.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(ply));
            //}
        }
    }

    public static void transmitPassengers(@Nullable Entity entity) {
        if (entity != null && entity.level() instanceof ServerLevel sw) {
            sw.getChunkSource().sendToTrackingPlayersAndSelf(entity, new ClientboundSetPassengersPacket(entity));
        }
    }
}
