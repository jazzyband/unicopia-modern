package com.minelittlepony.unicopia.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import com.minelittlepony.unicopia.item.ItemDuck;

@Mixin(Item.class)
abstract class MixinItem implements ItemDuck {
    private final List<GroundTickCallback> tickCallbacks = new ArrayList<>();

    @Override
    public List<GroundTickCallback> getCallbacks() {
        return tickCallbacks;
    }
}
