package com.minelittlepony.unicopia.compat.ad_astra;

import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface OxygenApi {
    AtomicReference<OxygenApi> API = new AtomicReference<>((world, pos) -> false);

    boolean hasOxygen(Level world, BlockPos pos);
}
