package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.item.ItemDuck;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.DataTrackerManager;
import com.minelittlepony.unicopia.network.track.Trackable;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.VecHelper;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;

public class ItemImpl implements Equine<ItemEntity> {
    private final ItemEntity entity;

    private final ItemPhysics physics;

    private final DataTrackerManager trackers;
    protected final DataTracker tracker;

    private final DataTracker.Entry<Race> race;

    public ItemImpl(ItemEntity owner) {
        this.entity = owner;
        this.trackers = Trackable.of(entity).getDataTrackers();
        this.tracker = trackers.getPrimaryTracker();
        this.physics = new ItemPhysics(owner);

        race = tracker.startTracking(TrackableDataType.RACE, Race.HUMAN);
    }

    @Override
    public boolean onProjectileImpact(Projectile projectile) {
        return false;
    }

    @Override
    public boolean beforeUpdate() {

        if (!entity.level().isClientSide()) {
            if (EnchantmentUtil.getWantItNeedItLevel(entity) > 0) {
                var random = entity.level().getRandom();

                if (random.nextInt(15) == 0) {
                    ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, entity.position().add(
                            VecHelper.sphere(random).get().add(0, 1, 0)
                    ), 0.2F), entity, 1);
                }
            }
        }

        ItemStack stack = entity.getItem();
        IItemEntity i = (IItemEntity)entity;
        ItemDuck duck = ItemDuck.of(stack);

        if (!stack.isEmpty()) {

            if (duck.isClingy(stack)) {
                RandomSource rng = entity.level().getRandom();

                entity.level().addParticle(duck.getParticleEffect(i),
                        entity.getX() + rng.nextFloat() - 0.5,
                        entity.getY() + rng.nextFloat() - 0.5,
                        entity.getZ() + rng.nextFloat() - 0.5,
                        0, 0, 0
                );

                Vec3 position = entity.position();
                VecHelper.findInRange(entity, entity.level(), entity.position(), duck.getFollowDistance(i), e -> e instanceof Player)
                    .stream()
                    .sorted((a, b) -> (int)(a.position().distanceTo(position) - b.position().distanceTo(position)))
                    .findFirst()
                    .ifPresent(player -> {
                        double distance = player.position().distanceTo(entity.position());

                        entity.move(MoverType.SELF,  player.position().subtract(entity.position()).scale(distance < 0.3 ? 1 : duck.getFollowSpeed(i)));
                        if (entity.horizontalCollision) {
                            entity.move(MoverType.SELF, new Vec3(0, entity.verticalCollision ? -0.3 : 0.3, 0));
                        }

                        duck.interactWithPlayer(i, (Player)player);
                    });
            }

            if (stack.is(UTags.Items.FALLS_SLOWLY)) {
                if (!entity.onGround() && Math.signum(entity.getDeltaMovement().y) != getPhysics().getGravitySignum()) {
                    double ticks = ((Entity)entity).tickCount;
                    double shift = Math.sin(ticks / 9D) / 9D;
                    double rise = -Math.cos(ticks / 9D) * getPhysics().getGravitySignum();

                    entity.yRotO = entity.yRotO;
                    entity.setYRot(entity.getYRot() + 0.3F);

                    entity.setDeltaMovement(
                            entity.getDeltaMovement()
                                .multiply(0.25, 0, 0.25)
                                .add(0, rise, 0)
                                .add(entity.getViewVector(1)).normalize().scale(shift)
                    );
                }
            }

            return duck.onGroundTick(i).consumesAction();
        }


        return false;
    }

    @Override
    public void tick() {
        physics.tick();
    }

    @Override
    public Physics getPhysics() {
        return physics;
    }

    @Override
    public Race getSpecies() {
        return race.get();
    }

    @Override
    public void setSpecies(Race race) {
        this.race.set(race);
    }

    @Override
    public boolean collidesWithClouds() {
        return entity.getItem().is(UTags.Items.FLOATS_ON_CLOUDS) || getSpecies().hasPersistentWeatherMagic();
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        compound.putString("owner_race", getSpecies().getId().toString());
        physics.toNBT(compound, lookup);
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        setSpecies(compound.read("owner_race", Race.REGISTRY.byNameCodec()).orElse(Race.HUMAN));
        physics.fromNBT(compound, lookup);
    }

    @Override
    public ItemEntity asEntity() {
        return entity;
    }

    public interface ClingyItem {
        default boolean isClingy(ItemStack stack) {
            return EnchantmentUtil.getLevel(UEnchantments.CLINGY, stack) > 0;
        }

        default ParticleOptions getParticleEffect(IItemEntity entity) {
            // TODO: was AMBIENT_ENTITY_EFFECT
            return net.minecraft.core.particles.SpellParticleOption.create(ParticleTypes.EFFECT, 0xFFFFFF, 1);
        }

        default float getFollowDistance(IItemEntity entity) {
            return 6 * (1 + EnchantmentUtil.getLevel(UEnchantments.CLINGY, entity.get().asEntity().getItem()));
        }

        default float getFollowSpeed(IItemEntity entity) {
            return Math.min(1, 0.02F * (1 + EnchantmentUtil.getLevel(UEnchantments.CLINGY, entity.get().asEntity().getItem())));
        }

        default void interactWithPlayer(IItemEntity entity, Player player) {

        }
    }
}
