package com.minelittlepony.unicopia;

import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;
import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity.Action;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.entity.player.dummy.DummyPlayerEntity;
import com.minelittlepony.unicopia.particle.ParticleSpawner;
import com.minelittlepony.unicopia.recipe.CloudShapingRecipe;
import com.mojang.authlib.GameProfile;

public class InteractionManager {
    public static final int SOUND_EARS_RINGING = 0;
    public static final int SOUND_CHANGELING_BUZZ = 1;
    public static final int SOUND_BEE = 2;
    public static final int SOUND_MINECART = 3;
    public static final int SOUND_GLIDING = 4;
    public static final int SOUND_MAGIC_BEAM = 5;
    public static final int SOUND_HEART_BEAT = 6;
    public static final int SOUND_KIRIN_RAGE = 7;
    public static final int SOUND_GEM_FINDING_MAGIC_HUM = 8;

    public static final int SCREEN_DISPELL_ABILITY = 0;

    private static InteractionManager INSTANCE = new InteractionManager();

    @Nullable
    private SyncedConfig config;

    private final Stack<EquineContext> equineContext = new Stack<>();

    public static InteractionManager getInstance() {
        return INSTANCE;
    }

    public InteractionManager() {
        INSTANCE = this;
    }

    public ParticleSpawner createBoundParticle(UUID id) {
        return ParticleSpawner.EMPTY;
    }

    public SelectableRecipe.SingleInputSet<CloudShapingRecipe> getCloudShapingRecipes() {
        return SelectableRecipe.SingleInputSet.empty();
    }

    public void setCloudShapingRecipes(SelectableRecipe.SingleInputSet<CloudShapingRecipe> recipes) {

    }

    /**
     * Plays a custom sound instance
     */
    public void playLoopingSound(Entity source, int type, long seed) {

    }

    /**
     * Returns true on the client if the passed in player entity is the client's player.
     * Always returns false on the server.
     */
    public boolean isClientPlayer(@Nullable Player player) {
        return false;
    }

    /**
     * The player's camera mode. Always 0 on the server.
     */
    public int getViewMode() {
        return 0;
    }

    public float getTickRate() {
        return 20;
    }

    public void openScreen(int type) {
    }

    /**
     * Side-independent method to create a new player.
     *
     * Returns an implementation of PlayerEntity appropriate to the side being called on.
     */
    @NotNull
    public final Player createPlayer(Entity observer, GameProfile profile) {
        return createPlayer(observer.level(), profile);
    }

    /**
     * Side-independent method to create a new player.
     *
     * Returns an implementation of PlayerEntity appropriate to the side being called on.
     */
    @NotNull
    public Player createPlayer(Level world, GameProfile profile) {
        return new DummyPlayerEntity(world, profile);
    }

    public void sendPlayerLookAngles(Player player) {

    }

    public void addBlockBreakingParticles(BlockPos pos, Direction direction) {

    }

    public void interactLevitatingItem(LevitatingItemEntity entity, Player player) {
        if (player != entity.getMaster()) {
            entity.handleAction(Action.GRAB, player, Optional.empty());
        }
    }

    public void setEquineContext(EquineContext context) {
        synchronized (equineContext) {
            equineContext.push(context);
        }
    }

    public void clearEquineContext() {
        synchronized (equineContext) {
            if (!equineContext.isEmpty()) {
                equineContext.pop();
            }
        }
    }

    public EquineContext getEquineContext() {
        return getClientPony().map(EquineContext.class::cast).orElseGet(this::getPathingEquineContext);
    }

    public EquineContext getPathingEquineContext() {
        synchronized (equineContext) {
            return equineContext.isEmpty() ? EquineContext.ABSENT : equineContext.peek();
        }
    }

    public Optional<Pony> getClientPony() {
        return Optional.empty();
    }

    public final Race getClientSpecies() {
        return getClientPony().map(Pony::getSpecies).orElse(Race.HUMAN);
    }

    public void setSyncedConfig(SyncedConfig config) {
        this.config = config;
    }

    public SyncedConfig getSyncedConfig() {
        if (config == null) {
            config = Unicopia.getConfig().toSynced();
        }
        return config;
    }
}
