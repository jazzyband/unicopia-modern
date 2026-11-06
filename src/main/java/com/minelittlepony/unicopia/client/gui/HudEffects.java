package com.minelittlepony.unicopia.client.gui;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.DeltaTracker;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.entity.player.Pony;

public class HudEffects {

    private static boolean addedHunger;
    private static Player invertedWaterPlayer;

    public static void tryApply(@Nullable Player player, DeltaTracker tickCounter, boolean on) {
        if (player != null) {
            apply(Pony.of(player), tickCounter.getGameTimeDeltaPartialTick(false), on);
        }
    }

    public static boolean shouldInvertWater(Player player) {
        return invertedWaterPlayer == player;
    }

    private static void apply(Pony pony, float tickDelta, boolean on) {
        if (on) {
            if (!pony.asEntity().hasEffect(MobEffects.HUNGER) && pony.asEntity().hasEffect(UEffects.FOOD_POISONING)) {
                addedHunger = true;
                pony.asEntity().addEffect(new MobEffectInstance(MobEffects.HUNGER, 1, 1, false, false));
            }
        } else {
            if (addedHunger) {
                addedHunger = false;
                pony.asEntity().removeEffect(MobEffects.HUNGER);
            }
        }

        if (pony.getCompositeRace().includes(Race.SEAPONY)) {
            invertedWaterPlayer = on ? pony.asEntity() : null;
        }

    }
}
