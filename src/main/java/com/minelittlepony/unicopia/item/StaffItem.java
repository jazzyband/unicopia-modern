package com.minelittlepony.unicopia.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import com.minelittlepony.unicopia.USounds;

public class StaffItem extends Item {
    public StaffItem(Properties settings) {
        super(settings.sword(ToolMaterial.WOOD, 2, -3));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level w = player.level();

        EntityDimensions dims = target.getDimensions(target.getPose());

        for (int i = 0; i < 130; i++) {
            w.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LOG.defaultBlockState()),
                    target.getX() + (target.level().getRandom().nextFloat() - 0.5F) * (dims.width() + 1),
                    (target.getY() + dims.height() / 2) + (target.level().getRandom().nextFloat() - 0.5F) * dims.height(),
                    target.getZ() + (target.level().getRandom().nextFloat() - 0.5F) * (dims.width() + 1),
                    0, 0, 0
            );
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        textConsumer.accept(Component.translatable(getDescriptionId() + ".lore").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity entity, LivingEntity attacker) {
        super.hurtEnemy(stack, entity, attacker);

        // TODO: Maybe swing?
        castContainedEffect(stack, entity, attacker);
    }

    protected boolean castContainedEffect(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.level().playSound(null, target.blockPosition(), USounds.ITEM_STAFF_STRIKE, attacker.getSoundSource(), 1, 1);

        target.knockback(attacker.getDeltaMovement().subtract(target.getDeltaMovement()).horizontalDistance(),
                Mth.sin(attacker.getYRot() * 0.017453292F),
               -Mth.cos(attacker.getYRot() * 0.017453292F),
               attacker.damageSources().mobAttack(attacker), 0
        );

        return true;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }
}
