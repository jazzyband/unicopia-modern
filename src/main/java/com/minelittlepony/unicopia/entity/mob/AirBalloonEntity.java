package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.collision.MultiBox;
import com.minelittlepony.unicopia.item.BasketItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.component.BalloonDesignComponent;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.server.world.WeatherConditions;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;

public class AirBalloonEntity extends FlyingVehicleEntity {
    private int getBurnDuration(ServerLevel world, ItemStack stack) {
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(world)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE, net.minecraft.world.level.block.Blocks.FURNACE.defaultBlockState())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, position())
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ALL_PARAMS);
        var context = new net.minecraft.world.level.storage.loot.LootContext.Builder(params).create(java.util.Optional.empty());
        return net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.getFromItem(
                stack, net.minecraft.core.component.DataComponents.COOKING_FUEL,
                net.minecraft.world.item.component.CookingFuel::burnTime, context, 0);
    }

    private static final EntityDataAccessor<Boolean> ASCENDING = SynchedEntityData.defineId(AirBalloonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BOOSTING = SynchedEntityData.defineId(AirBalloonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INFLATION = SynchedEntityData.defineId(AirBalloonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> BASKET_TYPE = SynchedEntityData.defineId(AirBalloonEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> BALLOON_DESIGN = SynchedEntityData.defineId(AirBalloonEntity.class, EntityDataSerializers.INT);

    private static final Codec<List<ItemStack>> FUEL_CODEC = ItemStack.OPTIONAL_CODEC.listOf();

    private static final int BASKET_GRID_SIZE = 2;
    private static final int TOP_GRID_SIZE = 4;

    public static final byte STATUS_BURNER_INTERACT = (byte)105;

    private static final Predicate<Entity> RIDER_PREDICATE = EntitySelector.NO_SPECTATORS.and(e -> {
        return !(e instanceof Player p && p.getAbilities().flying);
    });

    private boolean prevBoosting;
    private int prevInflation;
    private Vec3 manualVelocity = Vec3.ZERO;

    private int maxFuel = 10000;
    private int activeFuel;
    private List<ItemStack> fuelItems = new ArrayList<>();

    private final Animatable[] sandbags = IntStream.range(0, 5).mapToObj(Animatable::new).toArray(Animatable[]::new);
    private final Animatable burner = new Animatable(5);

    private double prevXDelta;
    private double xDelta;
    private double prevZDelta;
    private double zDelta;

    public AirBalloonEntity(EntityType<? extends AirBalloonEntity> type, Level world) {
        super(type, world);
        blocksBuilding = true;
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ASCENDING, false);
        builder.define(BOOSTING, 0);
        builder.define(INFLATION, 0);
        builder.define(BASKET_TYPE, BasketType.DEFAULT.id().toString());
        builder.define(BALLOON_DESIGN, 0);
    }

    @Override
    protected int getSeatCount() {
        return (BASKET_GRID_SIZE * BASKET_GRID_SIZE) + (TOP_GRID_SIZE * TOP_GRID_SIZE);
    }

    @Override
    protected Vec3 getSeatPosition(int seatIndex) {
        double y = 0.2;
        double alignment = -0.5;
        int gridSize = BASKET_GRID_SIZE;
        if (seatIndex >= 4) {
            seatIndex -= 4;
            y = 11.125;
            alignment = -1.75;
            gridSize = TOP_GRID_SIZE;
        }
        return new Vec3(alignment + seatIndex % gridSize, y, alignment + seatIndex / gridSize);
    }

    public BasketType getBasketType() {
        return BasketType.of(entityData.get(BASKET_TYPE));
    }

    public void setBasketType(BasketType type) {
        entityData.set(BASKET_TYPE, type.id().toString());
    }

    public BalloonDesign getDesign() {
        return BalloonDesign.getType(entityData.get(BALLOON_DESIGN));
    }

    public void setDesign(BalloonDesign design) {
        entityData.set(BALLOON_DESIGN, design.ordinal());
    }

    public Animatable getSandbag(int index) {
        return sandbags[Mth.clamp(index, 0, sandbags.length - 1)];
    }

    public Animatable getBurner() {
        return burner;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return equipment == null ? ItemStack.EMPTY : super.getItemBySlot(slot);
    }

    public boolean hasBalloon() {
        return getDesign() != BalloonDesign.NONE;
    }

    public boolean hasBurner() {
        return hasItemInSlot(EquipmentSlot.MAINHAND);
    }

    public float getInflation(float tickDelta) {
        return Mth.lerpInt(tickDelta, prevInflation, getInflation()) / (float)getMaxInflation();
    }

    private void setInflation(int inflation) {
        entityData.set(INFLATION, Mth.clamp(inflation, 0, getMaxInflation()));
    }

    private int getInflation() {
        return entityData.get(INFLATION);
    }

    protected int getMaxInflation() {
        return 100;
    }

    public boolean isAscending() {
        return hasBalloon() && entityData.get(ASCENDING);
    }

    public void setAscending(boolean ascending) {
        entityData.set(ASCENDING, ascending);
    }

    public int getBoostTicks() {
        return entityData.get(BOOSTING);
    }

    protected void setBoostTicks(int ticks) {
        entityData.set(BOOSTING, ticks);
    }

    private boolean isAirworthy() {
        return hasBalloon() && hasBurner() && getInflation() >= getMaxInflation();
    }

    public float getXVelocity(float tickDelta) {
        return (float)Mth.lerp(tickDelta, prevXDelta, xDelta);
    }

    public float getZVelocity(float tickDelta) {
        return (float)Mth.lerp(tickDelta, prevZDelta, zDelta);
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAMBOO_WOOD_BREAK;
    }

    @Override
    public void tick() {
        setAirSupply(getMaxAirSupply());
        int boostTicks = getBoostTicks();

        int inflation = getInflation();
        prevInflation = inflation;

        if (boostTicks > 0) {
            boostTicks--;
            if (inflation < getMaxInflation()) {
                boostTicks--;
            }
            setBoostTicks(boostTicks);
        }

        boolean boosting = boostTicks > 0;

        if (hasBurner() && isAscending()) {
            if (inflation < getMaxInflation()) {
                inflation++;
                if (boosting) {
                    inflation++;
                }
                setInflation(inflation);
            }

            if (level() instanceof ServerLevel sw) {
                if (activeFuel <= 0 && !fuelItems.isEmpty()) {
                    activeFuel = getBurnDuration(sw, fuelItems.remove(0));
                }
            }

            if (activeFuel > -6 && tickCount % 2 == 0) {
                activeFuel -= boosting ? 50 : 1;
                if (activeFuel <= -6) {
                    setBoostTicks(0);
                    setAscending(false);
                }
            }
        } else {
            if (inflation < getMaxInflation() && inflation > 0) {
                setInflation(--inflation);
            }
        }

        if (isAirworthy()) {
            push(0, isAscending() && inflation >= getMaxInflation() ? 0.005 : -0.013, 0);
            push(manualVelocity.scale(this.getDeltaMovement().y > 0.01F ? 0.1 : 0.01));
        }
        manualVelocity = manualVelocity.scale(0.9);

        if (!(hasBalloon() && isAscending()) && isUnderWater()) {
            setDeltaMovement(getDeltaMovement().multiply(0.9, 0.4, 0.9).add(0, 0.02, 0));
        }

        RandomSource rng = level().getRandom();

        if (level().isClientSide()) {
            if (hasBurner() && isAscending()) {
                Vec3 burnerPos = position().add(0, 3, 0);
                for (int i = 0; i < (boosting ? 6 : 1); i++) {
                    level().addParticle(activeFuel <= 0
                                ? ParticleTypes.SMOKE
                                : getItemInHand(InteractionHand.MAIN_HAND).is(Items.SOUL_LANTERN)
                                    ? ParticleTypes.SOUL_FIRE_FLAME
                                    : ParticleTypes.FLAME,
                            rng.triangle(burnerPos.x, 0.25),
                            rng.triangle(burnerPos.y, 1),
                            rng.triangle(burnerPos.z, 0.25),
                            0,
                            (boosting ? 0.1 : 0),
                            0
                    );
                }
            }
        } else if (inflation >= getMaxInflation()) {
            if (hasBurner() && isAscending()) {
                push(WeatherConditions.getAirflow(blockPosition(), level()).scale(0.2));
                setDeltaMovement(getDeltaMovement().multiply(0.3, 1, 0.3));
            }

            if (boosting) {
                push(0, 0.02, 0);
            }
        }

        if (boosting && !prevBoosting) {
            playSound(USounds.ENTITY_HOT_AIR_BALLOON_BOOST, 1, 1);
        }

        if (isAscending() && tickCount % 15 + rng.nextInt(5) == 0) {
            playSound(USounds.ENTITY_HOT_AIR_BALLOON_BURNER_FIRE, 0.2F, 1);
            level().gameEvent(null, GameEvent.FLAP, blockPosition());
        }

        if (isLeashed()) {
            Vec3 leashPost = getLeashHolder().position();
            Vec3 pos = position();

            if (leashPost.distanceTo(pos) >= 5) {
                Vec3 newVel = leashPost.subtract(pos).scale(0.01);
                if (isAirworthy()) {
                    setDeltaMovement(newVel.lengthSqr() < 0.0001 ? Vec3.ZERO : newVel);
                } else {
                    setDeltaMovement(getDeltaMovement().scale(0.9).add(newVel));
                }
            }
        }

        prevBoosting = boosting;

        if (getRemainingFireTicks() > 0) {
            setRemainingFireTicks(1);
        }

        for (Animatable bag : sandbags) {
            bag.tick();
        }
        burner.tick();

        super.tick();

        if (!level().isClientSide() && this.syncVelocity) {
            this.reapplyPosition();
        }

        prevXDelta = xDelta;
        prevZDelta = zDelta;
        xDelta = getX() - xo;
        zDelta = getZ() - zo;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 relativePositionOffset) {
        ItemStack stack = player.getItemInHand(hand);

        if (hasBalloon() && hasBurner()) {

            if (getBurnerBoundingBox(getBoundingBox()).inflate(0.7).contains(position().add(relativePositionOffset))) {
                if (stack.is(Items.FLINT_AND_STEEL)) {
                    setAscending(!isAscending());
                    if (isAscending()) {
                        playSound(USounds.ENTITY_HOT_AIR_BALLOON_BOOST, 1, 1);
                    }
                    stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
                    playSound(USounds.Vanilla.FLINTANDSTEEL_USE, 1, 1);
                    if (!player.isDiscrete()) {
                        level().gameEvent(player, GameEvent.ENTITY_INTERACT, blockPosition());
                    }
                    burner.setPulling();
                    return InteractionResult.SUCCESS;
                }

                if (stack.isEmpty() && isAscending()) {
                    setBoostTicks(50);
                    playSound(USounds.ENTITY_HOT_AIR_BALLOON_BOOST, 1, 1);
                    burner.setPulling();
                    if (!player.isDiscrete()) {
                        level().gameEvent(player, GameEvent.ENTITY_INTERACT, blockPosition());
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            if (getInflation(1) >= 1) {
                int xPush = (int)Math.signum(relativePositionOffset.x);
                int zPush = (int)Math.signum(relativePositionOffset.z);

                Vec3 absHitPos = position().add(relativePositionOffset);

                if (stack.isEmpty() && MultiBox.unbox(getBoundingBox()).inflate(0.5, 1, 0.5).move(2 * xPush, 3, 2 * zPush).contains(absHitPos)) {
                    if (!level().isClientSide()) {
                        manualVelocity = manualVelocity.add(1.7 * xPush, 0, 1.7 * zPush);
                    }
                    level().playSound(null, getX() + relativePositionOffset.x(), getY() + relativePositionOffset.y(), getZ() + relativePositionOffset.z(), net.minecraft.sounds.SoundEvents.CHAIN_PLACE, getSoundSource(), 1, 1);
                    if (!player.isDiscrete()) {
                        level().gameEvent(player, GameEvent.ENTITY_INTERACT, blockPosition());
                    }

                    int sandbagId = Mth.clamp(-xPush, 0, 1) + Mth.clamp(-zPush, 0, 1) * 2;

                    getSandbag(sandbagId).setPulling();

                    return InteractionResult.SUCCESS;
                }

                if (stack.isEmpty()) {
                    if (MultiBox.unbox(getBoundingBox()).inflate(0.5, 1, 0.5).contains(absHitPos)) {
                        return player.startRiding(this) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
                    }
                    AABB balloonBox = getBalloonBoundingBox(getBoundingBox());
                    if (balloonBox.inflate(0.5).setMinY(balloonBox.maxY - 0.25).contains(absHitPos)) {
                        return player.startRiding(this) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
                    }
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.has(UDataComponentTypes.BALLOON_DESIGN) && !hasBalloon()) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            playSound(USounds.ENTITY_HOT_AIR_BALLOON_EQUIP_CANOPY.value(), 1, 1);
            if (!player.isDiscrete()) {
                level().gameEvent(player, GameEvent.EQUIP, blockPosition());
            }
            setDesign(AirBalloonEntity.BalloonDesign.of(level(), stack));
            if (hasBurner() && hasBalloon()) {
                UCriteria.CONSTRUCT_BALLOON.trigger(player);
            }
            return InteractionResult.SUCCESS;
        }

        if (stack.is(ConventionalItemTags.SHEAR_TOOLS) && hasBalloon()) {
            stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
            if (level() instanceof ServerLevel sw) {
                spawnAtLocation(sw, BalloonDesignComponent.set(UItems.GIANT_BALLOON.getDefaultInstance(), new BalloonDesignComponent(getDesign(), true)));
            }
            setDesign(BalloonDesign.NONE);
            playSound(USounds.ENTITY_HOT_AIR_BALLOON_EQUIP_CANOPY.value(), 1, 1);
            if (!player.isDiscrete()) {
                level().gameEvent(player, GameEvent.EQUIP, blockPosition());
            }
            return InteractionResult.SUCCESS;
        }

        if ((stack.is(Items.LANTERN) || stack.is(Items.SOUL_LANTERN)) && !hasBurner()) {
            setItemInHand(InteractionHand.MAIN_HAND, stack.copyWithCount(1));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            playSound(USounds.ENTITY_HOT_AIR_BALLOON_EQUIP_BURNER, 0.2F, 1);
            if (!player.isDiscrete()) {
                level().gameEvent(player, GameEvent.EQUIP, blockPosition());
            }
            if (hasBurner() && hasBalloon()) {
                UCriteria.CONSTRUCT_BALLOON.trigger(player);
            }
            return InteractionResult.SUCCESS;
        }

        if (hasBurner() && level() instanceof ServerLevel sw) {
            int fuel = getBurnDuration(sw, stack);
            if (fuel > 0) {
                if (fuelItems.size() < 64) {
                    fuelItems.add(stack.consumeAndReturn(1, player));
                    burner.setPulling();
                    playSound(USounds.Vanilla.VILLAGER_YES, 1, 1);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void dropEquipment(ServerLevel world) {
        spawnAtLocation(world, getPickResult());
        if (world.getGameRules().get(GameRules.ENTITY_DROPS)) {
            ItemStack lantern = getItemInHand(InteractionHand.MAIN_HAND);
            setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            spawnAtLocation(world, lantern);
            if (hasBalloon()) {
                spawnAtLocation(world, BalloonDesignComponent.set(UItems.GIANT_BALLOON.getDefaultInstance(), new BalloonDesignComponent(getDesign(), true)));
                setDesign(BalloonDesign.NONE);
            }
            fuelItems.forEach(s -> spawnAtLocation(world, s));
            fuelItems.clear();
        }
    }

    @Override
    public ItemStack getPickResult() {
        return asItem().getDefaultInstance();
    }

    public Item asItem() {
        return Objects.requireNonNull(BasketItem.REGISTRY.get(getBasketType()), "BasketItem.REGISTRY.get(" + getBasketType() + ") != null");
    }

    @Override
    public void push(Entity entity) {
        if (entity instanceof AirBalloonEntity) {
            super.push(entity);
        }
    }

    @Override
    public void doPush(Entity entity) {
        if (entity instanceof AirBalloonEntity) {
            super.doPush(entity);
        }
    }

    @Override
    public SoundEvent getWalkedOnSound(double y) {
        if (y >= getBalloonBoundingBox(getBoundingBox()).minY) {
            return USounds.ENTITY_HOT_AIR_BALLOON_STEP;
        }
        return USounds.ENTITY_HOT_AIR_BALLOON_BASKET_STEP;
    }

    @Override
    public boolean collidesWithClouds() {
        return isAirworthy() && !isAscending();
    }


    @Override
    public float getCloudWalkingStrength() {
        return isAirworthy() ? 2 : 0;
    }

    @Override
    public void travel(Vec3 movementInput) {
        if (!isAirworthy()) {
            super.travel(movementInput);
        } else {
            final float speed = 0.02F;
            final float momentum = 0.91F;
            if (isLocalInstanceAuthoritative()) {
                if (isInWater() || isInLava()) {
                    moveRelative(speed, movementInput);
                    move(MoverType.SELF, getDeltaMovement());
                    setDeltaMovement(getDeltaMovement().scale(isInWater() ? 0.8 : 0.5));
                } else {
                    float slipperyness = (onGround() ? level().getBlockState(getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() : 1) * momentum;
                    float drag = onGround() ? 0.1F * (0.16277137F / (slipperyness * slipperyness * slipperyness)) : speed;
                    moveRelative(drag, movementInput);
                    move(MoverType.SELF, getDeltaMovement());
                    setDeltaMovement(getDeltaMovement().scale(slipperyness));
                }
            }
            calculateEntityAnimation(false);
        }

        if (isAirworthy()) {
            Map<AABB, List<Entity>> collidingEntities = getCollidingEntities(getBoundingBoxes(getBoundingBox()).stream());

            for (Map.Entry<AABB, List<Entity>> passengers : collidingEntities.entrySet()) {
                for (Entity passenger : passengers.getValue()) {
                    Living<?> living = Living.living(passenger);
                    if (living != null) {
                        living.getTransportation().setVehicle(this);
                    }

                }
            }
        }
    }

    @Override
    protected AABB makeBoundingBox(Vec3 pos) {
        AABB mainBox = super.makeBoundingBox(pos);
        return MultiBox.of(mainBox, getBoundingBoxes(mainBox), getInteractionZones(mainBox));
    }

    public AABB getInteriorBoundingBox(AABB mainBox) {
        AABB box = MultiBox.unbox(mainBox);
        return box.setMinY(box.minY - 0.5).deflate(0.15, 0, 0.15);
    }

    public AABB getBalloonBoundingBox(AABB mainBox) {
        float inflation = getInflation(1);
        return MultiBox.unbox(mainBox)
                .move(0.125, 7.3 * inflation, 0.125)
                .inflate(2.25, 3.7 * inflation, 2.25);
    }

    protected AABB getBurnerBoundingBox(AABB mainBox) {
        float inflation = getInflation(1);
        float horScale = -0.9F;
        return MultiBox.unbox(mainBox)
                .move(0, 2.6F * inflation + 0.4F, 0)
                .inflate(horScale, 0.4, horScale);
    }

    @Override
    public List<AABB> getGravityZoneBoxes() {
        AABB mainBox = getBoundingBox();
        AABB balloon = getBalloonBoundingBox(mainBox).inflate(0.001);
        AABB interior = getInteriorBoundingBox(mainBox).inflate(0.001);
        if (hasBalloon() && getInflation(1) > 0.999F) {
            return List.of(
                    // interior - basket to top of balloon
                    interior.setMaxY(interior.maxY + 0.1).setMinY(interior.maxY),
                    // balloon
                    balloon.setMaxY(balloon.maxY + 0.5).setMinY(balloon.maxY)
            );
        }
        return List.of(interior.setMaxY(interior.maxY + 0.1).setMinY(interior.maxY));
    }

    @Override
    public List<AABB> getBoundingBoxes(AABB mainBox) {
        mainBox = MultiBox.unbox(mainBox);
        List<AABB> boxes = new ArrayList<>();
        AABB box = getInteriorBoundingBox(mainBox);

        double wallheight = box.maxY + 0.72;
        double wallThickness = 0.2;
        double halfDoorWidth = 0.5;
        double balloonWallThickness = 0.25;

        if (!getBasketType().isOf(WoodType.BAMBOO)) {

            // front left (next to door)
            boxes.add(new AABB(mainBox.minX + wallThickness + 0.15, mainBox.maxY, mainBox.minZ, mainBox.minX + wallThickness + halfDoorWidth, wallheight, box.minZ + wallThickness));
            // front right (next to door)
            boxes.add(new AABB(mainBox.maxX - wallThickness - halfDoorWidth, mainBox.maxY, mainBox.minZ, mainBox.maxX - wallThickness - 0.15, wallheight, box.minZ + wallThickness));

            // back
            boxes.add(new AABB(mainBox.minX + wallThickness + 0.15, mainBox.maxY, box.maxZ - wallThickness, mainBox.maxX - wallThickness - 0.15, wallheight, mainBox.maxZ));

            // left
            boxes.add(new AABB(box.maxX - wallThickness, mainBox.maxY, mainBox.minZ, mainBox.maxX, wallheight, mainBox.maxZ));
            // right
            boxes.add(new AABB(mainBox.minX, mainBox.maxY, mainBox.minZ, box.minX + wallThickness, wallheight, mainBox.maxZ));
        }

        if (hasBalloon() && getInflation(1) >= 1) {
            AABB balloonBox = getBalloonBoundingBox(mainBox);
            boxes.add(balloonBox.setMinY(balloonBox.maxY - balloonWallThickness));
            boxes.add(balloonBox.setMaxX(balloonBox.minX + balloonWallThickness).setMaxY(balloonBox.maxY - balloonWallThickness).setMinZ(balloonBox.minZ + balloonWallThickness).setMaxZ(balloonBox.maxZ - balloonWallThickness));
            boxes.add(balloonBox.setMinX(balloonBox.maxX - balloonWallThickness).setMaxY(balloonBox.maxY - balloonWallThickness).setMinZ(balloonBox.minZ + balloonWallThickness).setMaxZ(balloonBox.maxZ - balloonWallThickness));
            boxes.add(balloonBox.setMaxZ(balloonBox.minZ + balloonWallThickness).setMaxY(balloonBox.maxY - balloonWallThickness));
            boxes.add(balloonBox.setMinZ(balloonBox.maxZ - balloonWallThickness).setMaxY(balloonBox.maxY - balloonWallThickness));
        }

        float yaw = (180 - getDirection().toYRot()) * Mth.DEG_TO_RAD;
        if (yaw != 0) {
            Vec3 center = position();
            for (int i = 0; i < boxes.size(); i++) {
                AABB b = boxes.get(i);
                Vec3 min = new Vec3(b.minX, b.minY, b.minZ).subtract(center).yRot(yaw).add(center);
                Vec3 max = new Vec3(b.maxX, b.maxY, b.maxZ).subtract(center).yRot(yaw).add(center);
                boxes.set(i, new AABB(min.x, min.y, min.z, max.x, max.y, max.z));
            }
        }

        return boxes;
    }

    private List<AABB> getInteractionZones(AABB mainBox) {
        List<AABB> boxes = new ArrayList<>();
        AABB box = getInteriorBoundingBox(mainBox);

        if (hasBalloon() && getInflation(1) >= 1) {
            double horScale = -0.8;
            double verScale = 0.2;
            double horOutset = 2.5;
            // x+ z+
            boxes.add(box.inflate(horScale, verScale, horScale).move(horOutset, 2.5, horOutset));
            // x- z+
            boxes.add(box.inflate(horScale, verScale, horScale).move(-horOutset, 2.5, horOutset));

            // x+ z-
            boxes.add(box.inflate(horScale, verScale, horScale).move(horOutset, 2.5, -horOutset));
            // x- z-
            boxes.add(box.inflate(horScale, verScale, horScale).move(-horOutset, 2.5, -horOutset));
        }
        if (hasBurner()) {
            boxes.add(getBurnerBoundingBox(mainBox));
        }

        return boxes;
    }

    @Override
    public void move(MoverType movementType, Vec3 movement) {
        Vec3 oldPos = this.position();
        List<AABB> boundingBoxes = getGravityZoneBoxes();
        super.move(movementType, movement);
        if (movementType == MoverType.SELF) {
            Vec3 actualMovement = position().subtract(oldPos);
            Map<AABB, List<Entity>> collidingEntities = getCollidingEntities(
                    boundingBoxes.stream().map(box -> box.expandTowards(actualMovement))
            );

            for (Map.Entry<AABB, List<Entity>> passengers : collidingEntities.entrySet()) {
                for (Entity passenger : passengers.getValue()) {
                    movePassenger(passenger, actualMovement);
                }
            }
        }
    }

    private void movePassenger(Entity passenger, Vec3 movement) {
        if (!EntitySelector.NO_SPECTATORS.test(passenger)) {
            return;
        }

        Living<?> living = Living.living(passenger);
        if (living != null) {
            if (living.getPhysics().isGravityNegative()) {
                movement = movement.multiply(1, -1, 1);
            }
            living.getTransportation().setVehicle(this);
        }

        List<VoxelShape> shapes = new ArrayList<>();
        getCollissionShapes(CollisionContext.of(passenger), shapes::add);
        movement = Entity.collideBoundingBox(passenger, movement, passenger.getBoundingBox(), level(), shapes);

        passenger.setPos(passenger.position().add(movement));
        passenger.syncPacketPositionCodec(passenger.getX(), passenger.getY(), passenger.getZ());
    }

    @Override
    public Map<AABB, List<Entity>> getCollidingEntities(Stream<AABB> boundingBoxes) {
        return boundingBoxes.collect(Collectors.toMap(Function.identity(), box -> {
            return level().getEntities(this, box.inflate(0.001).expandTowards(getDeltaMovement().scale(1)), RIDER_PREDICATE).stream().distinct().toList();
        }));
    }

    @Override
    public void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        setBasketType(BasketType.of(compound.getStringOr("basket", "")));
        setDesign(BalloonDesign.getType(compound.getStringOr("design", "")));
        setAscending(compound.getBooleanOr("burnerActive", false));
        setBoostTicks(compound.getIntOr("boostTicks", 0));
        prevInflation = compound.getIntOr("inflationAmount", 0);
        setInflation(prevInflation);
        activeFuel = Mth.clamp(compound.getIntOr("fuel", 0), 0, maxFuel);
        fuelItems = new ArrayList<>(compound.read("fuelItems", FUEL_CODEC).stream().flatMap(List::stream).toList());
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("design", getDesign().getSerializedName());
        compound.putString("basket", getBasketType().id().toString());
        compound.putBoolean("burnerActive", isAscending());
        compound.putInt("boostTicks", getBoostTicks());
        compound.putInt("inflationAmount", getInflation());
        compound.putInt("fuel", activeFuel);
        compound.store("fuelItems", FUEL_CODEC, fuelItems);
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (status >= 100 && status < 100 + sandbags.length) {
            getSandbag(status % sandbags.length).setPulling();
        } else if (status == STATUS_BURNER_INTERACT) {
        } else {
            super.handleEntityEvent(status);
        }
    }

    static boolean isBetween(double value, double min, double max) {
        return value >= min && value <= max;
    }

    public class Animatable {
        private final int id;
        private int pullTicks;
        private int prevPullTicks;
        private boolean pulling;

        public Animatable(int id) {
            this.id = id;
        }

        public void setPulling() {
            if (!level().isClientSide()) {
                level().broadcastEntityEvent(AirBalloonEntity.this, (byte)(100 + id));
            }
            pulling = true;
        }

        public float getPullProgress(float tickDelta) {
            return Mth.lerp(tickDelta, (float)prevPullTicks, pullTicks) / 6F;
        }

        public void tick() {
            prevPullTicks = pullTicks;
            if (pulling && pullTicks < 6) {
                pullTicks++;
            } else {
                pulling = false;
                if (pullTicks > 0) {
                    pullTicks--;
                }
            }
        }
    }

    public enum BalloonDesign implements StringRepresentable {
        NONE,
        LUNA,
        DAWN,
        EQUALITY,
        STORM,
        TALE;

        public static final BalloonDesign[] VALUES = values();
        public static final EnumCodec<BalloonDesign> CODEC = StringRepresentable.fromEnum(BalloonDesign::values);
        public static final StreamCodec<ByteBuf, BalloonDesign> PACKET_CODEC = PacketCodecUtils.ofEnum(BalloonDesign.class);

        private final String name = name().toLowerCase(Locale.ROOT);

        @Override
        public String getSerializedName() {
            return name;
        }

        public static AirBalloonEntity.BalloonDesign of(Level world, ItemStack stack) {
            AirBalloonEntity.BalloonDesign design = BalloonDesignComponent.get(stack).design();
            if (design == NONE) {
                return VALUES[1 + world.getRandom().nextInt(VALUES.length - 1)];
            }
            return design;
        }

        public static BalloonDesign getType(int type) {
            return VALUES[Math.abs(type) % VALUES.length];
        }

        @Deprecated
        public static BalloonDesign getType(String name) {
            return CODEC.byName(name, LUNA);
        }
    }

    public record BasketType(Identifier id, WoodType woodType) {
        private static final Map<Identifier, BasketType> REGISTRY = new HashMap<>();
        public static final BasketType DEFAULT = of(WoodType.OAK);

        public boolean isOf(WoodType woodType) {
            return this.woodType == woodType;
        }

        @Deprecated
        public static BasketType of(@Nullable String name) {
            Identifier id = name == null || name.isEmpty() ? null : Identifier.tryParse(name);
            if (id == null) {
                return of(WoodType.OAK);
            }
            return REGISTRY.get(id);
        }

        public static BasketType of(WoodType woodType) {
            return REGISTRY.computeIfAbsent(Identifier.parse(woodType.name()), id -> new BasketType(id, woodType));
        }

        @Deprecated
        public static BasketType of(ResourceKey<?> id) {
            return REGISTRY.computeIfAbsent(id.identifier(), i -> new BasketType(i, null));
        }
    }
}









