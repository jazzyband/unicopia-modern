package com.minelittlepony.unicopia.item;

import java.util.Arrays;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity.BalloonDesign;
import com.minelittlepony.unicopia.item.component.BalloonDesignComponent;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.item.group.MultiItem;

public class GiantBalloonItem extends Item implements MultiItem {
    public GiantBalloonItem(Properties settings) {
        super(settings);
    }

    @Override
    public List<ItemStack> getDefaultStacks() {
        return Arrays.stream(BalloonDesign.VALUES).map(design -> {
            ItemStack stack = getDefaultInstance();
            stack.set(UDataComponentTypes.BALLOON_DESIGN, new BalloonDesignComponent(design, true));
            return stack;
        }).toList();
    }
}
