package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public class SilverfishBehaviour extends EntityBehaviour<Silverfish> {
    @Override
    public void update(Pony player, Silverfish entity, Disguise spell) {
        if (!player.isClient() && player.sneakingChanged() && player.asEntity().isShiftKeyDown()) {
            BlockPos pos = entity.blockPosition().below();
            BlockState state = entity.level().getBlockState(pos);

            if (StateMaps.SILVERFISH_AFFECTED.convert(entity.level(), pos)) {
                entity.level().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            }
        }
    }
}
