package com.minelittlepony.unicopia.item.enchantment;

import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.util.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ConsumptionEnchantmentUtil {
    public static boolean applyConsumption(Level w, BlockState state, BlockPos pos, @Nullable BlockEntity blockEntity, Entity entity, ItemStack tool) {

        if (!(w instanceof ServerLevel world)) {
            return false;
        }

        if (tool.isEmpty() && entity instanceof LivingEntity l) {
            tool = l.getMainHandItem();
        }

        if (!EnchantmentHelper.hasTag(tool, UTags.Enchantments.CONVERTS_DROPS_TO_XP)) {
            return false;
        }

        Block.getDrops(state, world, pos, blockEntity, entity, tool).forEach(s -> {
            world.playSound(null, pos, USounds.ENCHANTMENT_CONSUMPTION_CONSUME, SoundSource.BLOCKS, 0.05F, (float)world.getRandom().triangle(0.6F, 0.2F));
            ExperienceOrb.award(world, Vec3.atCenterOf(pos).add(VecHelper.triangular(world.getRandom(), 0, 0.3)), s.getCount());
            UCriteria.USE_CONSUMPTION.trigger(entity);
        });
        state.spawnAfterBreak(world, pos, tool, true);

        return true;
    }
}
