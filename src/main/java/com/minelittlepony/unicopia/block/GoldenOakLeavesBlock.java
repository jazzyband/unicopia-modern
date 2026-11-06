package com.minelittlepony.unicopia.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class GoldenOakLeavesBlock extends FruitBearingBlock {

    public GoldenOakLeavesBlock(int overlay, Supplier<Block> fruit, Supplier<ItemStack> rottenFruitSupplier, Properties settings) {
        super(overlay, fruit, rottenFruitSupplier, settings);
    }


    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        super.animateTick(state, world, pos, random);
        if (random.nextInt(15) == 0) {
            ParticleUtils.spawnParticleInBlock(world, pos, 1 + random.nextInt(4), ParticleTypes.ELECTRIC_SPARK);
        }
    }

    @Override
    protected boolean shouldAdvance(RandomSource random) {
        return random.nextInt(1000) == 0;
    }

    @Override
    public BlockState getPlacedFruitState(RandomSource random) {
        return super.getPlacedFruitState(random).setValue(EnchantedFruitBlock.ENCHANTED, random.nextInt(1000) == 0);
    }
}
