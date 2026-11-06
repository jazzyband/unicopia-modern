package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.item.component.Appearance;
import com.minelittlepony.unicopia.item.component.BufferflyVariantComponent;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.EntityHitResult;

public class FilledJarItem extends ProjectileItem implements ProjectileDelegate.HitListener {
    public FilledJarItem(Item.Properties settings) {
        super(settings, 0);
    }

    @Override
    public SoundEvent getThrowSound(ItemStack stack) {
        return USounds.ENTITY_JAR_THROW;
    }

    @Override
    public Component getName(ItemStack stack) {
        Appearance appearance = stack.get(UDataComponentTypes.APPEARANCE);
        return appearance != null ? Component.translatable(getDescriptionId(), appearance.item().getHoverName()) : UItems.EMPTY_JAR.getName(UItems.EMPTY_JAR.getDefaultInstance());
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        Entity entity = hit.getEntity();

        if (!entity.isAttackable() || !(projectile instanceof ItemSupplier)) {
            return;
        }

        ItemStack stack = Appearance.upwrapAppearance(((ItemSupplier)projectile).getItem());

        boolean onFire = false;

        if (projectile.level() instanceof ServerLevel world) {
            DamageSource damageSource = entity.damageSources().thrown(projectile, projectile.getOwner());

            float damage = EnchantmentHelper.modifyDamage(world, stack, entity, damageSource, projectile.getThrowDamage());

            if (projectile.getOwner() instanceof LivingEntity owner) {
                owner.setLastHurtMob(entity);
            }

            if (entity.hurtServer(world, damageSource, damage)) {

                if (entity instanceof LivingEntity living) {
                    projectile.knockback(living, damageSource, stack);
                    EnchantmentHelper.doPostAttackEffectsWithItemSource(world, living, damageSource, stack);
                }
            } else {
                if (onFire) {
                    entity.setSharedFlagOnFire(false);
                }
                projectile.deflect(ProjectileDeflection.REVERSE, entity,
                        projectile.getOwner() == null ? null : net.minecraft.world.entity.EntityReference.of(projectile.getOwner()), false, 1);
                projectile.setDeltaMovement(projectile.getDeltaMovement().scale(0.2));
            }
        }
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile) {
        ItemStack stack = Appearance.upwrapAppearance(projectile.getItem());
        BufferflyVariantComponent butterflyVariant = stack.get(UDataComponentTypes.BUTTERFLY_VARIANT);

        if (projectile.level() instanceof ServerLevel sw) {
            if (butterflyVariant != null) {
                projectile.level().addFreshEntity(UEntities.BUTTERFLY.create(sw, b -> {
                    b.setVariant(butterflyVariant.variant());
                    b.absSnapTo(projectile.getX(), projectile.getY(), projectile.getZ());
                }, projectile.blockPosition(), EntitySpawnReason.EVENT, false, false));
            } else {
                stack.hurtAndBreak(1, sw, null, i -> {});
                projectile.spawnAtLocation(sw, stack);
            }
        }
        projectile.level().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, projectile.blockPosition(), Block.getId(Blocks.GLASS.defaultBlockState()));
    }
}
