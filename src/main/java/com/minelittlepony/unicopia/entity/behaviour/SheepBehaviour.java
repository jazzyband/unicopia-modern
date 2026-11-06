package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public class SheepBehaviour extends EntityBehaviour<Sheep> {
    @Override
    public void update(Pony player, Sheep entity, Disguise spell) {

        if (player.sneakingChanged()) {

            BlockPos pos = entity.blockPosition().below();
            BlockState state = entity.level().getBlockState(pos);
            boolean grass = state.is(Blocks.GRASS_BLOCK);

            if (player.asEntity().isShiftKeyDown()) {
                if (grass && entity.level().isClientSide() && entity.isSheared()) {
                    entity.handleEntityEvent((byte)10);
                }
            } else {
                if (entity.isSheared() && grass) {
                    entity.level().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
                    entity.level().setBlock(pos, Blocks.DIRT.defaultBlockState(), 2);

                    entity.ate();
                } else if (!entity.isSheared()) {
                    DyeColor color = entity.getColor();
                    Item wool = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(color.getSerializedName() + "_wool"));
                    if (wool == null) {
                        return;
                    }

                    ItemStack dropType = new ItemStack(wool);

                    player.asEntity().playSound(USounds.Vanilla.SHEEP_SHEAR, 1, 1);
                    entity.setSheared(true);

                    RandomSource rng = entity.level().getRandom();
                    Inventory inv = player.asEntity().getInventory();

                    int dropAmount = rng.nextInt(3);
                    int slot;

                    do {
                        slot = inv.findSlotMatchingCraftingIngredient(dropType.getItem().builtInRegistryHolder(), dropType);

                        if (slot < 0) {
                            break;
                        }
                        inv.removeItem(slot, 1);
                        ItemEntity itemEntity = entity.spawnAtLocation(player.asServerWorld(), new net.minecraft.world.item.ItemStack(dropType.getItem()), net.minecraft.world.phys.Vec3.ZERO);
                        if (itemEntity != null) {
                           itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(
                                   (rng.nextFloat() - rng.nextFloat()) * 0.1F,
                                   rng.nextFloat() * 0.05F,
                                   (rng.nextFloat() - rng.nextFloat()) * 0.1F
                           ));
                           itemEntity.setPickUpDelay(40);
                        }
                    } while (dropAmount-- > 0);
                }
                spell.getOptionalAppearance().ifPresent(EntityAppearance::markDirty);
            }
        }
    }
}
