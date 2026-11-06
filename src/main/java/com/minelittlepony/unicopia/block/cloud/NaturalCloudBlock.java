package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NaturalCloudBlock extends PoreousCloudBlock {

    private final Supplier<Block> compactedBlock;

    public NaturalCloudBlock(boolean meltable,
            @Nullable Supplier<Soakable> soggyBlock,
            Supplier<Block> compactedBlock,
            Properties settings) {
        super(meltable, soggyBlock, settings.noOcclusion());
        this.compactedBlock = compactedBlock;
    }


    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ItemTags.SHOVELS)) {
            BooleanProperty property = CompactedCloudBlock.FACING_PROPERTIES.get(hit.getDirection());
            world.setBlockAndUpdate(pos, compactedBlock.get().defaultBlockState().setValue(property, false));
            stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
            world.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN.value(), SoundSource.BLOCKS);
            return InteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }
}
