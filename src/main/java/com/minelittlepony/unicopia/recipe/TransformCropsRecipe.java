package com.minelittlepony.unicopia.recipe;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.minelittlepony.unicopia.block.state.StateUtil;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;

public class TransformCropsRecipe implements Recipe<TransformCropsRecipe.PlacementArea> {
    public static final MapCodec<TransformCropsRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("target").forGetter(recipe -> recipe.target),
            BlockState.CODEC.fieldOf("consume").forGetter(recipe -> recipe.catalyst),
            BlockState.CODEC.fieldOf("output").forGetter(recipe -> recipe.output)
    ).apply(instance, TransformCropsRecipe::new));
    private static final StreamCodec<ByteBuf, BlockState> STATE_PACKET_CODEC = ByteBufCodecs.INT.map(Block::stateById, Block::getId);
    public static final StreamCodec<RegistryFriendlyByteBuf, TransformCropsRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.BLOCK), recipe -> recipe.target,
            STATE_PACKET_CODEC, recipe -> recipe.catalyst,
            STATE_PACKET_CODEC, recipe -> recipe.output,
            TransformCropsRecipe::new
    );

    public static final int RADIUS = 3;
    public static final int SIDE_LENGTH = (2 * RADIUS) + 1;
    public static final int AREA = (SIDE_LENGTH * SIDE_LENGTH) - 1;
    public static final int MINIMUM_INPUT = 9;

    private final Block target;
    private final BlockState catalyst;
    private final BlockState output;

    public TransformCropsRecipe(Block target, BlockState catalyst, BlockState output) {
        this.output = output;
        this.target = target;
        this.catalyst = catalyst;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public BlockState getCatalystState() {
        return catalyst;
    }

    public BlockState getTargetState() {
        return target.defaultBlockState();
    }

    public ItemStack getTarget() {
        return target.asItem().getDefaultInstance();
    }

    public ItemStack getCatalyst() {
        return catalyst.getBlock().asItem().getDefaultInstance();
    }

    public ItemStack getOutput() {
        return output.getBlock().asItem().getDefaultInstance();
    }

    @Override
    public RecipeSerializer<TransformCropsRecipe> getSerializer() {
        return URecipes.TRANSFORM_CROP_SERIALIZER;
    }

    @Override
    public RecipeType<TransformCropsRecipe> getType() {
        return URecipes.GROWING;
    }

    @Override
    public boolean matches(PlacementArea inventory, Level world) {
        return world.getBlockState(inventory.position()).is(target);
    }

    @Override
    public ItemStack assemble(PlacementArea inventory) {
        return output.getBlock().asItem().getDefaultInstance();
    }

    public Result checkPattern(Level world, BlockPos pos) {
        BlockPos center = pos.below();
        Set<BlockPos> matches = new HashSet<>();
        for (BlockPos cell : BlockPos.spiralAround(center, RADIUS, Direction.EAST, Direction.NORTH)) {
            if (cell.equals(center)) {
                continue;
            }
            if (!world.getBlockState(cell).equals(catalyst)) {
                break;
            }
            matches.add(cell.immutable());
        }
        return new Result(this, matches);
    }

    public BlockState getResult(Level world, BlockPos pos) {
        return StateUtil.copyState(world.getBlockState(pos), output);
    }

    public static record PlacementArea (Pony pony, BlockPos position) implements RecipeInput {
        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ItemStack getItem(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return 0;
        }
    }

    public record Result (TransformCropsRecipe recipe, Set<BlockPos> matchedLocations) {
        public boolean shoudTransform(RandomSource random) {
            return random.nextInt(AREA) < matchedLocations().size();
        }
    }
}
