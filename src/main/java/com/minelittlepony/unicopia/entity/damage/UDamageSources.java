package com.minelittlepony.unicopia.entity.damage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.WorldConvertable;
import com.minelittlepony.unicopia.ability.magic.Caster;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface UDamageSources extends WorldConvertable {

    static UDamageSources of(Level world) {
        return () -> world;
    }

    private static Holder<DamageType> entryOf(WorldConvertable world, ResourceKey<DamageType> type) {
        return world.asWorld().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).get(type.identifier()).orElseThrow();
    }

    default MagicalDamageSource damageOf(ResourceKey<DamageType> type) {
        return new MagicalDamageSource(entryOf(this, type), null);
    }

    default MagicalDamageSource damageOf(ResourceKey<DamageType> type, @Nullable LivingEntity source) {
        return new MagicalDamageSource(entryOf(this, type), source, null);
    }

    default MagicalDamageSource damageOf(ResourceKey<DamageType> type, @NotNull Caster<?> caster) {
        return new MagicalDamageSource(entryOf(this, type), caster.asEntity(), caster.getMaster(), caster);
    }
}
