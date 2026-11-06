package com.minelittlepony.unicopia.item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.util.Dispensable;

public class BasketItem extends Item implements Dispensable {
    private static final Predicate<Entity> RIDERS = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
    private static final double REACH = 5;

    private final AirBalloonEntity.BasketType type;

    public static final Map<AirBalloonEntity.BasketType, BasketItem> REGISTRY = new HashMap<>();

    public BasketItem(AirBalloonEntity.BasketType type, Item.Properties settings) {
        super(settings);
        this.type = type;
        DispenserBlock.registerBehavior(this, createDispenserBehaviour());
        REGISTRY.put(type, this);
    }

    @Override
    public InteractionResult dispenseStack(BlockSource source, ItemStack stack) {
        Direction facing = source.state().getValue(DispenserBlock.FACING);
        BlockPos pos = source.pos().relative(facing);
        float yaw = facing.getOpposite().toYRot();
        return placeEntity(stack, source.level(), pos.getX(), pos.getY(), pos.getZ(), yaw, null);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        BlockHitResult hit = BoatItem.getPlayerPOVHitResult(world, user, ClipContext.Fluid.ANY);

        if (hit.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }

        Vec3 eyePos = user.getEyePosition();
        if (world.getEntities(user, user.getBoundingBox().expandTowards(user.getViewVector(1).scale(REACH)).inflate(1), RIDERS).stream()
                .anyMatch(entity -> entity.getBoundingBox().inflate(entity.getPickRadius()).contains(eyePos))) {
            return InteractionResult.PASS;
        }

        if (hit.getType() == HitResult.Type.BLOCK) {
            return placeEntity(stack, world, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, user.getDirection().toYRot(), user);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult placeEntity(ItemStack stack, Level world, double x, double y, double z, float yaw, @Nullable Player user) {
        AirBalloonEntity entity = UEntities.AIR_BALLOON.create(world, EntitySpawnReason.SPAWN_ITEM_USE);
        yaw += 180;
        entity.absSnapTo(x, y, z, yaw, 0);
        entity.setYHeadRot(yaw);
        entity.setYBodyRot(yaw);
        entity.setYRot(yaw);
        entity.setBasketType(type);
        if (!world.noCollision(entity, entity.getBoundingBox())) {
            return InteractionResult.FAIL;
        }
        if (!world.isClientSide()) {
            world.addFreshEntity(entity);
            if (user != null) {
                user.awardStat(Stats.ITEM_USED.get(this));
            }
            if (user == null || !user.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}