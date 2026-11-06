package com.minelittlepony.unicopia.ability;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.TraceHelper;

/**
 * Pegasi ability to pick up and carry other players
 */
public class CarryAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 5;
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
    public Optional<Hit> prepare(Pony player) {
        return Hit.INSTANCE;
    }

    protected LivingEntity findRider(Player player, Level w) {
        return TraceHelper.<LivingEntity>findEntity(player, 10, 1, hit -> {
            return EquinePredicates.VALID_LIVING_AND_NOT_MAGIC_IMMUNE.test(hit) && !player.isPassengerOfSameVehicle(hit);
        }).orElse(null);
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Hit> getSerializer() {
        return Hit.CODEC;
    }

    @Override
    public boolean onQuickAction(Pony player, ActivationType type, Optional<Hit> data) {

        if (type == ActivationType.TAP && player.getPhysics().isFlying()) {
            player.getPhysics().dashForward((float)player.asWorld().getRandom().triangle(1, 0.3F));
            return true;
        }

        return false;
    }

    @Override
    public boolean acceptsQuickAction(Pony player, ActivationType type) {
        return type == ActivationType.NONE || type == ActivationType.TAP;
    }

    @Override
    public boolean apply(Pony iplayer, Hit data) {
        Player player = iplayer.asEntity();
        LivingEntity rider = findRider(player, iplayer.asWorld());

        dropAllPassengers(player);

        if (rider != null) {
            rider.startRiding(player, true, true);
            Living.getOrEmpty(rider).ifPresent(living -> living.setCarrier(player));
        }

        Living.transmitPassengers(player);
        return true;
    }

    protected void dropAllPassengers(Player player) {
        if (player.isVehicle()) {
            List<Entity> passengers = StreamSupport.stream(player.getIndirectPassengers().spliterator(), false).toList();
            player.ejectPassengers();
            for (Entity passenger : passengers) {
                passenger.snapTo(player.position());
                Living<?> l = Living.living(passenger);
                if (l != null) {
                    l.setCarrier((UUID)null);
                }
            }
        }
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
    }
}
