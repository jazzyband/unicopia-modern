package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.ability.EarthPonyGrowAbility.Growable;
import com.minelittlepony.unicopia.entity.mob.IgnominiousBulbEntity;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CuringJokeBlock extends FlowerBlock implements Growable {

    public CuringJokeBlock(Holder<MobEffect> suspiciousStewEffect, int effectDuration, Properties settings) {
        super(suspiciousStewEffect, effectDuration, settings);
    }

    protected CuringJokeBlock(SuspiciousStewEffects effects, Properties settings) {
        super(effects, settings);
    }


    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 3; i++) {
            ParticleUtils.spawnParticleBelow(world, pos, random, new MagicParticleEffect(0x3388EE));
        }
    }

    @Override
    public boolean grow(Level world, BlockState state, BlockPos pos) {
        var otherFlowers = com.minelittlepony.unicopia.util.PosHelper.withinManhattanStream(pos, 16, 16, 16)
                .filter(p -> world.getBlockState(p).is(this))
                .map(BlockPos::immutable)
                .toList();

        IgnominiousBulbEntity bulb = new IgnominiousBulbEntity(world);
        bulb.setBaby(true);
        bulb.absSnapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);

        if (DismountHelper.canDismountTo(world, bulb, bulb.getBoundingBox())) {
            otherFlowers.forEach(p -> world.destroyBlock(p, false));
            world.addFreshEntity(bulb);
            return true;
        }

        return false;
    }
}
