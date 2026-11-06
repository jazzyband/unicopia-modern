package com.minelittlepony.unicopia.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.server.world.BlockDestructionManager;

public class ForageableItem extends Item {
    private static final List<ForageableItem> REGISTRY = new ArrayList<>();

    public static InteractionResult use(Player player, ItemStack stack, Level world, InteractionHand hand, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!stack.is(ItemTags.HOES)) {
            return InteractionResult.PASS;
        }

        InteractionResult result = InteractionResult.PASS;

        if (state.is(BlockTags.LEAVES)) {
            player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
            world.playSound(player, pos, state.getSoundType().getHitSound(), SoundSource.BLOCKS);
            InteractionManager.getInstance().addBlockBreakingParticles(pos, hit.getDirection());

            float foragingChance = getForagingChance(stack);

            for (ForageableItem item : REGISTRY) {
                if ((result = item.onTryForage(world, pos, state, stack, player, foragingChance)).consumesAction()) {
                    stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
                    return result;
                }
            }
        }

        return result.consumesAction() ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private final Supplier<Block> targetBlock;

    public ForageableItem(Properties settings, Supplier<Block> targetBlock) {
        super(settings);
        this.targetBlock = targetBlock;
        REGISTRY.add(this);
    }

    public InteractionResult onTryForage(Level world, BlockPos pos, BlockState state, ItemStack stack, Player player, float spawnChance) {
        if (!state.is(targetBlock.get())) {
            return InteractionResult.PASS;
        }

        spawnChance -= EnchantmentUtil.getLuck(1, player);

        if (spawnChance <= 0 || world.getRandom().nextInt((int)(spawnChance * 32)) == 0) {
            Block.popResource(world, pos, new ItemStack(this, 1 + EnchantmentHelper.getEnchantmentLevel(player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.LOOTING.identifier()).get(), player)));
            world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            if (BlockDestructionManager.of(world).damageBlock(pos, world.getRandom().nextIntBetweenInclusive(3, 7)) >= BlockDestructionManager.MAX_DAMAGE) {
                world.destroyBlock(pos, true);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    static final Map<Item, Float> SPAWN_CHANCES = Map.of(
            Items.WOODEN_HOE, 0.25F,
            Items.STONE_HOE, 0.3F,
            Items.IRON_HOE, 0.4F,
            Items.GOLDEN_HOE, 0.6F,
            Items.DIAMOND_HOE, 0.7F,
            Items.NETHERITE_HOE, 0.8F
    );

    static float getForagingChance(ItemStack stack) {
        if (!stack.is(ItemTags.HOES)) {
            return 0.25F;
        }

        float spawnChance = SPAWN_CHANCES.getOrDefault(stack.getItem(), -1F);
        if (spawnChance <= 0) {
            Tool comp = stack.get(DataComponents.TOOL);
            if (comp == null) {
                return 0.25F;
            }
            spawnChance = Mth.clamp(comp.defaultMiningSpeed(), 0, 1);
        }

        return 1F - spawnChance;
    }
}
