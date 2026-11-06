package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.Optional;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.server.world.Altar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FloatingArtefactEntity extends StationaryObjectEntity {
    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(FloatingArtefactEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Byte> STATE = SynchedEntityData.defineId(FloatingArtefactEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> TARGET_ROTATION_SPEED = SynchedEntityData.defineId(FloatingArtefactEntity.class, EntityDataSerializers.FLOAT);

    private static final int REGEN_GAP_TICKS = 5;
    private static final int REGEN_PAUSE_TICKS = 200;

    public static final int INFINITE_BOOST_DURATION = -1;

    private float bobAmount;

    private float prevRotationSpeed;
    private float rotationSpeed;
    private float prevRotation;
    private float rotation;

    private int boostDuration;

    private int ticksUntilRegen;
    public final float positionSeed;

    private Optional<Altar> altar = Optional.empty();

    public FloatingArtefactEntity(EntityType<?> entityType, Level world) {
        super(entityType, world);

        positionSeed = (float)(Math.random() * Math.PI * 2);
    }

    @Override
    protected Component getTypeName() {
        return getStack().getHoverName();
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ITEM, ItemStack.EMPTY);
        builder.define(STATE, (byte)0);
        builder.define(TARGET_ROTATION_SPEED, 1F);
    }

    public void setAltar(Altar altar) {
        this.altar = Optional.of(altar);
    }

    public ItemStack getStack() {
        return entityData.get(ITEM);
    }

    public void setStack(ItemStack stack) {
        entityData.set(ITEM, stack);
    }

    public State getState() {
        return State.valueOf(entityData.get(STATE));
    }

    public void setState(State state) {
        entityData.set(STATE, (byte)state.ordinal());
    }

    public void setRotationSpeed(float spin, int duration) {
        entityData.set(TARGET_ROTATION_SPEED, Math.max(spin, 0));
        boostDuration = duration;
    }

    public float getRotationSpeed() {
        return entityData.get(TARGET_ROTATION_SPEED);
    }

    public float getRotationSpeed(float tickDelta) {
        return Mth.lerp(tickDelta, prevRotationSpeed, rotationSpeed);
    }

    @Override
    public float getMaxHealth() {
        return 20F;
    }

    @Override
    public void tick() {
        Vec3 pos = Vec3.atBottomCenterOf(blockPosition());

        setPosRaw(pos.x, pos.y, pos.z);

        super.tick();

        ItemStack stack = getStack();

        if (stack.isEmpty()) {
            setStack(UItems.EMPTY_JAR.getDefaultInstance());
        }

        if (level().isClientSide()) {
            bobAmount++;
        }

        float targetRotationSpeed = getRotationSpeed();

        if (rotationSpeed != targetRotationSpeed) {
            float difference = targetRotationSpeed - rotationSpeed;
            rotationSpeed = Math.abs(difference) < 0.02F ? targetRotationSpeed : (rotationSpeed + difference * (difference > 0 ? 0.5F : 0.1F));
        } else {
            if (boostDuration > 0 && --boostDuration <= 0) {
                setRotationSpeed(1, 0);
            }
        }

        rotation %= 360;
        prevRotation = rotation;
        rotation += rotationSpeed;

        if (stack.getItem() instanceof Artifact) {
            ((Artifact)stack.getItem()).onArtifactTick(this);
        }

        if (getHealth() < getMaxHealth() && --ticksUntilRegen <= 0) {
            setHealth(getHealth() + 1);
            ticksUntilRegen = REGEN_GAP_TICKS;
        }

        if (level().getGameTime() % 80 == 0) {
            State state = getState();
            playSound(USounds.ENTITY_ARTEFACT_AMBIENT, state.getVolume(), state.getPitch());
        }
    }

    public float getVerticalOffset(float tickDelta) {
        return Mth.sin((bobAmount + tickDelta) / 10F + positionSeed) * 0.025F + 0.05F;
    }

    public float getRotation(float tickDelta) {
        return Mth.lerp(tickDelta, prevRotation, rotation) % 360;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        compound.read("Item", ItemStack.CODEC).ifPresent(this::setStack);
        setState(State.valueOf(compound.getIntOr("State", 0)));
        setRotationSpeed(compound.getFloatOr("spin", 0), compound.getIntOr("spinDuration", 0));
        ticksUntilRegen = compound.getIntOr("regen", 0);
        altar = compound.read("altar", Altar.CODEC);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            compound.store("Item", ItemStack.CODEC, stack);
        }
        compound.putInt("State", getState().ordinal());
        compound.putFloat("spin", getRotationSpeed());
        compound.putInt("spinDuration", boostDuration);
        compound.putInt("regen", ticksUntilRegen);
        compound.storeNullable("altar", Altar.CODEC, altar.orElse(null));
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float damage) {

        if (level().isClientSide() || isInvulnerable()) {
            return false;
        }

        if (isInvulnerableToBase(source) || !getStack().canBeHurtBy(source)) {
            return false;
        }

        ticksUntilRegen = REGEN_PAUSE_TICKS;

        if (source.isCreativePlayer()) {
            damage = getHealth();
        }

        return super.hurtServer(world, source, damage);
    }

    @Override
    protected void onKilled(DamageSource source) {
        ItemStack stack = getStack();

        if (altar.isEmpty() && level() instanceof ServerLevel sw) {
            if (!(stack.getItem() instanceof Artifact) || ((Artifact)stack.getItem()).onArtifactDestroyed(this) != InteractionResult.SUCCESS) {
                if (!source.isCreativePlayer()) {
                    spawnAtLocation(sw, stack);
                }
            }
        }
    }

    @Override
    protected void onHurt() {
        playSound(USounds.ITEM_ICARUS_WINGS_WARN, 1, 1);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        altar.ifPresent(altar -> altar.tearDown(this, level()));
    }

    public enum State {
        INITIALISING,
        RUNNING,
        SHUTTING_DOWN;

        static final State[] VALUES = values();

        public float getVolume() {
            return this == SHUTTING_DOWN ? 1 : 0.2F;
        }

        public float getPitch() {
            return this == INITIALISING ? 1 : this == RUNNING ? 2 : 0.5F;
        }

        @Deprecated
        static State valueOf(int state) {
            return state <= 0 || state >= VALUES.length ? INITIALISING : VALUES[state];
        }
    }

    public interface Artifact {
        void onArtifactTick(FloatingArtefactEntity entity);

        InteractionResult onArtifactDestroyed(FloatingArtefactEntity entity);
    }
}
