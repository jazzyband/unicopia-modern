package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.FallLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Mixin(DamageSource.class)
abstract class MixinDamageSource {
    @ModifyReturnValue(method = "getLocalizedDeathMessage", at = @At("RETURN"))
    private Component onGetDeathMessage(Component message, LivingEntity entity) {
        final DamageSource self = (DamageSource)(Object)this;

        return Pony.of(entity).filter(e -> e.getCompositeRace().canFly()).map(pony -> {
            if (pony.getPhysics().isFlying()) {
                return Component.translatable("death.attack.unicopia.generic.whilst_flying", message);
            }
            return (Component)null;
        }).or(() -> Living.getOrEmpty(entity).map(Living::getAttacker).map(attacker -> {
            Entity prime = entity.getKillCredit();
            if (prime != null && !attacker.isOwnedBy(prime)) {
                return Component.translatable("death.attack.unicopia.generic.and_also", message, attacker.asEntity().getDisplayName());
            }

            String name = self.getMsgId();
            if (!name.endsWith(".player")) {
                return Component.translatable("death.attack." + name + ".player", entity.getDisplayName(), attacker.asEntity().getDisplayName());
            }

            return (Component)null;
        })).orElse(message);
    }
}

@Mixin(FallLocation.class)
abstract class MixinFallLocation {
    @ModifyReturnValue(method = "getCurrentFallLocation", at = @At("RETURN"))
    private static FallLocation onFromEntity(FallLocation location, LivingEntity entity) {
        return location == null ? null : Pony.of(entity).map(pony -> {
            if (pony.getCompositeRace().canFly()) {
                return new FallLocation(location.id() + ".pegasus");
            }
            return null;
        }).orElse(location);
    }
}
