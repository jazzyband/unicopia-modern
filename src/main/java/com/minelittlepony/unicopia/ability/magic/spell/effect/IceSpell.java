package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.List;

import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractAreaEffectSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.block.state.StatePredicate;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class IceSpell extends AbstractSpell {
    public static final SpellTraits DEFAULT_TRAITS = SpellTraits.builder()
            .with(Trait.ICE, 15)
            .build();

    private static final SpellAttribute<Float> RANGE = AbstractAreaEffectSpell.range(3);

    static final TooltipFactory TOOLTIP = RANGE;

    protected IceSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        boolean submerged = source.asEntity().isUnderWater() || source.asEntity().isEyeInFluid(FluidTags.LAVA);
        float radius = RANGE.get(getTraits());

        long blocksAffected = AbstractAreaEffectSpell.randomBlockPositions(source, false, radius).filter(i -> {
            if (source.canModifyAt(i) && applyBlockSingle(source.asEntity(), source.asWorld(), i, situation)) {

                if (submerged & source.getOrigin().closerThan(i, RANGE.get(getTraits()) - 1)) {
                    BlockState state = source.asWorld().getBlockState(i);
                    if (state.is(BlockTags.ICE) || state.is(Blocks.OBSIDIAN)) {
                        source.asWorld().setBlock(i, Blocks.AIR.defaultBlockState(), Block.UPDATE_NEIGHBORS);
                    } else if (!state.getFluidState().isEmpty()) {
                        source.asWorld().setBlock(i, state.setValue(BlockStateProperties.WATERLOGGED, false), Block.UPDATE_NEIGHBORS);
                    }
                }

                ParticleUtils.spawnParticle(source.asWorld(), ParticleTypes.SPLASH, new Vec3(
                        i.getX() + source.asWorld().getRandom().nextFloat(),
                        i.getY() + 1,
                        i.getZ() + source.asWorld().getRandom().nextFloat()), Vec3.ZERO);

                return true;
            }

            return false;
        }).count();

        if (!source.subtractEnergyCost(Math.min(10, blocksAffected / 30))) {
            setDead();
        }

        return applyEntities(source, source.getOriginVector()) && situation == Situation.PROJECTILE && !isDead();
    }

    protected boolean applyEntities(Caster<?> source, Vec3 pos) {
        List<Entity> entities = VecHelper.findInRange(source.asEntity(), source.asWorld(), pos, 3);
        entities.forEach(entity -> applyEntitySingle(source, entity));
        return !entities.isEmpty();

    }

    protected void applyEntitySingle(Caster<?> source, Entity e) {
        if (source.asEntity() == e || source.isOwnedBy(e) || (e instanceof Owned<?> sibling && source.hasCommonOwner(sibling))) {
            return;
        }
        if (e instanceof PrimedTnt) {
            e.remove(RemovalReason.DISCARDED);
            e.level().setBlockAndUpdate(e.blockPosition(), Blocks.TNT.defaultBlockState());
        } else if (e.isOnFire()) {
            e.clearFire();
        } else if (!source.isClient()) {
            e.hurtServer(source.asServerWorld(), source.damageOf(DamageTypes.FREEZE, source), 2);
        }
    }

    private boolean applyBlockSingle(Entity owner, Level world, BlockPos pos, Situation situation) {
        BlockState state = world.getBlockState(pos);

        if ((situation == Situation.PROJECTILE && StateMaps.SNOW_PILED.convert(world, pos))
            || StateMaps.ICE_AFFECTED.convert(world, pos)) {
            return true;
        }

        if (world.loadedAndEntityCanStandOn(pos, owner)
                || (state.is(Blocks.SNOW) && state.getValue(SnowLayerBlock.LAYERS) == SnowLayerBlock.MAX_HEIGHT)
                || state.is(BlockTags.LEAVES)) {
            addSnowLayer(world, pos.above());
            return true;
        }

        if (state.is(BlockTags.ICE)
                && world.getRandom().nextInt(10) == 0
                && isSurroundedByIce(world, pos)) {
            world.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
            return true;
        }

        return false;
    }

    private static boolean isSurroundedByIce(Level w, BlockPos pos) {
        return PosHelper.adjacentNeighbours(pos).allMatch(i ->
            w.getBlockState(i).is(BlockTags.ICE)
        );
    }

    private static void addSnowLayer(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.isAir() || StatePredicate.Plants.INSTANCE.test(state)) {
            world.setBlock(pos, Blocks.SNOW.defaultBlockState(), 3);
        }
    }
}
