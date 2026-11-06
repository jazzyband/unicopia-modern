package com.minelittlepony.unicopia.item;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.mob.FloatingArtefactEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.entity.mob.FloatingArtefactEntity.State;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;

public class CrystalHeartItem extends Item implements FloatingArtefactEntity.Artifact {
    static final Predicate<Entity> TARGET_PREDICATE = EntitySelector.NO_CREATIVE_OR_SPECTATOR
            .and(EntitySelector.ENTITY_STILL_ALIVE)
            .and(e -> (e instanceof Player || e instanceof Mob));
    private static final Supplier<Map<Item, Item>> ITEM_MAP = Suppliers.memoize(() -> {
        return Map.of(
                Items.BUCKET, UItems.LOVE_BUCKET,
                Items.GLASS_BOTTLE, UItems.LOVE_BOTTLE,
                UItems.MUG, UItems.LOVE_MUG
        );
    });

    private static boolean isFillable(ItemStack stack) {
        return ITEM_MAP.get().containsKey(stack.getItem());
    }

    private static ItemStack fill(ItemStack stack) {
        Item item = ITEM_MAP.get().getOrDefault(stack.getItem(), stack.getItem());
        return item == stack.getItem() ? stack : stack.transmuteCopy(item);
    }

    public CrystalHeartItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Level world = context.getLevel();
        BlockPos blockPos = new BlockPlaceContext(context).getClickedPos();

        AABB placementArea = UEntities.FLOATING_ARTEFACT.getDimensions().makeBoundingBox(Vec3.atBottomCenterOf(blockPos));

        if (!world.noCollision(null, placementArea)) {
            return InteractionResult.FAIL;
        }

        if (world instanceof ServerLevel serverWorld) {
            var consumer = EntityType.<FloatingArtefactEntity>createDefaultStackConfig(serverWorld, context.getItemInHand(), context.getPlayer());
            FloatingArtefactEntity entity = UEntities.FLOATING_ARTEFACT.create(serverWorld, consumer, blockPos, EntitySpawnReason.SPAWN_ITEM_USE, false, true);

            if (entity == null) {
                return InteractionResult.FAIL;
            }

            entity.setStack(context.getItemInHand().split(1));
            serverWorld.addFreshEntity(entity);

            if (findStructure(entity)) {
                entity.playSound(USounds.ENTITY_CRYSTAL_HEART_ACTIVATE, 0.75F, 0.8F);
                UCriteria.POWER_UP_HEART.trigger(context.getPlayer());
            }
        } else {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onArtifactTick(FloatingArtefactEntity entity) {

        if (entity.getState() == State.INITIALISING) {
            if (entity.tickCount % 30 == 0 && findStructure(entity)) {
                entity.setState(State.RUNNING);
                entity.setRotationSpeed(4, 30);
            }
        } else {
            if (entity.tickCount % 30 == 0 && !findStructure(entity)) {
                entity.setState(State.INITIALISING);
            }

            BlockPos pos = entity.blockPosition();
            entity.level().addParticle(ParticleTypes.COMPOSTER,
                    pos.getX() + entity.level().getRandom().nextFloat(),
                    pos.getY() + entity.level().getRandom().nextFloat(),
                    pos.getZ() + entity.level().getRandom().nextFloat(),
                    0, 0, 0);

            if (entity.tickCount % 80 == 0 && entity.level() instanceof ServerLevel sw) {
                List<LivingEntity> inputs = new ArrayList<>();
                List<LivingEntity> outputs = new ArrayList<>();
                List<ItemEntity> containers = new ArrayList<>();

                VecHelper.findInRange(entity, entity.level(), entity.position(), 20, TARGET_PREDICATE).forEach(e -> {
                    LivingEntity living = (LivingEntity)e;

                    if (e instanceof Player
                            || (e instanceof TamableAnimal t && t.isTame())
                            || (e instanceof Mob s && s.isSaddled())
                            || (e instanceof AbstractVillager)) {
                        if (living.getHealth() < living.getMaxHealth()) {
                            outputs.add(living);
                        }
                    } else if (e.getType().getCategory() == MobCategory.MONSTER) {
                        inputs.add(living);
                    }
                });
                VecHelper.findInRange(entity, entity.level(), entity.position(), 20, i -> {
                    return i instanceof ItemEntity ie && isFillable(ie.getItem()) && EquinePredicates.CHANGELING.test(i);
                }).forEach(i -> containers.add((ItemEntity)i));

                int demand = outputs.size() + containers.stream().mapToInt(i -> i.getItem().getCount()).sum();
                int supply = inputs.size();

                if (demand == 0 || supply == 0) {
                    return;
                }

                float gives = supply > demand ? supply / demand : 1;
                float takes = demand > supply ? demand / supply : 1;

                inputs.forEach(input -> {
                    input.hurtServer(sw, entity.damageOf(UDamageTypes.LIFE_DRAINING), takes);
                    ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, entity, 0.2F), input, 1);
                });
                outputs.forEach(output -> {
                    ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, output, 0.2F), entity, 1);
                    output.heal(gives);
                });
                containers.forEach(container -> {
                    ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, container, 0.2F), entity, 1);
                    container.setItem(fill(container.getItem()));
                });

                if (gives > 0) {
                    entity.setRotationSpeed(37, 80);
                }
            }
        }

    }

    @Override
    public InteractionResult onArtifactDestroyed(FloatingArtefactEntity entity) {
        entity.playSound(USounds.ENTITY_CRYSTAL_HEART_DEACTIVATE, 0.75F, 1);
        if (entity.level() instanceof ServerLevel sw) {
            entity.spawnAtLocation(sw, new ItemStack(UItems.CRYSTAL_SHARD, 1 + sw.getRandom().nextInt(5)), 0);
        }
        return InteractionResult.SUCCESS;
    }

    private boolean findStructure(FloatingArtefactEntity entity) {
        return findPyramid(entity, Direction.UP) && findPyramid(entity, Direction.DOWN);
    }

    private boolean findPyramid(FloatingArtefactEntity entity, Direction direction) {

        BlockPos tip = entity.blockPosition().relative(direction);
        BlockState tipState = entity.level().getBlockState(tip);
        if (!tipState.is(UTags.Blocks.CRYSTAL_HEART_ORNAMENT) || (!tipState.hasProperty(EndRodBlock.FACING)|| tipState.getValue(EndRodBlock.FACING) != direction.getOpposite())) {
            return false;
        }

        tip = tip.relative(direction);
        if (!isDiamond(entity.level().getBlockState(tip))) {
            return false;
        }
        tip = tip.relative(direction);

        final BlockPos center = tip;

        return com.minelittlepony.unicopia.util.PosHelper.withinManhattanStream(center, 1, 0, 1)
                .filter(p -> p.getX() == center.getX() || p.getZ() == center.getZ())
                .map(entity.level()::getBlockState)
                .allMatch(this::isDiamond);
    }

    private boolean isDiamond(BlockState state) {
        return state.is(UTags.Blocks.CRYSTAL_HEART_BASE);
    }
}
