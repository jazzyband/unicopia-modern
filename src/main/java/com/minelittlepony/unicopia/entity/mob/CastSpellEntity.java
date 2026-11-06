package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.UUID;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.SpellInventory;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.SpellSlots;
import com.minelittlepony.unicopia.ability.magic.spell.PlacementControlSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.PlacementControlSpell.PlacementDelegate;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.EntityPhysics;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.Physics;
import com.minelittlepony.unicopia.network.track.Trackable;
import com.minelittlepony.unicopia.server.world.Ether;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;

public class CastSpellEntity extends LightEmittingEntity implements Caster<CastSpellEntity>, WeaklyOwned.Mutable<LivingEntity>, MagicImmune {
    private static final EntityDataAccessor<Integer> LEVEL = SynchedEntityData.defineId(CastSpellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LEVEL = SynchedEntityData.defineId(CastSpellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CORRUPTION = SynchedEntityData.defineId(CastSpellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_CORRUPTION = SynchedEntityData.defineId(CastSpellEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> DEAD = SynchedEntityData.defineId(CastSpellEntity.class, EntityDataSerializers.BOOLEAN);

    private final EntityPhysics<CastSpellEntity> physics = new EntityPhysics<>(this);

    private final SpellInventory spells = SpellSlots.ofSingle(this);

    private final EntityReference<LivingEntity> owner = new EntityReference<>();

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

    private UUID controllingEntityUuid;
    private UUID controllingSpellUuid;

    private int prevAge;

    private int prevDeathTicks;
    private int deathTicks;

    public CastSpellEntity(Level world, Caster<?> caster, PlacementControlSpell control) {
        this(UEntities.CAST_SPELL, world);
        this.controllingEntityUuid = caster.asEntity().getUUID();
        this.controllingSpellUuid = control.getUuid();
        setCaster(caster);

        if (control.getDelegate() instanceof PlacementDelegate delegate) {
            delegate.onPlaced(caster, control);
        }

        spells.getSlots().put(Spell.copy(control.getDelegate(), world.registryAccess()));
    }

    public CastSpellEntity(EntityType<?> type, Level world) {
        super(type, world);
        Trackable.of(this).getDataTrackers().getPrimaryTracker().startTracking(owner);
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        builder.define(LEVEL, 0);
        builder.define(CORRUPTION, 0);
        builder.define(MAX_LEVEL, 1);
        builder.define(MAX_CORRUPTION, 1);
        builder.define(DEAD, false);
    }

    @Override
    public void absSnapTo(double x, double y, double z, float yaw, float pitch) {
        super.absSnapTo(x, y, z, yaw, pitch);
        spells.getSlots().stream(SpellPredicate.IS_ORIENTED).forEach(spell -> spell.setOrientation(this, pitch, yaw));
    }

    private boolean checkConnection() {
        return Ether.get(level()).get(SpellType.PLACE_CONTROL_SPELL, controllingEntityUuid, controllingSpellUuid) != null;
    }

    public float getAge(float tickDelta) {
        return Mth.lerpInt(tickDelta, prevAge, tickCount);
    }

    public float getScale(float tickDelta) {
        float add = Mth.clamp(getAge(tickDelta) / 25F, 0, 1);
        float subtract = Mth.clamp(Mth.lerpInt(tickDelta, prevDeathTicks, deathTicks) / 20F, 0, 1);
        return Mth.clamp(add - subtract, 0, 1);
    }

    @Override
    public int getLightLevel() {
        return 11;
    }

    @Override
    public Component getName() {
        Entity master = getMaster();
        if (master != null) {
            return Component.translatable("entity.unicopia.cast_spell.by", master.getName());
        }
        return super.getName();
    }

    @Override
    public void baseTick() {
        prevAge = tickCount;
        tickCount++;

        super.baseTick();

        if (!isClient()) {
            if (!checkConnection()) {
                kill((ServerLevel)level());
            }

            spells.getSlots().get().ifPresent(spell -> {
                Ether.get(level()).getOrCreate(spell, this);
            });
        } else if (isDead()) {
            spells.getSlots().clear();
        }

        prevDeathTicks = deathTicks;

        if (!spells.tick(Situation.GROUND) && deathTicks++ > 40) {
            remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        return true;
    }

    @Override
    public void kill(ServerLevel world) {
        setDead(true);
    }

    public boolean isDead() {
        return entityData.get(DEAD);
    }

    public void setDead(boolean dead) {
        entityData.set(DEAD, dead);
        if (dead) {
            spells.getSlots().clear();
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(getScale(1));
    }

    @Override
    public EntityReference<LivingEntity> getMasterReference() {
        return owner;
    }

    @Override
    public CastSpellEntity asEntity() {
        return this;
    }

    public void setCaster(Caster<?> caster) {
        entityData.set(LEVEL, caster.getLevel().get());
        entityData.set(MAX_LEVEL, caster.getLevel().getMax());
        entityData.set(CORRUPTION, caster.getCorruption().get());
        entityData.set(MAX_CORRUPTION, caster.getCorruption().getMax());
        setMaster(caster);
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
    public Affinity getAffinity() {
        return getSpellSlot().get().map(Spell::getAffinity).orElse(Affinity.NEUTRAL);
    }

    @Override
    public Physics getPhysics() {
        return physics;
    }

    @Override
    public SpellSlots getSpellSlot() {
        return spells.getSlots();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean subtractEnergyCost(double amount) {
        if (getMaster() == null) {
            return true;
        }

        return Caster.of(getMaster())
                .filter(c -> c.subtractEnergyCost(amount))
                .isPresent();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        tag.store("level", Levelled.CODEC, level);
        tag.store("corruption", Levelled.CODEC, corruption);

        tag.storeNullable("owningEntity", UUIDUtil.AUTHLIB_CODEC, controllingEntityUuid);
        tag.storeNullable("owningSpell", UUIDUtil.AUTHLIB_CODEC, controllingSpellUuid);

        spells.getSlots().toValue(tag, registryAccess());
        tag.putInt("age", tickCount);
        tag.putInt("prevAge", prevAge);
        tag.putBoolean("dead", isDead());
        tag.store("owner", CompoundTag.CODEC, owner.toNBT(registryAccess()));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        var level = tag.read("level", Levelled.CODEC).orElse(Levelled.ZERO);
        entityData.set(MAX_LEVEL, level.getMax());
        entityData.set(LEVEL, level.get());
        var corruption = tag.read("corruption", Levelled.CODEC).orElse(Levelled.ZERO);
        entityData.set(MAX_CORRUPTION, corruption.getMax());
        entityData.set(CORRUPTION, corruption.get());

        controllingEntityUuid = tag.read("owningEntity", UUIDUtil.AUTHLIB_CODEC).orElse(null);
        controllingSpellUuid = tag.read("owningSpell", UUIDUtil.AUTHLIB_CODEC).orElse(null);

        spells.getSlots().fromValue(tag, registryAccess());
        tickCount = tag.getIntOr("age", 0);
        prevAge = tag.getIntOr("prevAge", 0);
        setDead(tag.getBooleanOr("dead", false));

        if (tag.contains("owner")) {
            owner.fromNBT(tag.read("owner", CompoundTag.CODEC).orElseGet(CompoundTag::new), registryAccess());
        }
    }
}
