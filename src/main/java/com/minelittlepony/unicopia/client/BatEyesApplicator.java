package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.EquinePredicates;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class BatEyesApplicator {

    public static final BatEyesApplicator INSTANCE = new BatEyesApplicator();

    private boolean batEyesApplied;

    private final Minecraft client = Minecraft.getInstance();

    public static float getWorldBrightness(float initial, LivingEntity entity, float tickDelta) {
        if (EquinePredicates.PLAYER_BAT.test(entity)) {
            return 0.6F;
        }
        return initial;
        /*StatusEffectInstance statusEffectInstance = entity.getStatusEffect(StatusEffects.NIGHT_VISION);
        return !statusEffectInstance.isDurationBelow(200)
            ? 1.0F
            : 0.7F + MathHelper.sin(((float)statusEffectInstance.getDuration() - tickDelta) * (float) Math.PI * 0.2F) * 0.3F;*/
    }

    public void enable() {
        if (client.level != null && client.player != null) {
            if (!client.player.hasEffect(MobEffects.NIGHT_VISION) && EquinePredicates.PLAYER_BAT.test(client.player)) {
                client.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 1, false, false));
                batEyesApplied = true;
            }
        }
    }

    public void disable() {
        if (batEyesApplied) {
            client.player.removeEffect(MobEffects.NIGHT_VISION);
            batEyesApplied = false;
        }
    }

}
