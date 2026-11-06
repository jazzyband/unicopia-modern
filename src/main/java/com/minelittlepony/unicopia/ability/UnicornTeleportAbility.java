package com.minelittlepony.unicopia.ability;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.block.state.StatePredicate;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.Trace;

/**
 * Unicorn teleport ability
 */
public class UnicornTeleportAbility implements Ability<Pos> {

    @Override
    public Identifier getIcon(Pony player) {
        return getId().withPath(p -> "textures/gui/ability/" + p + (player.asEntity().isShiftKeyDown() ? "_far" : "_near") + ".png");
    }

    @Override
    public Component getName(Pony player) {
        if (player.asEntity().isShiftKeyDown()) {
            return Component.translatable(getTranslationKey() + ".far");
        }
        return Ability.super.getName(player);
    }

    @Override
    public int getWarmupTime(Pony player) {
        return (int)(20 - Math.min(17F, player.getLevel().get() * 0.75F));
    }

    @Override
    public int getCooldownTime(Pony player) {
        return (int)(50 - Math.min(45F, player.getLevel().get() * 0.75F));
    }

    @Override
    public double getCostEstimate(Pony player) {
        return prepare(player).map(pos -> pos.distanceTo(player) / 10).orElse(0D);
    }

    @Override
    public int getColor(Pony player) {
        return SpellType.PORTAL.getColor();
    }

    @Override
    public Optional<Pos> prepare(Pony player) {

        if (!player.canCast()) {
            return Optional.empty();
        }

        int maxDistance = (int)((player.asEntity().isCreative() ? 1000 : 100) + (player.getLevel().get() * 0.25F));

        Level w = player.asWorld();

        Trace trace = Trace.create(player.asEntity(), maxDistance, 1, EntitySelector.NO_SPECTATORS);
        return trace.getBlockOrEntityPos().map(pos -> {
            final BlockPos originalPos = pos;

            Direction globalUp = player.getPhysics().isGravityNegative() ? Direction.DOWN : Direction.UP;

            boolean originalPosHasSupport = exception(w, pos, player.asEntity());
            boolean originalPosValid = enterable(w, pos.relative(globalUp)) && enterable(w, pos.relative(globalUp, 2));

            if (w.getBlockState(pos).is(Blocks.POWDER_SNOW) && !PowderSnowBlock.canEntityWalkOnPowderSnow(player.asEntity())) {
                return null;
            }

            if (originalPosHasSupport) {
                final BlockPos p = pos;
                // offset to adjacent
                pos = trace.getSide().map(sideHit -> {
                    if (player.asEntity().isShiftKeyDown()) {
                        sideHit = sideHit.getOpposite();
                    }

                    return p.relative(sideHit);
                }).orElse(pos);
            }

            if (pos.getX() != originalPos.getX() || pos.getZ() != originalPos.getZ()) {
                // check support
                int steps = 0;
                while (enterable(w, pos.relative(globalUp.getOpposite()))) {
                    pos = pos.relative(globalUp.getOpposite());
                    if (++steps > 2) {
                        if (originalPosValid) {
                            pos = originalPos.relative(globalUp);
                            break;
                        } else {
                            return null;
                        }
                    }
                }
            }

            if ((!enterable(w, pos) && exception(w, pos, player.asEntity()))
             || (!enterable(w, pos.relative(globalUp)) && exception(w, pos.relative(globalUp), player.asEntity()))) {
                return null;
            }

            return new Pos(pos);
        });
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Pos> getSerializer() {
        return Pos.CODEC;
    }

    @Override
    public boolean apply(Pony iplayer, Pos data) {
        return teleport(iplayer, iplayer, data);
    }

    protected boolean teleport(Pony teleporter, Caster<?> teleportee, Pos destination) {

        if (!teleporter.canCast()) {
            return false;
        }

        Entity participant = teleportee.asEntity();

        if (participant == null) {
            return false;
        }

        teleportee.asWorld().playSound(null, teleportee.getOrigin(), USounds.ENTITY_PLAYER_UNICORN_TELEPORT, SoundSource.PLAYERS, 1, 1);

        double distance = destination.distanceTo(teleportee) / 10;

        if (participant.isPassenger()) {
            Entity mount = participant.getVehicle();

            participant.stopRiding();
            Living.transmitPassengers(mount);
        }

        Vec3 offset = teleportee.getOriginVector()
                .subtract(teleporter.getOriginVector())
                .add(
                    participant.getX() - Math.floor(participant.getX()),
                    0,
                    participant.getZ() - Math.floor(participant.getZ())
                );

        double yPos = getTargetYPosition(participant.level(), destination.pos(), CollisionContext.of(participant));
        Vec3 dest = new Vec3(
                destination.x() + offset.x(),
                yPos,
                destination.z() + offset.z()
        );

        participant.teleportTo(dest.x, dest.y, dest.z);
        if (participant.level().getBlockCollisions(participant, participant.getBoundingBox()).iterator().hasNext()) {
            dest = destination.vec();
            participant.teleportTo(dest.x, participant.getY(), dest.z);
        }
        teleporter.subtractEnergyCost(distance);

        participant.fallDistance /= Math.min(distance, 1);

        BlockPos blockPos = BlockPos.containing(dest);

        participant.level().playSound(null, blockPos, USounds.ENTITY_PLAYER_UNICORN_TELEPORT, SoundSource.PLAYERS, 1, 1);
        if (!participant.level().isInWorldBounds(blockPos)) {
            UCriteria.TELEPORT_ABOVE_WORLD.trigger(teleporter.asEntity());
        }

        return true;
    }

    private boolean enterable(Level w, BlockPos pos) {
        BlockState state = w.getBlockState(pos);
        if (StatePredicate.isFluid(state) || state.getBlock() instanceof LeavesBlock) {
            return false;
        }
        return w.isEmptyBlock(pos) || !state.canOcclude() || !state.isSuffocating(w, pos);
    }

    private boolean exception(Level w, BlockPos pos, Player player) {
        BlockState state = w.getBlockState(pos);
        VoxelShape shape;

        return state.entityCanStandOn(w, pos, player)
                || StatePredicate.isFluid(state)
                || (shape = state.getCollisionShape(w, pos, CollisionContext.of(player))).isEmpty()
                || shape.bounds().getYsize() > 1;
    }

    private double getTargetYPosition(Level world, BlockPos pos, CollisionContext context) {
        VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos, context);
        return pos.getY() + (shape.isEmpty() ? 0 : shape.bounds().getYsize());
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(30);
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);

        if (Debug.CHECK_GAME_VALUES) {
            var pos = prepare(player);
            if (pos.isPresent()) {
                for (int i = 0; i < 90; i++) {
                    Debug.drawBoxSelection(player, player.isClient() ? ParticleTypes.CHERRY_LEAVES : ParticleTypes.PORTAL, pos.get().pos());
                }
            }
        }
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
