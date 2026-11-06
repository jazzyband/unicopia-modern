package com.minelittlepony.unicopia.projectile;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.SpellInventory;
import com.minelittlepony.unicopia.ability.magic.SpellInventory.Operation;
import com.minelittlepony.unicopia.ability.magic.SpellSlots;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.block.state.StatePredicate;
import com.minelittlepony.unicopia.entity.EntityPhysics;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.Physics;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MagicBeamEntity extends MagicProjectileEntity implements Caster<MagicBeamEntity>, MagicImmune {
    private static final EntityDataAccessor<Boolean> HYDROPHOBIC = SynchedEntityData.defineId(MagicBeamEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LEVEL = SynchedEntityData.defineId(MagicBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LEVEL = SynchedEntityData.defineId(MagicBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CORRUPTION = SynchedEntityData.defineId(MagicBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_CORRUPTION = SynchedEntityData.defineId(MagicBeamEntity.class, EntityDataSerializers.INT);

    private final SpellInventory spells = SpellSlots.ofSingle(this);
    private final EntityPhysics<MagicProjectileEntity> physics = new EntityPhysics<>(this);

    private final LevelStore level = Levelled.of(
            () -> entityData.get(LEVEL),
            l -> entityData.set(LEVEL, l),
            () -> entityData.get(MAX_LEVEL)
    );
    private final LevelStore corruption = Levelled.of(
            () -> entityData.get(CORRUPTION),
            l -> entityData.set(CORRUPTION, l),
            () -> entityData.get(MAX_CORRUPTION)
    );

    public MagicBeamEntity(EntityType<MagicBeamEntity> type, Level world) {
        super(type, world);
    }

    public MagicBeamEntity(Level world, Entity owner, float divergance, Spell spell) {
        super(UEntities.MAGIC_BEAM, world);
        setPos(owner.getX(), owner.getEyeY() - 0.1F, owner.getZ());
        setOwner(owner);
        shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0, 1.5F, divergance);
        setNoGravity(true);
        spell.apply(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HYDROPHOBIC, false)
            .define(LEVEL, 0)
            .define(CORRUPTION, 0)
            .define(MAX_LEVEL, 1)
            .define(MAX_CORRUPTION, 1);
    }

    @Override
    public void tick() {
        super.tick();

        if (getOwner() != null) {
            spells.tick(Situation.PROJECTILE);
        }

        if (getHydrophobic()) {
            if (StatePredicate.isFluid(level().getBlockState(blockPosition()))) {
                Vec3 vel = getDeltaMovement();

                double velY = vel.y;

                velY *= -1;

                if (!isNoGravity()) {
                    velY += 0.16;
                }

                setDeltaMovement(new Vec3(vel.x, velY, vel.z));
            }
        }
    }

    public void setHydrophobic() {
        getEntityData().set(HYDROPHOBIC, true);
    }

    public boolean getHydrophobic() {
        return getEntityData().get(HYDROPHOBIC);
    }

    @Override
    public MagicBeamEntity asEntity() {
        return this;
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        Caster.of(entity).ifPresent(caster -> {
            entityData.set(LEVEL, caster.getLevel().get());
            entityData.set(MAX_LEVEL, caster.getLevel().getMax());
            entityData.set(CORRUPTION, caster.getCorruption().get());
            entityData.set(MAX_CORRUPTION, caster.getCorruption().getMax());
        });
    }

    @Override
    public LevelStore getLevel() {
        return level;
    }

    @Override
    public LevelStore getCorruption() {
        return corruption;
    }

    @Override
    public Physics getPhysics() {
        return physics;
    }

    @Override
    public Affinity getAffinity() {
        return getSpellSlot().get().map(Affine::getAffinity).orElse(Affinity.NEUTRAL);
    }

    @Override
    public SpellSlots getSpellSlot() {
        return spells.getSlots();
    }

    @Override
    public boolean subtractEnergyCost(double amount) {
        return Caster.of(getMaster()).filter(c -> c.subtractEnergyCost(amount)).isPresent();
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        InteractionManager.getInstance().playLoopingSound(this, InteractionManager.SOUND_MAGIC_BEAM, getId());
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        getSpellSlot().clear();
    }

    @Override
    protected <T extends ProjectileDelegate> void forEachDelegates(Consumer<T> consumer, Function<Object, T> predicate) {
        spells.tick(spell -> {
            Optional.ofNullable(predicate.apply(spell)).ifPresent(consumer);
            return Operation.SKIP;
        });
        super.forEachDelegates(consumer, predicate);
    }

    @Override
    public void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        getEntityData().set(HYDROPHOBIC, compound.getBooleanOr("hydrophobic", false));
        physics.fromValue(compound);
        spells.getSlots().fromValue(compound, registryAccess());
        var level = compound.read("level", Levelled.CODEC).orElse(Levelled.ZERO);
        entityData.set(MAX_LEVEL, level.getMax());
        entityData.set(LEVEL, level.get());
        var corruption = compound.read("corruption", Levelled.CODEC).orElse(Levelled.ZERO);
        entityData.set(MAX_CORRUPTION, corruption.getMax());
        entityData.set(CORRUPTION, corruption.get());
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        compound.store("level", Levelled.CODEC, level);
        compound.store("corruption", Levelled.CODEC, corruption);
        compound.putBoolean("hydrophobic", getHydrophobic());
        physics.toValue(compound);
        spells.getSlots().toValue(compound, registryAccess());
    }
}
