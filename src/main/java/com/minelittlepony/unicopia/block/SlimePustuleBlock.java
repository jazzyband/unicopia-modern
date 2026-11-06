package com.minelittlepony.unicopia.block;

import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.minelittlepony.unicopia.USounds;
import com.mojang.serialization.MapCodec;

public class SlimePustuleBlock extends Block {
    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
    private static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final Direction[] DIRECTIONS = Arrays.stream(Direction.values())
            .filter(direction -> direction != Direction.UP)
            .toArray(Direction[]::new);
    private static final VoxelShape SHAFT_SHAPE = Block.box(7.5, 0, 7.5, 8.5, 16, 8.5);
    private static final VoxelShape DRIP_SHAPE = Shapes.or(
            Block.box(7, 10, 7, 9, 16, 9),
            Block.box(3, 15, 4, 9, 16, 10),
            Block.box(7, 15, 7, 12, 16, 12)
    );
    private static final VoxelShape BULB_SHAPE = Shapes.or(
            Block.box(4, 1, 4, 12, 10, 12),
            Block.box(5, 10, 5, 11, 13, 11),
            Block.box(6, 13, 6, 10, 15, 10),
            Block.box(7, 13, 7, 9, 20, 9)
    );
    private static final VoxelShape CAP_SHAPE = Shapes.or(SHAFT_SHAPE, DRIP_SHAPE);
    private static final int DUST_COLOR = ARGB.colorFromFloat(1, 1F, 0.2F, 0.1F);

    public SlimePustuleBlock(Properties settings) {
        super(settings.randomTicks());
        registerDefaultState(defaultBlockState().setValue(SHAPE, Shape.DRIP).setValue(POWERED, false));
    }


    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(SHAPE)) {
            case POD -> BULB_SHAPE;
            case DRIP -> DRIP_SHAPE;
            case CAP -> CAP_SHAPE;
            case STRING -> SHAFT_SHAPE;
        };
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        super.animateTick(state, world, pos, random);

        if (state.getValue(POWERED)) {
            VoxelShape shape = state.getOcclusionShape();
            float x = (float)Mth.lerp(random.nextFloat(), shape.min(Axis.X), shape.max(Axis.X));
            float z = (float)Mth.lerp(random.nextFloat(), shape.min(Axis.Z), shape.max(Axis.Z));
            world.addParticle(new DustParticleOptions(DUST_COLOR, 1),
                    pos.getX() + x,
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + z, 0, 0, 0);
        }

        if (random.nextInt(15) == 0) {
            VoxelShape shape = state.getOcclusionShape();
            float x = (float)Mth.lerp(random.nextFloat(), shape.min(Axis.X), shape.max(Axis.X));
            float z = (float)Mth.lerp(random.nextFloat(), shape.min(Axis.Z), shape.max(Axis.Z));
            world.addParticle(ParticleTypes.DRIPPING_HONEY,
                    pos.getX() + x,
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + z, 0, 0, 0);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(SHAPE) == Shape.POD && random.nextInt(130) == 0) {
            Slime slime = EntityTypes.SLIME.create(world, EntitySpawnReason.NATURAL);
            slime.setSize(1, true);
            slime.setPos(Vec3.atCenterOf(pos));
            world.addFreshEntity(slime);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (state.getValue(SHAPE) == Shape.POD) {
            if (world instanceof ServerLevel sw) {
                world.getEntities(player.hasCorrectToolForDrops(state) ? player : null, new AABB(pos).inflate(1)).forEach(entity -> {
                    entity.hurtServer(sw, entity.damageSources().inFire(), 2);
                    entity.setRemainingFireTicks(3);
                });
            }

            world.playSound(null, pos, USounds.BLOCK_SLIME_PUSTULE_POP, SoundSource.BLOCKS, 5, 1);
            for (int i = 0; i < 8; i++) {
                world.addParticle(ParticleTypes.LAVA,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        world.getRandom().nextGaussian() * 1.5F,
                        0,
                        world.getRandom().nextGaussian() * 1.5F
                );
                world.addParticle(ParticleTypes.CRIT,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        world.getRandom().nextGaussian() * 1.5F,
                        world.getRandom().nextGaussian() * 1.5F,
                        world.getRandom().nextGaussian() * 1.5F
                );
                world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        world.getRandom().nextGaussian() * 1.5F,
                        world.getRandom().nextGaussian() * 1.5F,
                        world.getRandom().nextGaussian() * 1.5F
                );
            }

            return state;
        }

        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, POWERED);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        pos = pos.above();
        state = world.getBlockState(pos);
        return state.is(this) || state.isFaceSturdy(world, pos, Direction.DOWN, SupportType.CENTER);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (!canSurvive(state, world, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        if (direction.getAxis() == Direction.Axis.Y) {
            Shape currentShape = state.getValue(SHAPE);

            if (direction == Direction.DOWN && (currentShape == Shape.CAP || currentShape == Shape.STRING)) {
                return state.setValue(POWERED, getReceivedRedstonePower(world, pos) > 0);
            }

            Shape shape = determineShape(world, pos);
            return state.setValue(SHAPE, shape).setValue(POWERED, getReceivedRedstonePower(world, pos) > 0);
        }

        return state.setValue(POWERED, getReceivedRedstonePower(world, pos) > 0);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Shape shape = determineShape(ctx.getLevel(), ctx.getClickedPos());
        return super.getStateForPlacement(ctx)
                .setValue(SHAPE, shape == Shape.STRING ? Shape.POD : shape)
                .setValue(POWERED, getReceivedRedstonePower(ctx.getLevel(), ctx.getClickedPos()) > 0);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
        BlockState newState = world.getBlockState(pos);
        if (state.is(this) && newState.is(this) && state.getValue(POWERED) != newState.getValue(POWERED)) {
            world.updateNeighborsAt(pos.above(), this, ExperimentalRedstoneUtils.initialOrientation(world, null, Direction.UP));
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return state.getValue(POWERED);
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        if (direction == Direction.DOWN && isSignalSource(state)) {
            return 15;
        }
        return 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        if (direction == Direction.DOWN && isSignalSource(state)) {
            return 15;
        }
        return 0;
    }

    private int getReceivedRedstonePower(BlockGetter world, BlockPos pos) {
        int power = 0;
        for (Direction direction : UPDATE_SHAPE_ORDER) {
            power = Math.max(power, world.getBlockState(pos.relative(direction)).getDirectSignal(world, pos, direction));
            if (power >= 15) {
                return Math.min(15, power);
            }
        }
        return Math.min(15, power);
    }

    private Shape determineShape(LevelReader world, BlockPos pos) {
        BlockState above = world.getBlockState(pos.above());
        BlockState below = world.getBlockState(pos.below());

        boolean hasAbove = above.is(this);
        boolean hasRoof = !hasAbove && above.isFaceSturdy(world, pos.above(), Direction.DOWN, SupportType.CENTER);
        boolean hasBelow = below.is(this);

        if (hasRoof && below.isAir()) {
            return Shape.DRIP;
        }
        if (hasRoof && hasBelow) {
            return Shape.CAP;
        }

        if ((hasRoof || hasAbove) && hasBelow) {
            return Shape.STRING;
        }

        return Shape.POD;
    }

    public enum Shape implements StringRepresentable {
        DRIP,
        CAP,
        STRING,
        POD;

        private final String name = name().toLowerCase(Locale.ROOT);

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
