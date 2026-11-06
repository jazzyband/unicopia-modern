package com.minelittlepony.unicopia.ability;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.recipe.TransformCropsRecipe;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.minelittlepony.unicopia.server.world.BlockDestructionManager;
import com.minelittlepony.unicopia.server.world.ModificationType;
import com.minelittlepony.unicopia.util.TraceHelper;
import com.minelittlepony.unicopia.util.VecHelper;

/**
 * Earth Pony ability to grow crops
 */
public class EarthPonyGrowAbility implements Ability<Pos> {

    @Override
    public int getWarmupTime(Pony player) {
        return 10;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 50;
    }

    @Override
    public Optional<Pos> prepare(Pony player) {
        return TraceHelper.findBlock(player.asEntity(), 3, 1).map(Pos::new);
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Pos> getSerializer() {
        return Pos.CODEC;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 10;
    }

    @Override
    public boolean apply(Pony player, Pos data) {
        int count = 0;

        if (!applyDirectly(player, data.pos())) {
            for (BlockPos pos : BlockPos.betweenClosed(
                    data.pos().offset(-2, -2, -2),
                    data.pos().offset( 2,  2,  2))) {
                if (player.canModifyAt(pos, ModificationType.PHYSICAL)) {
                    count += applySingle(player, player.asWorld(), player.asWorld().getBlockState(pos), pos);
                }
            }
        } else {
            count = 1;
        }

        if (count > 0) {
            player.subtractEnergyCost(count / 5D);
        }
        return true;
    }

    protected int applySingle(Pony player, Level w, BlockState state, BlockPos pos) {

        ItemStack stack = new ItemStack(Items.BONE_MEAL);

        if (state.getBlock() instanceof Growable growable) {
            return growable.grow(w, state, pos) ? 1 : 0;
        }

        if (state.is(Blocks.CARROTS)) {
            if (state.getValue(CarrotBlock.AGE) == CarrotBlock.MAX_AGE) {
                boolean transform = w.getRandom().nextInt(3) == 0;
                spawnConversionParticles(w, pos, transform);
                if (transform) {
                    w.setBlockAndUpdate(pos, UBlocks.GOLD_ROOT.defaultBlockState().setValue(CarrotBlock.AGE, CarrotBlock.MAX_AGE));
                }

                return 5;
            }
        }

        if (w.getBlockState(pos).is(UTags.Blocks.UNAFFECTED_BY_GROW_ABILITY)) {
            return 0;
        }

        if (BoneMealItem.growCrop(stack, w, pos)) {
            if (w.getRandom().nextInt(350) == 0) {
                if (w.getBlockState(pos.below()).is(Blocks.FARMLAND)) {
                    w.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
                }
                w.setBlockAndUpdate(pos, UBlocks.PLUNDER_VINE_BUD.defaultBlockState());
            } else if (w.getRandom().nextInt(5000) == 0) {
                if (w.getBlockState(pos.below()).is(Blocks.FARMLAND)) {
                    w.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
                }
                UBlocks.CURING_JOKE.grow(w, state, pos);
            }
            return 1;
        }

        if (BoneMealItem.growWaterPlant(stack, w, pos, Direction.UP)) {
            return 1;
        }

        return 0;
    }

    @SuppressWarnings("unchecked")
    private boolean applyDirectly(Pony player, BlockPos pos) {
        var placementArea = new TransformCropsRecipe.PlacementArea(player, pos);
        return StreamSupport.stream(((ServerLevel)player.asWorld()).recipeAccess().getRecipes().spliterator(), false)
                .filter(recipe -> recipe.value().getType() == URecipes.GROWING)
                .map(recipe -> (RecipeHolder<TransformCropsRecipe>)recipe)
                .filter(recipe -> recipe.value().matches(placementArea, player.asWorld()))
                .map(recipe -> recipe.value().checkPattern(player.asWorld(), pos))
                .filter(result -> result.matchedLocations().size() + 1 >= TransformCropsRecipe.MINIMUM_INPUT)
                .filter(result -> {
            boolean transform = result.shoudTransform(player.asWorld().getRandom());

            player.playSound(USounds.ENTITY_CRYSTAL_SHARDS_AMBIENT, 1);

            result.matchedLocations().forEach(cell -> {
                spawnConversionParticles(player.asWorld(), cell.above(), false);
                BlockDestructionManager manager = BlockDestructionManager.of(player.asWorld());
                if (transform) {
                    if (manager.damageBlock(cell, 8) >= BlockDestructionManager.MAX_DAMAGE || player.asWorld().getRandom().nextInt(20) == 0) {
                        player.asWorld().setBlockAndUpdate(cell, Blocks.DIRT.defaultBlockState());
                        player.asWorld().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, cell, Block.getId(player.asWorld().getBlockState(cell)));
                    }
                } else {
                    if (manager.damageBlock(cell, 4) >= BlockDestructionManager.MAX_DAMAGE || player.asWorld().getRandom().nextInt(20) == 0) {
                        player.asWorld().setBlockAndUpdate(cell, Blocks.DIRT.defaultBlockState());
                        player.asWorld().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, cell, Block.getId(player.asWorld().getBlockState(cell)));
                    }
                }
            });

            spawnConversionParticles(player.asWorld(), pos, transform);
            if (transform) {
                player.asWorld().setBlockAndUpdate(pos, result.recipe().getResult(player.asWorld(), pos));
            }

            return true;
        })
        .findFirst()
        .isPresent();
    }

    private static void spawnConversionParticles(Level w, BlockPos pos, boolean success) {
        Supplier<Vec3> posSupplier = () -> Vec3.atCenterOf(pos).add(VecHelper.triangular(w.getRandom(), 0, 0.5));

        for (int i = 0; i < 25; i++) {
            ParticleUtils.spawnParticle(w, new MagicParticleEffect(0xFFFF00), posSupplier.get(), Vec3.ZERO);
            if (success) {
                ParticleUtils.spawnParticle(w, ParticleTypes.CLOUD, posSupplier.get(), Vec3.ZERO);
            }
        }
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(30);

        if (player.asWorld().isClientSide()) {
            player.spawnParticles(MagicParticleEffect.UNICORN, 1);
        }
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {

    }

    public interface Growable {
        boolean grow(Level world, BlockState state, BlockPos pos);
    }
}
