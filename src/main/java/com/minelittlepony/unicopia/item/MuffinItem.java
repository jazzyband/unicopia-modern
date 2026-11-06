package com.minelittlepony.unicopia.item;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.projectile.PhysicsBodyProjectileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MuffinItem extends HeavyProjectileItem {
    public MuffinItem(Item.Properties settings, float projectileDamage) {
        super(settings, projectileDamage);
    }

    @Override
    public PhysicsBodyProjectileEntity createProjectile(ItemStack stack, Level world, @Nullable Player player) {
        PhysicsBodyProjectileEntity projectile = super.createProjectile(stack, world, player);
        projectile.setBouncy();
        projectile.setBaseDamage(0);
        UCriteria.THROW_MUFFIN.trigger(player);
        return projectile;
    }
}
