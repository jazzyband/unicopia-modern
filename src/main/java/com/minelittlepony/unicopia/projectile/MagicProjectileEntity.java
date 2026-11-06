package com.minelittlepony.unicopia.projectile;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.WeaklyOwned;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.item.UItems;

/**
 * A generalised version of Mojang's projectile entity class with added support for a custom appearance and water phobia.
 */
public class MagicProjectileEntity extends ThrowableItemProjectile implements WeaklyOwned.Mutable<LivingEntity> {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(MagicProjectileEntity.class, EntityDataSerializers.FLOAT);

    public static final byte PROJECTILE_COLLISSION = 3;

    private final EntityReference<Entity> homingTarget = new EntityReference<>();
    private EntityReference<LivingEntity> owner;

    private int maxAge = 90;

    public MagicProjectileEntity(EntityType<? extends MagicProjectileEntity> type, Level world) {
        super(type, world);
    }

    public MagicProjectileEntity(Level world) {
        this(UEntities.THROWN_ITEM, world);
    }

    public MagicProjectileEntity(Level world, LivingEntity thrower) {
        super(UEntities.THROWN_ITEM, thrower, world, UItems.GEMSTONE.getDefaultInstance());
    }

    protected MagicProjectileEntity(EntityType<? extends MagicProjectileEntity> type, Level world, LivingEntity thrower) {
        super(type, thrower, world, UItems.GEMSTONE.getDefaultInstance());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DAMAGE, 0F);
    }

    @Override
    public Level asWorld() {
        return level();
    }

    @Override
    protected Item getDefaultItem() {
        return UItems.GEMSTONE;
    }

    @Override
    public final void setMaster(LivingEntity owner) {
        setOwner(owner);
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        if (entity instanceof LivingEntity l) {
            WeaklyOwned.Mutable.super.setMaster(l);
        }
    }

    @Override
    @Nullable
    public final Entity getOwner() {
        return getMaster();
    }

    @Override
    public EntityReference<LivingEntity> getMasterReference() {
        if (owner == null) {
            owner = new EntityReference<>();
        }
        return owner;
    }

    public void setHomingTarget(@Nullable Entity target) {
        homingTarget.set(target);
    }

    public void addThrowDamage(float damage) {
        setThrowDamage(getThrowDamage() + damage);
    }

    public void setThrowDamage(float damage) {
        getEntityData().set(DAMAGE, Math.max(0, damage));
    }

    public float getThrowDamage() {
        return getEntityData().get(DAMAGE);
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public void tick() {
        if (maxAge > 0 && !level().isClientSide() && homingTarget.getOrEmpty(asWorld()).isEmpty() && (getDeltaMovement().length() < 0.1 || tickCount > maxAge)) {
            discard();
        }

        super.tick();

        homingTarget.ifPresent(level(), e -> {
            setNoGravity(true);
            noPhysics = true;
            setDeltaMovement(getDeltaMovement().add(e.position().subtract(position()).normalize().scale(0.2)).multiply(0.6, 0.6, 0.6));
        });
    }

    private ParticleOptions getParticleParameters() {
       ItemStack stack = getItem();

       if (stack.isEmpty()) {
           return ParticleTypes.ITEM_SNOWBALL;
       }

       if (stack.is(UItems.FILLED_JAR)) {
           stack = UItems.EMPTY_JAR.getDefaultInstance();
       }

       return new ItemParticleOption(ParticleTypes.ITEM, net.minecraft.world.item.ItemStackTemplate.fromStack(stack));
    }

    @Override
    public void handleEntityEvent(byte id) {
       if (id == PROJECTILE_COLLISSION) {
          ParticleOptions effect = getParticleParameters();

          for(int i = 0; i < 8; i++) {
             level().addParticle(effect, getX(), getY(), getZ(), 0, 0, 0);
          }
       } else {
           super.handleEntityEvent(id);
       }
    }

    @Override
    public void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        homingTarget.fromNBT(compound.read("homingTarget", net.minecraft.nbt.CompoundTag.CODEC).orElseGet(net.minecraft.nbt.CompoundTag::new), registryAccess());
        getMasterReference().fromNBT(compound.read("owner", net.minecraft.nbt.CompoundTag.CODEC).orElseGet(net.minecraft.nbt.CompoundTag::new), registryAccess());
        maxAge = compound.getInt("maxAge").orElse(maxAge);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        compound.store("homingTarget", net.minecraft.nbt.CompoundTag.CODEC, homingTarget.toNBT(registryAccess()));
        compound.store("owner", net.minecraft.nbt.CompoundTag.CODEC, getMasterReference().toNBT(registryAccess()));
        compound.putInt("maxAge", maxAge);
    }

    @Override
    protected void onHit(HitResult result) {
        if (!isRemoved()) {
            super.onHit(result);

            if (!level().isClientSide()) {
                level().broadcastEntityEvent(this, PROJECTILE_COLLISSION);
                discard();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);

        forEachDelegates(effect -> effect.onImpact(this, hit), ProjectileDelegate.BlockHitListener.PREDICATE);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        Entity entity = hit.getEntity();

        if (!(entity instanceof FallingBlockEntity) && EquinePredicates.IS_MAGIC_IMMUNE.test(entity)) {
            return;
        }

        if (entity != null) {
            float damage = getThrowDamage();

            if (damage > 0 && !level().isClientSide()) {
                entity.hurtServer((ServerLevel)level(), damageSources().thrown(this, getOwner()), getThrowDamage());
            }

            forEachDelegates(effect -> effect.onImpact(this, hit), ProjectileDelegate.EntityHitListener.PREDICATE);
        }
    }

    public void knockback(LivingEntity target, DamageSource source, ItemStack weapon) {
        double d = weapon != null && level() instanceof ServerLevel serverWorld ? EnchantmentHelper.modifyKnockback(serverWorld, weapon, target, source, 0) : 0;
        if (d > 0) {
            double e = Math.max(0, 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            Vec3 vec3d = this.getDeltaMovement().multiply(1, 0, 1).normalize().scale(d * 0.6 * e);
            if (vec3d.lengthSqr() > 0) {
                target.push(vec3d.x, 0.1, vec3d.z);
            }
        }
    }

    protected <T extends ProjectileDelegate> void forEachDelegates(Consumer<T> consumer, Function<Object, T> predicate) {
        try {
            Optional.ofNullable(predicate.apply(getItem().getItem())).ifPresent(consumer);
        } catch (Throwable t) {
            Unicopia.LOGGER.error("Error whilst ticking spell on entity {}", getMasterReference(), t);
        }
    }
}
