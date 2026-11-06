package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.AltarRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.AltarRecipeMatch;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSpellbookStateChanged;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.minelittlepony.unicopia.server.world.Altar;
import com.minelittlepony.unicopia.util.MeteorlogicalUtil;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.damagesource.DamageTypes;

public class SpellbookEntity extends Mob implements MagicImmune {
    private static final EntityDataAccessor<Byte> LOCKED = SynchedEntityData.defineId(SpellbookEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> ALTERED = SynchedEntityData.defineId(SpellbookEntity.class, EntityDataSerializers.BOOLEAN);
    private static final byte ALTAR_BEAMS_START = 61;
    private static final byte ALTAR_BEAMS_END = 62;

    private static final int TICKS_TO_SLEEP = 600;

    private int activeTicks = TICKS_TO_SLEEP;
    private boolean prevDaytime;

    private SpellbookState state = new SpellbookState();

    private Optional<Altar> altar = Optional.empty();

    private boolean hasBeams;
    private int beamsActive;

    @Nullable
    private AltarRecipeMatch activeRecipe;

    public SpellbookEntity(EntityType<SpellbookEntity> type, Level world) {
        super(type, world);
        setPersistenceRequired();
        setAltered(world.getRandom().nextInt(3) == 0);
        state.setCurrentPageId(SpellbookState.CRAFTING_ID);
        if (!world.isClientSide()) {
            state.setSynchronizer(state -> {
                level().players().forEach(player -> {
                    if (player instanceof ServerPlayer recipient
                            && player.containerMenu instanceof SpellbookScreenHandler book
                            && getUUID().equals(book.entityId)) {
                        Channel.SERVER_SPELLBOOK_UPDATE.sendToPlayer(MsgSpellbookStateChanged.create(book, state), recipient);
                    }
                });
            });
        }
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LOCKED, (byte)1);
        builder.define(ALTERED, false);
    }

    public SpellbookState getSpellbookState() {
        return state;
    }

    public void setSpellbookState(SpellbookState state) {
        state.copySyncronizer(this.state);
        this.state = state;
    }

    @Override
    public ItemStack getPickResult() {
        ItemStack stack = UItems.SPELLBOOK.getDefaultInstance();
        stack.set(UDataComponentTypes.SPELLBOOK_STATE, state.createCopy());
        return stack;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    public void setAltar(Altar altar) {
        this.altar = Optional.of(altar);
    }

    public Optional<Altar> getAltar() {
        return altar;
    }

    public boolean hasBeams() {
        return hasBeams && altar.isPresent();
    }

    public boolean isAltered() {
        return entityData.get(ALTERED);
    }

    public void setAltered(boolean altered) {
        entityData.set(ALTERED, altered);
    }

    protected void setForcedState(TriState state) {
        entityData.set(LOCKED, (byte)state.ordinal());
    }

    public void clearForcedState() {
        setForcedState(TriState.DEFAULT);
        activeTicks = 0;
    }

    private TriState getForcedState() {
        if (activeTicks <= 0) {
            setForcedState(TriState.DEFAULT);
        }
        return TriState.values()[Math.abs(entityData.get(LOCKED)) % 3];
    }

    public boolean isOpen() {
        return getForcedState().orElse(!shouldBeSleeping());
    }

    public void keepAwake() {
        activeTicks = 100;
    }

    @Override
    public void tick() {
        boolean open = isOpen();
        super.tick();

        if (open && isInWater()) {
            push(0, 0.01, 0);
            keepAwake();
        }

        if (activeTicks > 0) {
            activeTicks--;
        }

        if (level().isClientSide() && open) {
            for (int offX = -2; offX <= 1; ++offX) {
                for (int offZ = -2; offZ <= 1; ++offZ) {
                    if (offX > -1 && offX < 1 && offZ == -1) {
                        offZ = 1;
                    }

                    if (random.nextInt(320) == 0) {
                        for (int offY = 0; offY <= 1; ++offY) {
                            level().addParticle(ParticleTypes.ENCHANT,
                                    getX(), getY(), getZ(),
                                    offX/2F + random.nextFloat(),
                                    offY/2F - random.nextFloat() + 0.5f,
                                    offZ/2F + random.nextFloat()
                            );
                        }
                    }
                }
            }
        }

        level().getEntities(this, getBoundingBox().inflate(2), EquinePredicates.PLAYER_UNICORN.and(e -> e instanceof Player)).stream().findFirst().ifPresent(player -> {
            keepAwake();
            if (open) {
                Vec3 diff = player.position().subtract(position());
                double yaw = Math.atan2(diff.z, diff.x) * 180D / Math.PI - 90;

                setYHeadRot((float)yaw);
                setYBodyRot((float)yaw);
            }
        });

        boolean daytime = MeteorlogicalUtil.getSkyAngle(level()) < 1;
        if (daytime != prevDaytime) {
            prevDaytime = daytime;
            if (daytime != getForcedState().orElse(daytime)) {
                clearForcedState();
            }
        }

        if (!level().isClientSide() && tickCount % 15 == 0) {

            if (altar.isEmpty()) {
                altar = Altar.locateAltar(level(), blockPosition()).map(altar -> {
                    altar.generateDecorations(level());
                    return altar;
                });
            }

            altar = altar.filter(altar -> {
                if (!altar.isValid(level())) {
                    altar.tearDown(null, level());
                    return false;
                }

                tickAltarCrafting((ServerLevel)level(), altar);

                Vec3 origin = Vec3.atCenterOf(altar.origin());
                altar.pillars().forEach(pillar -> tickAltarPillar(origin, pillar));

                return true;
            });
        }
    }

    public void setBeamTicks(int ticks) {
        level().broadcastEntityEvent(this, ticks > 0 ? ALTAR_BEAMS_START : ALTAR_BEAMS_END);
        beamsActive = ticks;
    }

    private void tickAltarCrafting(ServerLevel world, Altar altar) {
        if (activeRecipe == null || activeRecipe.isRemoved()) {
            AltarRecipe.Input entities = new AltarRecipe.Input(level().getEntitiesOfClass(ItemEntity.class, AABB.ofSize(Vec3.atCenterOf(altar.origin()), 2, 2, 2), EntitySelector.ENTITY_STILL_ALIVE));

            activeRecipe = world.recipeAccess()
                    .getRecipeFor(URecipes.ALTAR, entities, level())
                    .map(recipe -> recipe.value().toMatch(entities, level().registryAccess()))
                    .orElse(null);

            if (activeRecipe != null) {
                setBeamTicks(5);
            }
        }

        if (beamsActive <= 0) {
            return;
        }

        if (--beamsActive > 0) {
            playSound(USounds.Vanilla.GUARDIAN_ATTACK, 1.5F, 0.5F);
            return;
        }

        setBeamTicks(0);

        if (activeRecipe == null) {
            return;
        }

        activeRecipe.craft(level());
        activeRecipe = null;
        level().explode(this, altar.origin().getX(), altar.origin().getY(), altar.origin().getZ(), 0, ExplosionInteraction.NONE);
    }

    private void tickAltarPillar(Vec3 origin, BlockPos pillar) {
        Vec3 center = Vec3.atCenterOf(pillar).add(
                random.triangle(0.5, 0.2),
                random.triangle(0.5, 0.2),
                random.triangle(0.5, 0.2)
        );

        ((ServerLevel)level()).sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                center.x - 0.5, center.y + 0.5, center.z - 0.5,
                0,
                0.5, 0.5, 0.5, 0);

        if (random.nextInt(12) != 0) {
            return;
        }

        Vec3 vel = center.subtract(origin).normalize();

        ((ServerLevel)level()).sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                center.x - 0.5, center.y + 0.5, center.z - 0.5,
                0,
                vel.x, vel.y, vel.z, -0.2);

        if (random.nextInt(2000) == 0) {
            if (level().getBlockState(pillar).is(Blocks.CRYING_OBSIDIAN)) {
                pillar = pillar.below();
            }
            level().setBlockAndUpdate(pillar, Blocks.CRYING_OBSIDIAN.defaultBlockState());
        }
    }

    private boolean shouldBeSleeping() {
        return MeteorlogicalUtil.getSkyAngle(level()) > 1 && activeTicks <= 0;
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        remove(Entity.RemovalReason.KILLED);

        SoundType sound = SoundType.WOOD;

        level().playLocalSound(getX(), getY(), getZ(), sound.getBreakSound(), SoundSource.BLOCKS, sound.getVolume(), sound.getPitch(), true);

        if (world.getGameRules().get(GameRules.BLOCK_DROPS)) {
            spawnAtLocation(world, getPickResult(), 1);
        }
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        altar.ifPresent(altar -> altar.tearDown(this, level()));
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 vec) {
        if (player.isShiftKeyDown()) {
            setForcedState(TriState.of(!isOpen()));
            keepAwake();
            player.playSound(USounds.Vanilla.BOOK_PAGE_TURN, 2, 1);
            return InteractionResult.SUCCESS;
        }

        if (isOpen()) {
            keepAwake();
            player.openMenu(new ExtendedMenuProvider<SpellbookState>() {
                @Override
                public Component getDisplayName() {
                    return SpellbookEntity.this.getDisplayName();
                }

                @Override
                public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                    return new SpellbookScreenHandler(syncId, inv, ContainerLevelAccess.create(level(), blockPosition()), state, getUUID());
                }

                @Override
                public SpellbookState getScreenOpeningData(ServerPlayer player) {
                    return state;
                }
            });
            player.playSound(USounds.Vanilla.BOOK_PAGE_TURN, 2, 1);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel world, DamageSource damageSource) {
        return super.isInvulnerableTo(world, damageSource) || damageSource.is(UTags.DamageTypes.SPELLBOOK_IMMUNE_TO);
    }

    @Override
    public void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        prevDaytime = compound.getBooleanOr("prevDaytime", false);
        activeTicks = compound.getIntOr("activeTicks", 0);
        setAltered(compound.getBooleanOr("altered", false));
        setForcedState(compound.read("locked", com.mojang.serialization.Codec.BOOL).map(TriState::of).orElse(TriState.DEFAULT));
        setSpellbookState(compound.read("spellbookState", SpellbookState.CODEC).orElse(new SpellbookState()));
        altar = compound.read("altar", Altar.CODEC);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("activeTicks", activeTicks);
        compound.putBoolean("prevDaytime", prevDaytime);
        compound.putBoolean("altered", isAltered());
        compound.store("spellbookState", SpellbookState.CODEC, state);
        getForcedState().map(t -> {
            compound.putBoolean("locked", t);
            return null;
        });
        compound.storeNullable("altar", Altar.CODEC, altar.orElse(null));
    }

    @Override
    public void handleEntityEvent(byte status) {
        switch (status) {
            case ALTAR_BEAMS_START:
                altar = Altar.locateAltar(level(), blockPosition());
                hasBeams = altar.isPresent();
                break;
            case ALTAR_BEAMS_END:
                altar = Optional.empty();
                hasBeams = false;
            default:
                super.handleEntityEvent(status);
        }
    }
}
