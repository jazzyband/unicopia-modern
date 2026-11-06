package com.minelittlepony.unicopia.ability;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.TraceHelper;

/**
 * Hippogriff ability to use their beak as a weapon
 */
public class PeckAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 1;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 10;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 0;
    }

    @Override
    public boolean activateOnEarlyRelease() {
        return true;
    }

    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.INSTANCE;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Hit> getSerializer() {
        return Hit.CODEC;
    }

    protected LivingEntity findTarget(Player player, Level w) {
        return TraceHelper.<LivingEntity>findEntity(player, 5, 1, hit -> {
            return EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(hit) && !player.isPassengerOfSameVehicle(hit);
        }).orElse(null);
    }

    @Override
    public boolean apply(Pony player, Hit hit) {
        LivingEntity target = findTarget(player.asEntity(), player.asWorld());

        playSounds(player, player.asWorld().getRandom(), 1);

        if (target != null) {
            spookMob(player, target, 1);
        } else {
            BlockPos pos = TraceHelper.findBlock(player.asEntity(), 5, 1).orElse(BlockPos.ZERO);
            if (pos != BlockPos.ZERO) {
                BlockState state = player.asWorld().getBlockState(pos);
                if (state.canBeReplaced()) {
                    player.asWorld().destroyBlock(pos, true);
                } else if (state.is(BlockTags.DIRT) || player.asWorld().getRandom().nextInt(40000) == 0) {
                    player.asWorld().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
                    pos = pos.above();
                    Level world = player.asWorld();

                    if (world instanceof ServerLevel sw) {
                        for (ItemStack stack : Block.getDrops(state, sw, pos, null)) {
                            if (Block.byItem(stack.getItem()) == Blocks.AIR) {
                                Block.popResource(world, pos, stack);
                            }
                        }
                        state.spawnAfterBreak(sw, pos, ItemStack.EMPTY, true);

                        if (world.getRandom().nextInt(20) == 0) {
                            world.destroyBlock(pos.below(), false);
                            player.asEntity().sendOverlayMessage(Component.translatable("ability.unicopia.peck.block.fled"));
                        }
                    }
                } else {
                    player.asEntity().sendOverlayMessage(Component.translatable("ability.unicopia.peck.block.unfased"));
                }
            }
        }

        return true;
    }

    protected void playSounds(Pony player, RandomSource rng, float strength) {
        player.getMagicalReserves().getExertion().addPercent(100);
        player.getMagicalReserves().getEnergy().addPercent(10);
        player.playSound(USounds.Vanilla.CHICKEN_HURT_BABY.value(),
                1,
                0.9F + (rng.nextFloat() - 0.5F)
        );
        player.asWorld().gameEvent(player.asEntity(), GameEvent.STEP, player.asEntity().getEyePosition());
    }

    protected void spookMob(Pony player, LivingEntity living, float strength) {
        boolean isEarthPony = EquinePredicates.PLAYER_EARTH.test(living);
        boolean isBracing = isEarthPony && player.asEntity().isShiftKeyDown();

        if (!isBracing && living.level() instanceof ServerLevel sw) {
            living.hurtServer(sw, player.damageOf(UDamageTypes.BAT_SCREECH, player), isEarthPony ? 0.1F : 0.3F);
        }

        Vec3 knockVec = player.getOriginVector().subtract(living.position()).scale(strength);
        living.knockback((isBracing ? 0.2F : isEarthPony ? 0.3F : 0.5F) * strength, knockVec.x(), knockVec.z(),
                player.asWorld().damageSources().mobAttack(player.asEntity()), 0);
        if (!isEarthPony) {
            living.push(0, 0.1 * strength, 0);
        }
        Living.updateVelocity(living);
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
    }
}
