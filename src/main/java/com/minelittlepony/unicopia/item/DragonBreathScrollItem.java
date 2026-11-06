package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.server.world.DragonBreathStore;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DragonBreathScrollItem extends Item {

    public DragonBreathScrollItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack payload = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);

        if (payload.isEmpty() || !stack.has(DataComponents.CUSTOM_NAME)) {
            return InteractionResult.FAIL;
        }

        ItemStack scroll = stack.consumeAndReturn(1, player);
        if (!world.isClientSide()) {
            String recipient = scroll.get(DataComponents.CUSTOM_NAME).getString();
            UCriteria.SEND_DRAGON_BREATH.triggerSent(player, payload, recipient, (counterName, count) -> {
               if (count == 1 && "dings_on_celestias_head".equals(counterName)) {
                   UnicopiaWorldProperties properties = UnicopiaWorldProperties.forWorld((ServerLevel)world);
                   properties.setTangentalSkyAngle(properties.getTangentalSkyAngle() + 15);
                   world.playSound(null, player.blockPosition(), USounds.Vanilla.ANVIL_LAND, SoundSource.NEUTRAL, 0.2F, world.getRandom().triangle(1, 0.2F));
               }
            });
            DragonBreathStore.get(world).put(recipient, payload.split(1));
        }
        player.playSound(USounds.ITEM_DRAGON_BREATH_SCROLL_USE, 1, 1);
        return InteractionResult.CONSUME;
    }
}
