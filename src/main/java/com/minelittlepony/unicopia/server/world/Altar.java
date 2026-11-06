package com.minelittlepony.unicopia.server.world;

import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.entity.mob.FloatingArtefactEntity;
import com.minelittlepony.unicopia.entity.mob.SpellbookEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Altar(
        /**
         * The position of the central spectral fire of this altar
         */
        BlockPos origin,
        /**
         * Pillar top positions
         */
        Set<BlockPos> pillars
) {
    private static final Direction[] HORIZONTALS = { Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST };
    private static final Predicate<Entity> IS_PARTICIPANT = EntitySelector.ENTITY_STILL_ALIVE.and(e -> e instanceof FloatingArtefactEntity || e instanceof SpellbookEntity);
    public static final Codec<Altar> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("origin").forGetter(Altar::origin),
        CodecUtils.setOf(BlockPos.CODEC).fieldOf("pillars").forGetter(Altar::pillars)
    ).apply(instance, Altar::new));

    private static final int INNER_RADIUS = 4;
    private static final int PILLAR_OFFSET_FROM_CENTER = 2;

    private static final BlockPos PILLAR_A = new BlockPos(INNER_RADIUS, 0,  PILLAR_OFFSET_FROM_CENTER);
    private static final BlockPos PILLAR_B = new BlockPos(INNER_RADIUS, 0, -PILLAR_OFFSET_FROM_CENTER);

    public static Optional<Altar> locateAltar(Level world, BlockPos startingPoint) {

        BlockPos.MutableBlockPos mutable = startingPoint.mutable();

        if (!world.canSeeSky(mutable)) {
            return Optional.empty();
        }

        mutable.move(Direction.DOWN);
        if (!world.getBlockState(mutable).is(Blocks.LODESTONE)) {
            return Optional.empty();
        }

        for (int i = 0; i < 4; i++) {
            mutable.set(startingPoint);
            mutable.move(Direction.DOWN);
            mutable.move(Direction.from2DDataValue(i), 2);

            if (world.getBlockState(mutable).is(Blocks.SOUL_SAND)) {
                if (checkSlab(world, mutable)) {
                    mutable.move(Direction.UP);
                    BlockState fireState = world.getBlockState(mutable);
                    if (!(fireState.isAir() || fireState.is(BlockTags.FIRE))) {
                        return Optional.empty();
                    }
                    BlockPos firePos = mutable.immutable();
                    mutable.move(Direction.DOWN);

                    final Set<BlockPos> pillars = new HashSet<>();

                    if (checkPillarPair(world, mutable, Rotation.NONE, pillars::add)
                      && checkPillarPair(world, mutable, Rotation.CLOCKWISE_90, pillars::add)
                      && checkPillarPair(world, mutable, Rotation.COUNTERCLOCKWISE_90, pillars::add)
                      && checkPillarPair(world, mutable, Rotation.CLOCKWISE_180, pillars::add)) {

                        return Optional.of(new Altar(firePos, pillars));
                    }

                }
            }
        }

        return Optional.empty();
    }

    private static boolean checkSlab(Level world, BlockPos pos) {
        return !PosHelper.fastAny(pos, p -> !isObsidian(world, p), HORIZONTALS);
    }

    private static boolean checkPillarPair(Level world, BlockPos.MutableBlockPos center, Rotation rotation, Consumer<BlockPos> pillarPosCollector) {
        return checkPillar(world, center, PILLAR_A.rotate(rotation), pillarPosCollector)
            && checkPillar(world, center, PILLAR_B.rotate(rotation), pillarPosCollector);
    }

    private static boolean checkPillar(Level world, BlockPos.MutableBlockPos pos, Vec3i pillarPos, Consumer<BlockPos> pillarPosCollector) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (isObsidian(world, pos.move(pillarPos))
            && isObsidian(world, pos.move(Direction.UP))
            && isObsidian(world, pos.move(Direction.UP))
            && isObsidian(world, pos.move(Direction.UP))
            && world.canSeeSky(pos.move(Direction.UP))) {
            pillarPosCollector.accept(pos.move(Direction.DOWN).immutable());
            pos.set(x, y, z);
            return true;
        }

        return false;
    }

    private static boolean isObsidian(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN);
    }

    private static boolean checkState(Level world, BlockPos pos, Block block) {
        return world.getBlockState(pos).is(block);
    }

    public void generateDecorations(Level world) {
        world.setBlock(origin, UBlocks.SPECTRAL_FIRE.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_ALL);
        pillars.forEach(pillar -> {
            FloatingArtefactEntity artefact = UEntities.FLOATING_ARTEFACT.create(world, EntitySpawnReason.NATURAL);
            artefact.setStack(UItems.ALICORN_BADGE.getDefaultInstance());
            artefact.setPos(Vec3.atCenterOf(pillar.above()));
            artefact.setPermanentlyInvulnerable(true);
            artefact.setAltar(this);
            removeExisting(null, world, pillar);
            world.addFreshEntity(artefact);
        });
        if (!world.isClientSide()) {
            UnicopiaWorldProperties.forWorld((ServerLevel)world).addAltar(origin());
        }
    }

    public void tearDown(@Nullable Entity except, Level world) {
        if (!world.isClientSide()) {
            UnicopiaWorldProperties.forWorld((ServerLevel)world).removeAltar(origin());
        }
        pillars.forEach(pillar -> removeExisting(except, world, pillar));
    }

    private void removeExisting(@Nullable Entity except, Level world, BlockPos pillar) {
        world.getEntities(except, new AABB(pillar.above()), IS_PARTICIPANT).forEach(e -> {
            if (world instanceof ServerLevel sw) {
                e.kill(sw);
            } else {
                e.setRemoved(RemovalReason.KILLED);
            }
        });
    }

    public boolean isValid(Level world) {
        return checkState(world, origin, UBlocks.SPECTRAL_FIRE)
                && checkState(world, origin.below(), Blocks.SOUL_SAND)
                && checkSlab(world, origin.below())
                && pillars.stream().allMatch(pillar ->
                           isObsidian(world, pillar)
                        && world.canSeeSky(pillar.above())
                        && isObsidian(world, pillar.below())
                        && isObsidian(world, pillar.below(2))
                        && isObsidian(world, pillar.below(3)));
    }
}
