package com.minelittlepony.unicopia.client;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.BeeAggressiveSoundInstance;
import net.minecraft.client.resources.sounds.BeeFlyingSoundInstance;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.FlightType;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.client.gui.DismissSpellScreen;
import com.minelittlepony.unicopia.client.gui.UHud;
import com.minelittlepony.unicopia.client.particle.ClientBoundParticleSpawner;
import com.minelittlepony.unicopia.client.sound.*;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;
import com.minelittlepony.unicopia.entity.player.PlayerPhysics;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.entity.player.dummy.DummyClientPlayerEntity;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgPlayerTargetEntity;
import com.minelittlepony.unicopia.particle.ParticleSpawner;
import com.minelittlepony.unicopia.recipe.CloudShapingRecipe;
import com.mojang.authlib.GameProfile;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class ClientInteractionManager extends InteractionManager {
    private final Minecraft client = Minecraft.getInstance();

    private final Int2ObjectMap<WeakReference<TickableSoundInstance>> playingSounds = new Int2ObjectOpenHashMap<>();
    private final Map<UUID, Int2ObjectMap<WeakReference<TickableSoundInstance>>> entitySounds = new HashMap<>();

    private SelectableRecipe.SingleInputSet<CloudShapingRecipe> recipes = SelectableRecipe.SingleInputSet.empty();

    @Override
    public SelectableRecipe.SingleInputSet<CloudShapingRecipe> getCloudShapingRecipes() {
        return recipes;
    }

    @Override
    public void setCloudShapingRecipes(SelectableRecipe.SingleInputSet<CloudShapingRecipe> recipes) {
        this.recipes = recipes;
    }

    @Override
    public void playLoopingSound(Entity source, int type, long seed) {
        client.execute(() -> {
            if (type == SOUND_EARS_RINGING && source instanceof LivingEntity living) {
                play(type, () -> new LoopingSoundInstance<>(living,
                        createTicker(100).and(e -> !e.isRemoved()),
                        USounds.ENTITY_PLAYER_EARS_RINGING, 0.01F, 2, RandomSource.create(seed)).setFadeIn()
                );
            } else if (type == SOUND_BEE && source instanceof Bee bee) {
                play(type, () ->
                        bee.isAngry()
                            ? new BeeAggressiveSoundInstance(bee)
                            : new BeeFlyingSoundInstance(bee)
                );
            } else if (type == SOUND_MINECART && source instanceof AbstractMinecart minecart) {
                play(type, () -> new MinecartSoundInstance(minecart));
            } else if (type == SOUND_CHANGELING_BUZZ && source instanceof Player player) {
                play(type, () -> new MotionBasedSoundInstance<>(USounds.ENTITY_PLAYER_CHANGELING_BUZZ, player, e -> {
                    PlayerPhysics physics = Pony.of(e).getPhysics();
                    return physics.isFlying() && physics.getFlightType() == FlightType.INSECTOID;
                }, 0.25F, 0.5F, 0.66F, RandomSource.create(seed)));
            } else if (type == SOUND_GLIDING && source instanceof Player player && isClientPlayer(player)) {
                play(type, () -> new MotionBasedSoundInstance<>(USounds.Vanilla.ELYTRA_FLYING, player, e -> {
                    Pony pony = Pony.of(e);
                    return pony.getPhysics().isFlying() && pony.getPhysics().getFlightType().isAvian();
                }, 0, 1, 1, RandomSource.create(seed)));
            } else if (type == SOUND_GLIDING && source instanceof Player player) {
                play(type, () -> new MotionBasedSoundInstance<>(USounds.ENTITY_PLAYER_PEGASUS_FLYING, player, e -> {
                    Pony pony = Pony.of(e);
                    return pony.getPhysics().isFlying() && pony.getPhysics().getFlightType().isAvian();
                }, 0, 1, 1, RandomSource.create(seed)));
            } else if (type == SOUND_MAGIC_BEAM) {
                play(type, () -> new LoopedEntityTrackingSoundInstance(USounds.SPELL_CAST_SHOOT, 0.3F, 1F, source, seed));
            } else if (type == SOUND_HEART_BEAT) {
                play(type, () -> new NonLoopingFadeOutSoundInstance(USounds.ENTITY_PLAYER_HEARTBEAT_LOOP, SoundSource.PLAYERS, 0.3F, RandomSource.create(seed), 80L));
            } else if (type == SOUND_KIRIN_RAGE) {
                Entity soundEntity = source;
                play(type, () -> new FadeOutSoundInstance(USounds.ENTITY_PLAYER_KIRIN_RAGE_LOOP, SoundSource.AMBIENT, 0.3F, RandomSource.create(seed)) {
                    @Override
                    protected boolean shouldKeepPlaying() {
                        return EquinePredicates.RAGING.test(soundEntity);
                    }
                });
            } else if (type == SOUND_GEM_FINDING_MAGIC_HUM) {
                play(source.getUUID(), type, () -> {
                    return new MagicAuraSoundInstance(source.getSoundSource(), Living.living(source), source.level().getRandom());
                });
            }
        });
    }

    private void play(UUID sourceId, int type, Supplier<TickableSoundInstance> soundSupplier) {
        play(entitySounds.computeIfAbsent(sourceId, id -> new Int2ObjectOpenHashMap<>()), type, soundSupplier);
    }

    private void play(int type, Supplier<TickableSoundInstance> soundSupplier) {
        play(playingSounds, type, soundSupplier);
    }

    private void play(Int2ObjectMap<WeakReference<TickableSoundInstance>> playingSounds, int type, Supplier<TickableSoundInstance> soundSupplier) {
        WeakReference<TickableSoundInstance> activeSound = playingSounds.get(type);
        TickableSoundInstance existing;
        if (activeSound == null || (existing = activeSound.get()) == null || existing.isStopped()) {
            existing = soundSupplier.get();
            playingSounds.put(type, new WeakReference<>(existing));
            playNow(existing);
        }
    }

    private void playNow(TickableSoundInstance sound) {
        client.getSoundManager().queueTickingSound(sound);
    }

    static Predicate<LivingEntity> createTicker(int ticks) {
        int[] ticker = new int[] {ticks};
        return entity -> ticker[0]-- > 0;
    }

    @Override
    public void openScreen(int type) {
        client.execute(() -> {
            if (type == SCREEN_DISPELL_ABILITY) {
                client.gui.setScreen(new DismissSpellScreen());
            }
        });
    }

    @Override
    @NotNull
    public Player createPlayer(Level world, GameProfile profile) {
        if (world instanceof ClientLevel cw) {
            return new DummyClientPlayerEntity(cw, profile);
        }
        return super.createPlayer(world, profile);
    }

    @Override
    public boolean isClientPlayer(@Nullable Player player) {
        return (client.player != null && player != null)
             && (client.player == player
                 || Pony.equal(client.player, player));
    }

    @Override
    public int getViewMode() {
        return client.options.getCameraType().ordinal();
    }

    @Override
    public float getTickRate() {
        return client.level == null ? 20 : client.level.tickRateManager().tickrate();
    }

    @Override
    public void interactLevitatingItem(LevitatingItemEntity entity, Player player) {
        if (!entity.level().isClientSide()) {
            super.interactLevitatingItem(entity, player);
        } else {
            if (player == client.player && entity == UHud.INSTANCE.levitatingItemActions.targetEntity
                    && UHud.INSTANCE.levitatingItemActions.currentAction != null
                    && UHud.INSTANCE.levitatingItemActions.currentAction != LevitatingItemEntity.Action.MOVE) {
                Channel.CLIENT_PLAYER_LOOK_AT_ENTITY.sendToServer(new MsgPlayerTargetEntity(Optional.of(entity.getId()), Optional.of(UHud.INSTANCE.levitatingItemActions.currentAction), Optional.empty()));
            }
        }
    }

    @Override
    public ParticleSpawner createBoundParticle(UUID id) {
        return new ClientBoundParticleSpawner(id);
    }

    @Override
    public void sendPlayerLookAngles(Player player) {
        if (player instanceof LocalPlayer c) {
            c.connection.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround(), player.horizontalCollision));
        }
    }

    @Override
    public void addBlockBreakingParticles(BlockPos pos, Direction direction) {
        if (client.level != null) {
            client.level.addDestroyBlockEffect(pos, client.level.getBlockState(pos));
        }
    }

    @Override
    public Optional<Pony> getClientPony() {
        return Optional.ofNullable(client.player).map(Pony::of);
    }
}
