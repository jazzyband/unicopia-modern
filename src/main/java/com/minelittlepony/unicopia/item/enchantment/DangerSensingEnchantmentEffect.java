package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.entity.player.Pony;

public class DangerSensingEnchantmentEffect implements EnchantmentEntityEffect {
    public static final DangerSensingEnchantmentEffect INSTANCE = new DangerSensingEnchantmentEffect();
    public static final MapCodec<DangerSensingEnchantmentEffect> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<DangerSensingEnchantmentEffect> codec() {
        return CODEC;
    }

    @Override
    public void apply(ServerLevel world, int level, EnchantedItemInUse context, Entity entity, Vec3 pos) {
        Pony.of(entity).ifPresent(pony -> {
            if (pony.asEntity().tickCount % 10 == 0) {
                int range = (level + 1) * 3;
                if (pony.asWorld().getEntitiesOfClass(Monster.class, pony.asEntity().getBoundingBox().inflate(range, 0, range), enemy -> {
                    return enemy != null
                            && enemy.canAttack(pony.asEntity())
                            && enemy.hasLineOfSight(pony.asEntity())
                            && enemy.getTarget() == pony.asEntity();
                }).isEmpty()) {
                    return;
                }

                Bar bar = pony.getMagicalReserves().getEnergy();
                float targetPercent = (level / (float)pony.entryFor(UEnchantments.STRESSED).value().definition().maxLevel()) * 0.05125F;
                float increase = 1F + (level * level)/100F;
                if (bar.getPercentFill() < targetPercent) {
                    bar.add(increase);
                }
            }
        });

    }
}
