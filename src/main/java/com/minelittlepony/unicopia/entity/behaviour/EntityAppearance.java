package com.minelittlepony.unicopia.entity.behaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.FlightType;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.compat.pehkui.PehkUtil;
import com.minelittlepony.unicopia.entity.collision.EntityCollisions;
import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.minelittlepony.unicopia.entity.mob.SombraEntity;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.entity.player.PlayerDimensions;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.track.TrackableObject;
import com.minelittlepony.unicopia.projectile.ProjectileUtil;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import com.mojang.authlib.GameProfile;

public class EntityAppearance implements NbtSerialisable, PlayerDimensions.Provider, FlightType.Provider, EntityCollisions.ComplexCollidable, TrackableObject<EntityAppearance> {
    @NotNull
    private transient String entityId = "";

    @Nullable
    private transient Entity entity;

    @Nullable
    private transient BlockEntity blockEntity;

    private transient List<Attachment> attachments = new ArrayList<>();

    private Optional<EntityDimensions> dimensions = Optional.empty();

    /**
     * Tag that allows behaviours to store data between ticks.
     * This is not serialized, so should only be used for server-side data.
     */
    @Nullable
    private transient CompoundTag tag;

    @Nullable
    private CompoundTag entityNbt;

    private boolean dirty;

    @Nullable
    public Entity getEntity() {
        return entity;
    }

    @Nullable
    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public record Attachment(Vec3 offset, Entity entity) {}

    public void setBlockEntity(@Nullable BlockEntity blockEntity) {
        if (this.blockEntity != null) {
            this.blockEntity.setRemoved();
        }
        this.blockEntity = blockEntity;
    }

    public void attachExtraEntity(Vec3 offset, Entity entity) {
        attachments.add(new Attachment(offset, entity));
    }

    public void setAppearance(@Nullable Entity entity) {
        remove();

        entityNbt = entity == null ? null : encodeEntityToNBT(entity);
        entityId = entityNbt == null ? "" : entityNbt.getString("id").orElse(null);
        markDirty();
    }

    public boolean isPresent() {
        return entity != null;
    }

    public boolean isOf(@Nullable Entity entity) {
        return isPresent() && entity != null && EntityBehaviour.forEntity(this.entity).isEqual(this.entity, entity);
    }

    public CompoundTag getOrCreateTag() {
        if (tag == null) {
            tag = new CompoundTag();
        }
        return tag;
    }

    public boolean hasTag() {
        return tag != null;
    }

    public void remove() {
        attachments.clear();
        if (entity != null) {
            EntityBehaviour.forEntity(entity).onDestroy(entity);
            entity = null;
        }
        if (blockEntity != null) {
            try {
                blockEntity.setRemoved();
            } catch (Throwable ignored) {}
            blockEntity = null;
        }
    }

    private synchronized void createPlayer(CompoundTag nbt, GameProfile profile, Caster<?> source) {
        remove();

        entity = InteractionManager.getInstance().createPlayer(source.asEntity(), profile);
        entity.setCustomName(source.asEntity().getName());
        entity.load(TagValueInput.create(ProblemReporter.DISCARDING, source.asWorld().registryAccess(), nbt.getCompoundOrEmpty("playerNbt")));
        nbt.getByte("playerVisibleParts").ifPresent(visibilityByte -> {
            entity.getEntityData().set(Disguise.PlayerAccess.getModelBitFlag(), visibilityByte);
        });
        entity.setUUID(UUID.randomUUID());
        entity.clearFire();

        onEntityLoaded(source);
    }

    public Entity getOrCreate(Caster<?> source) {
        if (entity == null && entityNbt != null) {
            CompoundTag nbt = entityNbt;
            entity = null;
            entityNbt = null;
            attachments.clear();

            if ("player".equals(entityId)) {
                String name = nbt.getString("playerName").orElse(null);
                createPlayer(nbt, new GameProfile(
                    nbt.read("playerId", UUIDUtil.AUTHLIB_CODEC).orElse(UUID.randomUUID()),
                    name
                ), source);

                if (name != null && source.asWorld().getServer() != null) {
                    ResolvableProfile.createUnresolved(name)
                            .resolveProfile(source.asWorld().getServer().services().profileResolver())
                            .thenAccept(profile -> createPlayer(nbt, profile, source));
                }
            } else {
                if (source.isClient()) {
                    var input = TagValueInput.create(ProblemReporter.DISCARDING, source.asWorld().registryAccess(), nbt);
                    entity = EntityType.by(input).map(type -> type.create(source.asWorld(), EntitySpawnReason.CONVERSION)).orElse(null);
                    if (entity != null) {
                        try {
                            entity.load(TagValueInput.create(ProblemReporter.DISCARDING, source.asWorld().registryAccess(), nbt));
                        } catch (Exception ignored) {
                            // Mojang pls
                        }
                        entity = EntityBehaviour.forEntity(entity).onCreate(entity, this, true);
                    }
                } else {
                    entity = EntityType.loadEntityRecursive(TagValueInput.create(ProblemReporter.DISCARDING, source.asWorld().registryAccess(), nbt), source.asWorld(), EntitySpawnReason.CONVERSION, e -> {
                        return EntityBehaviour.forEntity(e).onCreate(e, this, true);
                    });
                }

                onEntityLoaded(source);
            }
        }

        return entity;
    }

    public void onImpact(Caster<?> pony, double distance, float damageMultiplier, DamageSource cause) {
        EntityBehaviour.forEntity(entity).onImpact(pony, entity, distance, damageMultiplier, cause);
    }

    private void onEntityLoaded(Caster<?> source) {
        source.asEntity().refreshDimensions();

        if (entity == null) {
            return;
        }

        if (entity instanceof LivingEntity l) {
            l.getAttribute(UEntityAttributes.ENTITY_GRAVITY_MODIFIER).removeModifiers();
        }

        if (entity instanceof Guest guest) {
            guest.setHost(source);
        }

        if (source.isClient()) {
            source.asWorld().addFreshEntity(entity);
        } else {
            entity.setId(source.asEntity().getId());
        }
    }

    @Override
    public FlightType getFlightType() {
        if (!isPresent()) {
            return FlightType.UNSET;
        }

        if (entity == null) {
            return FlightType.NONE;
        }

        if (entity instanceof Owned) {
            @SuppressWarnings("unchecked")
            Pony iplayer = Pony.of(((Owned<Player>)entity).getMaster());

            return iplayer == null ? FlightType.NONE : iplayer.getSpecies().flightType();
        }

        if (entity instanceof Mob mob && mob.getNavigation() instanceof FlyingPathNavigation
                || entity instanceof AmbientCreature
                || entity instanceof EnderDragon
                || entity instanceof Vex
                || entity instanceof Allay
                || entity instanceof Bat
                || entity instanceof ButterflyEntity
                || entity instanceof ShulkerBullet
                || entity instanceof SombraEntity
                || ProjectileUtil.isFlyingProjectile(entity)) {
            return FlightType.INSECTOID;
        }

        return FlightType.NONE;
    }

    public float getHeight() {
        if (entity != null) {
            if (entity instanceof FallingBlockEntity) {
                return 0.9F;
            }

            return PehkUtil.ignoreScaleFor(entity, Entity::getBbHeight) - 0.1F;
        }
        return -1;
    }

    public Optional<Float> getDistance(Pony player) {
        return PehkUtil.ignoreScaleFor(entity, e -> EntityBehaviour.forEntity(e).getCameraDistance(e, player));
    }

    @Override
    public Optional<EntityDimensions> getTargetDimensions(Pony player) {
        return dimensions = PehkUtil.ignoreScaleFor(entity, e -> EntityBehaviour.forEntity(e).getDimensions(e, dimensions));
    }

    public boolean skipsUpdate() {
        return entity instanceof FallingBlockEntity
            || entity instanceof HangingEntity
            || entity instanceof Player
            || entity instanceof HangingEntity;
    }

    public boolean isAxisAligned() {
        return isAxisAligned(entity);
    }

    public boolean canClimbWalls() {
        return entity instanceof Spider;
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        compound.putString("entityId", entityId);
        var nbt = getEntityNbt();
        if (nbt != null) {
            compound.put("entity", nbt);
        }
    }

    @Nullable
    private CompoundTag getEntityNbt() {
        if (entityNbt == null && entity != null) {
            entityNbt = encodeEntityToNBT(entity);
        }
        return entityNbt;
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        String newId = compound.getString("entityId").orElse("");
        loadEntityFromNbt(newId, compound.getCompound("entity").orElse(null));
    }

    private void loadEntityFromNbt(String newId, @Nullable CompoundTag newNbt) {
        String newPlayerName = newNbt == null ? null : newNbt.getString("playerName").orElse(null);
        String oldPlayerName = entity instanceof Player player ? player.getGameProfile().name() : null;

        if (!Objects.equals(newId, entityId) || !Objects.equals(newPlayerName, oldPlayerName)) {
            entityNbt = null;
            remove();
        }

        entityId = newId;
        entityNbt = newNbt;

        if (entityNbt != null) {
            if (entity != null) {
                try {
                    entity.load(TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), entityNbt));
                } catch (Exception ignored) {
                    // Mojang pls
                }

                attachments.clear();
                // onCreate re-populates the attachments
                entity = EntityBehaviour.forEntity(entity).onCreate(entity, this, false);
            }
        }
    }

    public static boolean isAxisAligned(@Nullable Entity entity) {
        return entity instanceof Shulker
            || entity instanceof HangingEntity
            || entity instanceof FallingBlockEntity;
    }

    private static CompoundTag encodeEntityToNBT(Entity entity) {
        CompoundTag entityNbt = new CompoundTag();

        if (entity instanceof Player player) {
            GameProfile profile = player.getGameProfile();

            entityNbt.putString("id", "player");
            entityNbt.storeNullable("playerId", UUIDUtil.AUTHLIB_CODEC, profile.id());
            entityNbt.putString("playerName", profile.name());
            entityNbt.putByte("playerVisibleParts", player.getEntityData().get(Disguise.PlayerAccess.getModelBitFlag()));

            return NbtSerialisable.subTag("playerNbt", entityNbt, playerNbt -> {
                var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
                player.saveWithoutId(output);
                playerNbt.merge(output.buildResult());
                playerNbt.remove("unicopia_caster");
                Pony pony = Pony.of(player);
                if (pony != null) {
                    NbtSerialisable.subTag("unicopia_caster", playerNbt, comp -> pony.toSyncronisedNbt(comp, pony.asEntity().registryAccess()));
                }
            });
        }

        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
        entity.saveAsPassenger(output);
        entityNbt.merge(output.buildResult());
        entityNbt.remove("unicopia_caster");

        return entityNbt;
    }

    @Override
    public void getCollissionShapes(CollisionContext context, Consumer<VoxelShape> output) {
        EntityCollisions.getCollissionShapes(getEntity(), context, output);
        getAttachments().forEach(e -> EntityCollisions.getCollissionShapes(e.entity(), context, output));
    }

    public void markDirty() {
        dirty = true;
    }

    @Override
    public Status getStatus() {
        if (dirty) {
            dirty = false;
            return Status.UPDATED;
        }
        return Status.DEFAULT;
    }

    @Override
    public void read(RegistryFriendlyByteBuf buffer) {
        loadEntityFromNbt(buffer.readUtf(), buffer.readNullable(ByteBufCodecs.COMPOUND_TAG));
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(entityId);
        buffer.writeNullable(getEntityNbt(), ByteBufCodecs.COMPOUND_TAG);
    }

    @Override
    public void discard(boolean immediate) {
        setAppearance(null);
        dirty = false;
    }

    @Override
    public void copyTo(EntityAppearance destination) {
        destination.entityId = entityId;
        destination.entity = entity;
        destination.blockEntity = blockEntity;
        destination.attachments = new ArrayList<>(attachments);
        destination.dimensions = dimensions;
        destination.tag = tag == null ? null : tag.copy();
        destination.entityNbt = entityNbt == null ? null : entityNbt.copy();
    }
}
