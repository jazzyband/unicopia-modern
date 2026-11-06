package com.minelittlepony.unicopia.item;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.minelittlepony.unicopia.block.FancyBedBlock;
import com.minelittlepony.unicopia.block.cloud.CloudBedBlock;

public class BedsheetsItem extends Item {
    public static final Map<CloudBedBlock.SheetPattern, Item> ITEMS = new HashMap<>();

    private final CloudBedBlock.SheetPattern pattern;

    public static Item forPattern(CloudBedBlock.SheetPattern pattern) {
        return ITEMS.getOrDefault(pattern, Items.AIR);
    }

    public BedsheetsItem(CloudBedBlock.SheetPattern pattern, Properties settings) {
        super(settings);
        this.pattern = pattern;
        ITEMS.put(pattern, this);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() instanceof FancyBedBlock) {
            FancyBedBlock.setBedPattern(world, context.getClickedPos(), pattern);
            context.getItemInHand().shrink(1);
            Player player = context.getPlayer();
            world.playSound(player, pos, SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.BLOCKS, 1, 1);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
