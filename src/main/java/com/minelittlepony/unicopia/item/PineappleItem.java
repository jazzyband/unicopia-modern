package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.util.RegistryUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;

public class PineappleItem extends Item {
    public PineappleItem(Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        if (consumable != null) {
            stack = consumable.onConsume(world, user, stack);
            if (!world.isClientSide()) {
                stack.hurtAndBreak(1, (ServerLevel)world, user instanceof ServerPlayer p ? p : null, i -> {
                    AwaitTickQueue.scheduleTask(world, w -> {
                        w.playSound(null, user, USounds.Vanilla.PLAYER_BURP, SoundSource.PLAYERS, 1, 0.7F);
                    });
                });

                if (world.getRandom().nextInt(20) == 0) {
                    RegistryUtils.pickRandomEntry(world, UTags.StatusEffects.PINEAPPLE_EFFECTS, e -> !user.hasEffect(e)).ifPresent(effect -> {
                        user.addEffect(new MobEffectInstance(effect, 10, 1));
                    });
                }
            }
        }
        return stack;
    }
}
