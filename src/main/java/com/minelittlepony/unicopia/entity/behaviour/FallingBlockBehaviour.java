package com.minelittlepony.unicopia.entity.behaviour;

import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.mixin.MixinBlockEntity;
import com.minelittlepony.unicopia.mixin.MixinFallingBlock;
import com.minelittlepony.unicopia.util.Tickable;

public class FallingBlockBehaviour extends EntityBehaviour<FallingBlockEntity> {
    private static final Optional<EntityDimensions> FULL_BLOCK = Optional.of(EntityDimensions.scalable(0.6F, 0.9F).withEyeHeight(0.5F));

    @Override
    public Optional<EntityDimensions> getDimensions(FallingBlockEntity entity, Optional<EntityDimensions> current) {
        return FULL_BLOCK;
    }

    @Override
    public void onImpact(Caster<?> source, FallingBlockEntity entity, double distance, float damageMultiplier, DamageSource cause) {
        if (source.asEntity().fallDistance > 3) {
            entity.fallDistance = source.asEntity().fallDistance;
            entity.causeFallDamage(distance, damageMultiplier, cause);

            BlockState state = entity.getBlockState();
            if (state.getBlock() instanceof FallingBlock fb) {
                fb.onLand(entity.level(), entity.blockPosition(), state, state, entity);
            }
        }
    }

    @Override
    public boolean isEqual(FallingBlockEntity a, Entity b) {
        return b instanceof FallingBlockEntity f && f.getBlockState() == a.getBlockState();
    }

    private FallingBlockEntity configure(FallingBlockEntity entity, Block block) {
        if (block instanceof MixinFallingBlock) {
            ((MixinFallingBlock)block).invokeFalling(entity);
        }
        entity.dropItem = false;

        return entity;
    }

    @Override
    public FallingBlockEntity onCreate(FallingBlockEntity entity, EntityAppearance context, boolean replaceOld) {
        super.onCreate(entity, context, replaceOld);

        BlockState state = entity.getBlockState()
                .trySetValue(BlockStateProperties.CHEST_TYPE, ChestType.SINGLE)
                .trySetValue(BlockStateProperties.BED_PART, BedPart.HEAD)
                .trySetValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        Block block = state.getBlock();
        context.setBlockEntity(block instanceof EntityBlock bep ? bep.newBlockEntity(entity.blockPosition(), state) : null);

        if (state.hasProperty(BlockStateProperties.BED_PART)) {
            Vec3i offset = BedBlock.getConnectedDirection(state).getUnitVec3i();
            BlockState foot = state.setValue(BlockStateProperties.BED_PART, BedPart.FOOT);
            context.attachExtraEntity(Vec3.atLowerCornerOf(offset), configure(new FallingBlockEntity(entity.level(), entity.getX() + offset.getX(), entity.getY() + offset.getY(), entity.getZ() + offset.getZ(), foot), block));
        }

        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            BlockState upperState = state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
            context.attachExtraEntity(new Vec3(0, 1, 0), configure(new FallingBlockEntity(entity.level(), entity.getX(), entity.getY() + 1, entity.getZ(), upperState), block));
        }

        if (state != entity.getBlockState()) {
            entity = new FallingBlockEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), state);
        }
        return configure(entity, block);
    }

    @Override
    public void update(Living<?> source, FallingBlockEntity entity, Disguise spell) {

        BlockState state = entity.getBlockState();
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            boolean logged = entity.level().isWaterAt(entity.blockPosition());

            if (state.getValue(BlockStateProperties.WATERLOGGED) != logged) {
                entity = new FallingBlockEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), state.setValue(BlockStateProperties.WATERLOGGED, logged));
                spell.getAppearance().setAppearance(entity);
            }
        }

        EntityAppearance disguise = spell.getAppearance();

        BlockEntity be = disguise.getBlockEntity();

        if (source instanceof Pony player && be instanceof Tickable ceb && (be instanceof ChestBlockEntity || be instanceof EnderChestBlockEntity)) {
            if (player.sneakingChanged()) {
                be.triggerEvent(1, isSneakingOnGround(source) ? 1 : 0);
            }

            be.setLevel(entity.level());
            ((MixinBlockEntity)be).setPos(entity.blockPosition());
            ceb.tick();
            be.setLevel(null);
        }

        for (var attachment : disguise.getAttachments()) {
            copyBaseAttributes(source.asEntity(), attachment.entity(), attachment.offset());
        }
    }
}
