package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.HomingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.behaviour.EntitySwap;
import com.minelittlepony.unicopia.entity.behaviour.Inventory;
import com.minelittlepony.unicopia.item.AlicornAmuletItem;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;

public class MindSwapSpell extends MimicSpell implements ProjectileDelegate.EntityHitListener {

    private final EntityReference<LivingEntity> counterpart = new EntityReference<>();

    private Optional<Inventory> myStoredInventory = Optional.empty();
    private Optional<Inventory> theirStoredInventory = Optional.empty();

    private boolean initialized;

    protected MindSwapSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Nullable
    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        if (caster.asEntity() instanceof LivingEntity living && isValidTarget(living)) {
            return null;
        }
        return method == CastingMethod.STAFF ? toThrowable() : this;
    }

    @Override
    protected void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        if (initialized && !caster.isClient()) {
            counterpart.ifPresent(caster.asWorld(), e -> {
                initialized = false;
                LivingEntity master = caster.getMaster();
                Caster<?> other = Caster.of(e).get();

                other.getSpellSlot().removeIf(SpellType.MIMIC);
                caster.getSpellSlot().removeIf(getType());

                if (!isValidTarget(master) || !isValidTarget(e)) {
                    master.hurtServer(caster.asServerWorld(), caster.asWorld().damageSources().magic(), Float.MAX_VALUE);
                    master.setHealth(0);
                    e.hurtServer(caster.asServerWorld(), caster.asWorld().damageSources().magic(), Float.MAX_VALUE);
                    e.setHealth(0);
                } else {
                    if (master instanceof ServerPlayer sMaster && e instanceof ServerPlayer sE) {
                        swapPlayerData(sMaster, sE);
                    } else {
                        EntitySwap.ALL.accept(e, master);
                        Inventory.swapInventories(caster.asServerWorld(),
                                e, myStoredInventory.or(() -> Inventory.of(e)),
                                master, theirStoredInventory.or(() -> Inventory.of(master)),
                                a -> {},
                                a -> {}
                        );
                    }
                }

                other.playSound(USounds.SPELL_MINDSWAP_UNSWAP, 0.2F);
                caster.playSound(USounds.SPELL_MINDSWAP_UNSWAP, 0.2F);
            });
            counterpart.set(null);
        }
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {

        if (situation != Situation.BODY) {
            return true;
        }

        if (!caster.isClient()) {
            if (!initialized) {
                counterpart.ifPresent(caster.asWorld(), e -> {
                    if (!isValidTarget(e)) {
                        counterpart.set(null);
                        setDead();
                        return;
                    }

                    LivingEntity master = caster.getMaster();

                    if (!isValidTarget(master)) {
                        counterpart.set(null);
                        setDead();
                        return;
                    }

                    setDisguise(e);
                    Caster<?> other = Caster.of(e).get();
                    MimicSpell mimic = SpellType.MIMIC.withTraits().apply(other, CastingMethod.INDIRECT);
                    mimic.setHidden(true);
                    mimic.setDisguise(master);

                    if (master instanceof ServerPlayer sMaster && e instanceof ServerPlayer sE) {
                        swapPlayerData(sMaster, sE);
                    } else {
                        EntitySwap.ALL.accept(master, e);
                        Inventory.swapInventories(caster.asServerWorld(),
                                master, Inventory.of(master),
                                e, Inventory.of(e),
                                a -> myStoredInventory = Optional.of(a),
                                a -> theirStoredInventory = Optional.of(a)
                        );
                    }

                    other.playSound(USounds.SPELL_MINDSWAP_SWAP, 1);
                    caster.playSound(USounds.SPELL_MINDSWAP_SWAP, 1);
                });
                initialized = true;
            }

            if (counterpart.isSet()) {
                LivingEntity other = counterpart.get(caster.asWorld());

                if (other == null) {
                    caster.getOriginatingCaster().asEntity().hurtServer(caster.asServerWorld(), caster.asWorld().damageSources().magic(), Float.MAX_VALUE);
                    destroy(caster);
                    return false;
                }

                if (!Caster.of(other).get().getSpellSlot().contains(SpellType.MIMIC)) {
                    destroy(caster);
                    return false;
                }
            }

            if (!caster.asEntity().isAlive()) {
                counterpart.ifPresent(caster.asWorld(), e -> {
                    e.hurtServer(caster.asServerWorld(), e.damageSources().magic(), Float.MAX_VALUE);
                });
                destroy(caster);
                return false;
            }
        }

        return super.tick(caster, situation);
    }


    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        Caster.of(projectile.getMaster()).ifPresent(master -> {
            if (hit.getEntity() instanceof LivingEntity living
                    && isValidTarget(living)
                    && getTypeAndTraits().apply(master, CastingMethod.DIRECT) instanceof HomingSpell homing) {
                homing.setTarget(hit.getEntity());
            }
        });
    }

    @Override
    public boolean setTarget(Entity target) {
        if (target instanceof LivingEntity living) {
            counterpart.set(living);
        }

        return false;
    }

    protected boolean isValidTarget(LivingEntity entity) {
        return Caster.of(entity).isPresent()
                //&& !UItems.ALICORN_AMULET.isApplicable(entity)
                && !AmuletItem.isApplicable(entity, UItems.PEARL_NECKLACE);
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        compound.put("counterpart", counterpart.toNBT(lookup));
        compound.putBoolean("initialized", initialized);
        compound.storeNullable("myStoredInventory", Inventory.CODEC, lookup.createSerializationContext(NbtOps.INSTANCE), myStoredInventory.orElse(null));
        compound.storeNullable("theirStoredInventory", Inventory.CODEC, lookup.createSerializationContext(NbtOps.INSTANCE), theirStoredInventory.orElse(null));
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        counterpart.fromNBT(compound.getCompoundOrEmpty("counterpart"), lookup);
        initialized = compound.getBooleanOr("initialized", false);
        myStoredInventory = compound.read("myStoredInventory", Inventory.CODEC, lookup.createSerializationContext(NbtOps.INSTANCE));
        theirStoredInventory = compound.read("theirStoredInventory", Inventory.CODEC, lookup.createSerializationContext(NbtOps.INSTANCE));
    }

    private static void swapPlayerData(ServerPlayer a, ServerPlayer b) {
        AlicornAmuletItem.updateAttributes(Living.living(a), 0);
        AlicornAmuletItem.updateAttributes(Living.living(b), 0);

        final GameType aMode = a.gameMode.getGameModeForPlayer();
        final GameType bMode = b.gameMode.getGameModeForPlayer();

        final UUID aUUid = a.getUUID();
        final UUID bUUid = b.getUUID();

        final byte aModelBits = a.getEntityData().get(PlayerAccess.getModelBitFlag());
        final byte bModelBits = b.getEntityData().get(PlayerAccess.getModelBitFlag());

        final ServerPlayer aClone = clonePlayer(a);
        final ServerPlayer bClone = clonePlayer(b);

        aClone.getAbilities().instabuild = a.getAbilities().instabuild;
        bClone.getAbilities().instabuild = b.getAbilities().instabuild;

        a.restoreFrom(bClone, true);
        b.restoreFrom(aClone, true);

        a.setUUID(aUUid);
        b.setUUID(bUUid);

        a.getEntityData().set(PlayerAccess.getModelBitFlag(), aModelBits);
        b.getEntityData().set(PlayerAccess.getModelBitFlag(), bModelBits);

        a.gameMode.changeGameModeForPlayer(aMode);
        b.gameMode.changeGameModeForPlayer(bMode);

        a.deathTime = 0;
        b.deathTime = 0;

        EntitySwap.POSITION.accept(a, b);
        EntitySwap.VELOCITY.accept(a, b);
        EntitySwap.PITCH.accept(a, b);
        EntitySwap.YAW.accept(a, b);

        a.onUpdateAbilities();
        b.onUpdateAbilities();

        Living.updateVelocity(a);
        Living.updateVelocity(b);
    }

    private static ServerPlayer clonePlayer(ServerPlayer player) {
        ServerPlayer clone = new FakePlayer(player.level(), player.getGameProfile()) {
            @Override
            public void tick() {
                discard();
            }

            @Override
            public void setUUID(UUID uuid) {
                super.setUUID(UUID.randomUUID());
            }
        };

        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
        player.saveWithoutId(output);
        CompoundTag compound = output.buildResult();
        compound.remove("Dimension");
        compound.getCompoundOrEmpty("unicopia_caster").remove("spells");
        clone.load(TagValueInput.create(ProblemReporter.DISCARDING, player.registryAccess(), compound));
        return clone;
    }
}
