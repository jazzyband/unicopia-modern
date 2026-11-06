package com.minelittlepony.unicopia.item;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.minelittlepony.unicopia.entity.mob.SpellbookEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.server.world.Altar;
import com.minelittlepony.unicopia.util.Dispensable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public class SpellbookItem extends Item implements Dispensable {
    public SpellbookItem(Properties settings) {
        super(settings);
        DispenserBlock.registerBehavior(this, createDispenserBehaviour());
    }

    @Override
    public InteractionResult dispenseStack(BlockSource pointer, ItemStack stack) {
        Direction facing = pointer.state().getValue(DispenserBlock.FACING);
        Position pos = DispenserBlock.getDispensePosition(pointer);

        float yaw = facing.getOpposite().toYRot();
        if (placeBook(stack, pointer.level(), pos, yaw, null)) {
            stack.shrink(1);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        @Nullable
        Player player = context.getPlayer();

        if (!context.getLevel().isClientSide()) {
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

            if (placeBook(context.getItemInHand(), context.getLevel(), Vec3.atBottomCenterOf(pos), context.getRotation() + 180, player)) {
                if (!player.getAbilities().instabuild) {
                    player.getItemInHand(context.getHand()).shrink(1);
                }

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private static boolean placeBook(ItemStack stack, Level world, Position pos, float yaw, @Nullable Entity placer) {
        SpellbookEntity book = UEntities.SPELLBOOK.create(world, EntitySpawnReason.MOB_SUMMONED);

        book.snapTo(pos.x(), pos.y(), pos.z(), 0, 0);
        book.setYHeadRot(yaw);
        book.setYRot(yaw);

        if (!book.checkSpawnObstruction(world)) {
            return false;
        }

        @Nullable
        SpellbookState state = stack.get(UDataComponentTypes.SPELLBOOK_STATE);
        if (state != null) {
            book.setSpellbookState(state);
        }

        world.addFreshEntity(book);

        Altar.locateAltar(world, book.blockPosition()).ifPresent(altar -> {
            book.setAltar(altar);
            altar.generateDecorations(world);
            UCriteria.LIGHT_ALTAR.trigger(placer);
        });

        return true;
    }
}















