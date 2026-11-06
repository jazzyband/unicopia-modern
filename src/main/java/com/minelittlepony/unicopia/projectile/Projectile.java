package com.minelittlepony.unicopia.projectile;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.SoundEmitter;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public interface Projectile extends ItemLike, ProjectileItem {
    ProjectileItem.DispenseConfig SETTINGS = ProjectileItem.DispenseConfig
            .builder()
            .uncertainty(0)
            .build();

    static void makeDispensable(Projectile projectile) {
        DispenserBlock.registerBehavior(projectile.asItem(), new ProjectileDispenseBehavior(projectile.asItem()));
    }

    @Override
    default net.minecraft.world.entity.projectile.Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction) {
        net.minecraft.world.entity.projectile.Projectile entity = createProjectile(stack.copyWithCount(1), world, null);
        entity.setPos(pos.x(), pos.y(), pos.z());
        if (entity instanceof AbstractArrow p) {
            p.pickup = AbstractArrow.Pickup.ALLOWED;
        }
        return entity;
    }

    @Override
    default ProjectileItem.DispenseConfig createDispenseConfig() {
        return SETTINGS;
    }

    default InteractionResult triggerThrow(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide()) {
            SoundEmitter.playSoundAt(player,
                    getThrowSound(stack), SoundSource.NEUTRAL,
                    0.5F,
                    0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

            ItemStack projectileStack = stack.consumeAndReturn(1, player);
            if (player.isCreative()) {
                projectileStack.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
            }

            world.addFreshEntity(createProjectile(projectileStack, world, player));
        }

        player.awardStat(Stats.ITEM_USED.get(asItem()));

        if (!player.isCreative()) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(stack);
    }

    default net.minecraft.world.entity.projectile.Projectile createProjectile(ItemStack stack, Level world, @Nullable Player player) {
        MagicProjectileEntity projectile = player == null ? new MagicProjectileEntity(world) : new MagicProjectileEntity(world, player);
        projectile.setItem(stack);
        projectile.setThrowDamage(getProjectileDamage(stack));
        projectile.setMaxAge(-1);
        if (player != null) {
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 1.5F, 1);
        }
        return projectile;
    }

    SoundEvent getThrowSound(ItemStack stack);

    float getProjectileDamage(ItemStack stack);
}
