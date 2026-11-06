package com.minelittlepony.unicopia.ability;

import java.util.*;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.ability.data.tree.TreeType;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.server.world.BlockDestructionManager;
import com.minelittlepony.unicopia.server.world.ModificationType;
import com.minelittlepony.unicopia.util.*;

/**
 * Earth Pony kicking ability
 */
public class EarthPonyKickAbility implements Ability<Pos> {

    @Override
    public int getWarmupTime(Pony player) {
        return 3;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 50;
    }

    @Override
    public Identifier getIcon(Pony player) {
        return getId().withPath(p -> "textures/gui/ability/" + p
            + "_" + (player.getObservedSpecies().isHuman() ? Race.EARTH : player.getObservedSpecies()).getId().getPath()
            + "_" + (getKickDirection(player) > 0 ? "forward" : "backward")
            + ".png");
    }

    @Override
    public double getCostEstimate(Pony player) {
        return TraceHelper.findBlock(player.asEntity(), getKickDirection(player) * 6, 1)
                .filter(pos -> TreeType.at(pos, player.asWorld()) != TreeType.NONE)
                .isPresent() ? 3 : 1;
    }

    @Override
    public Optional<Pos> prepareQuickAction(Pony player, ActivationType type) {
        return Optional.of(getDefaultKickLocation(player));
    }

    @Override
    public boolean onQuickAction(Pony player, ActivationType type, Optional<Pos> data) {
        if (type == ActivationType.TAP) {

            if (!player.isClient()) {
                data.ifPresent(kickLocation -> {
                    Vec3 origin = player.getOriginVector();
                    Level w = player.asWorld();

                    player.asEntity().causeFoodExhaustion(3);

                    for (var e : VecHelper.findInRange(player.asEntity(), w, kickLocation.vec(), 2.5, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
                        if (e instanceof LivingEntity entity) {
                            float calculatedStrength = 0.5F * (1 + player.getLevel().getScaled(9));

                            if (player.asWorld() instanceof ServerLevel sw) {
                                entity.hurtServer(sw, player.damageOf(UDamageTypes.KICK, player), player.asWorld().getRandom().nextIntBetweenInclusive(2, 10) + calculatedStrength);
                            }
                            entity.knockback(calculatedStrength, origin.x - entity.getX(), origin.z - entity.getZ(),
                                    player.damageOf(UDamageTypes.KICK, player), 0);
                            Living.updateVelocity(entity);
                            player.subtractEnergyCost(3);
                            player.setAnimation(Animation.KICK, Animation.Recipient.ANYONE);
                            return;
                        }
                    }

                    BlockPos pos = kickLocation.pos();
                    EarthPonyStompAbility.stompBlock(player, w, pos, 10 * (1 + player.getLevel().getScaled(5)) * w.getBlockState(pos).getDestroyProgress(player.asEntity(), w, pos));
                    player.setAnimation(Animation.KICK, Animation.Recipient.ANYONE);
                });
            }

            return true;
        }

        if (type == ActivationType.DOUBLE_TAP && player.asEntity().onGround() && player.getMagicalReserves().getMana().get() > 40) {
            player.getPhysics().dashForward(player.asWorld().getRandom().triangle(3.5F, 0.3F));
            player.subtractEnergyCost(4);
            player.asEntity().causeFoodExhaustion(5);
            return true;
        }

        return false;
    }

    @Override
    public boolean acceptsQuickAction(Pony player, ActivationType type) {
        return type == ActivationType.NONE || type == ActivationType.TAP || type == ActivationType.DOUBLE_TAP;
    }

    @Nullable
    @Override
    public Optional<Pos> prepare(Pony player) {
        return TraceHelper.findBlock(player.asEntity(), 6 * getKickDirection(player), 1)
                .filter(pos -> TreeType.at(pos, player.asWorld()) != TreeType.NONE)
                .map(Pos::new)
                .or(() -> Optional.of(getDefaultKickLocation(player)));
    }

    private int getKickDirection(Pony player) {
        return MineLPDelegate.getInstance().getPlayerPonyRace(player.asEntity()).isEquine() && player.asEntity().isCrouching() ? -1 : 1;
    }

    private Pos getDefaultKickLocation(Pony player) {
        Vec3 kickVector = player.asEntity().getLookAngle().multiply(1, 0, 1);

        if (MineLPDelegate.getInstance().getPlayerPonyRace(player.asEntity()).isEquine()) {
            kickVector = kickVector.yRot((float)Math.PI);
        }
        return new Pos(BlockPos.containing(player.getOriginVector().add(kickVector)));
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Pos> getSerializer() {
        return Pos.CODEC;
    }

    @Override
    public boolean apply(Pony iplayer, Pos data) {

        BlockPos pos = data.pos();
        TreeType treeType = TreeType.at(pos, iplayer.asWorld());

        iplayer.setAnimation(Animation.KICK, Animation.Recipient.ANYONE);
        iplayer.subtractEnergyCost(treeType == TreeType.NONE ? 1 : 3);
        iplayer.asEntity().causeFoodExhaustion(3);

        return treeType.collectBlocks(iplayer.asWorld(), pos).filter(tree -> {
            ParticleUtils.spawnParticle(iplayer.asWorld(), UParticles.GROUND_POUND, data.vec(), Vec3.ZERO);

            Player player = iplayer.asEntity();

            if (BlockDestructionManager.of(player.level()).getBlockDestruction(pos) + 4 >= BlockDestructionManager.MAX_DAMAGE) {
                if (player.level().getRandom().nextInt(30) == 0) {
                    tree.logs().forEach(player.level(), (w, state, p) -> {
                        if (iplayer.canModifyAt(p, ModificationType.PHYSICAL)) {
                            w.destroyBlock(p, true);
                        }
                    });
                    tree.leaves().forEach(player.level(), (w, state, p) -> {
                        if (iplayer.canModifyAt(p, ModificationType.PHYSICAL)) {
                            Block.dropResources(w.getBlockState(p), w, p);
                            w.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    });
                }

                iplayer.subtractEnergyCost(3);
            } else {
                tree.leaves().forEach(player.level(), (w, state, p) -> {
                    if (w.getRandom().nextInt(30) == 0) {
                        w.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, p, Block.getId(state));
                    }
                });

                int cost = dropApples(player, pos);

                if (cost > 0) {
                    iplayer.subtractEnergyCost(cost / 7F);
                }
            }

            return true;
        }).isPresent();
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(40);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        player.asEntity().getFoodData().addExhaustion(0.1F);
    }

    private int dropApples(Player player, BlockPos pos) {
        TreeType treeType = TreeType.at(pos, player.level());
        return treeType.collectBlocks(player.level(), pos).map(tree -> {
            tree.logs().forEach(player.level(), (world, state, position) -> {
                affectBlockChange(player, position);
            });

            int[] dropCount = {0};
            tree.leaves().forEach(player.level(), (world, state, position) -> {
                affectBlockChange(player, position);
                if (!buckBlock(treeType, state, world, position)
                        .filter(i -> !i.isEmpty())
                        .map(stack -> createDrop(stack, position, world, dropCount))
                        .toList().isEmpty()) {
                    world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, position, Block.getId(state));
                }
            });

            return dropCount[0] / 3;
        }).orElse(0);
    }

    private ItemEntity createDrop(ItemStack stack, BlockPos pos, Level world, int[] dropCount) {
        ItemEntity entity = new ItemEntity(world,
            pos.getX() + world.getRandom().nextFloat(),
            pos.getY() - 0.5,
            pos.getZ() + world.getRandom().nextFloat(),
            stack
        );
        entity.setDefaultPickUpDelay();
        world.addFreshEntity(entity);
        dropCount[0]++;
        return entity;
    }

    private Stream<ItemStack> buckBlock(TreeType treeType, BlockState treeState, Level world, BlockPos position) {

        if (treeState.getBlock() instanceof Buckable buckable) {
            return buckable.onBucked((ServerLevel)world, treeState, position).stream();
        }

        BlockPos down = position.below();
        BlockState below = world.getBlockState(down);

        if (below.isAir()) {
            return Stream.of(treeType.pickRandomStack(world.getRandom(), treeState));
        }

        if (below.getBlock() instanceof Buckable buckable) {
            return buckable.onBucked((ServerLevel)world, below, down).stream();
        }

        return Stream.empty();
    }

    private void affectBlockChange(Player player, BlockPos position) {
        BlockDestructionManager.of(player.level()).damageBlock(position, 4);

        PosHelper.fastAll(position, p -> {
            BlockState s = player.level().getBlockState(p);

            if (s.getBlock() instanceof BeehiveBlock) {
                if (player.level().getBlockEntity(p) instanceof BeehiveBlockEntity hive) {
                    hive.emptyAllLivingFromHive(player, s, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
                }

                player.level().updateNeighbourForOutputSignal(position, s.getBlock());

                AABB area = new AABB(position).inflate(8, 6, 8);
                List<Bee> nearbyBees = player.level().getEntitiesOfClass(Bee.class, area);

                if (!nearbyBees.isEmpty()) {
                    List<Player> nearbyPlayers = player.level().getEntitiesOfClass(Player.class, area);
                    int i = nearbyPlayers.size();

                    for (Bee bee : nearbyBees) {
                        if (bee.getTarget() == null) {
                            bee.setTarget(nearbyPlayers.get(player.level().getRandom().nextInt(i)));
                        }
                    }
                }
            }
        }, PosHelper.HORIZONTAL);
    }

    public interface Buckable {
        List<ItemStack> onBucked(ServerLevel world, BlockState state, BlockPos pos);
    }
}
