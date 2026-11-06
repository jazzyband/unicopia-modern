package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.projectile.Projectile;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

abstract class ProjectileItem extends Item implements Projectile {

    private final float projectileDamage;

    public ProjectileItem(Item.Properties settings, float projectileDamage) {
        super(settings);
        this.projectileDamage = projectileDamage;
        Projectile.makeDispensable(this);
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        if (!player.isSecondaryUseActive()) {
            InteractionResult result = super.use(world, player, hand);
            if (result.consumesAction()) {
                return result;
            }
        }

        return triggerThrow(world, player, hand);
    }

    @Override
    public float getProjectileDamage(ItemStack stack) {
        return projectileDamage;
    }
}
