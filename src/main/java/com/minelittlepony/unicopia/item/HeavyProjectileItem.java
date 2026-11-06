package com.minelittlepony.unicopia.item;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.projectile.PhysicsBodyProjectileEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HeavyProjectileItem extends ProjectileItem {
    public HeavyProjectileItem(Item.Properties settings, float projectileDamage) {
        super(settings, projectileDamage);
    }

    @Override
    public PhysicsBodyProjectileEntity createProjectile(ItemStack stack, Level world, @Nullable Player player) {
        PhysicsBodyProjectileEntity projectile = player == null
                ? new PhysicsBodyProjectileEntity(world, stack)
                : new PhysicsBodyProjectileEntity(world, player, stack);
        if (player != null) {
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 1.5F, 1);
        }
        return projectile;
    }

    @Override
    public SoundEvent getThrowSound(ItemStack stack) {
        return USounds.ENTITY_JAR_THROW;
    }
}
