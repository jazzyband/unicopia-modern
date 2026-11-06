package com.minelittlepony.unicopia.ability;

import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;

public class ToggleFlightAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 0;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 0;
    }

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.of(!player.asEntity().isCreative() && !player.getPhysics().getFlightType().isGrounded());
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Hit> getSerializer() {
        return Hit.CODEC;
    }

    @Override
    public Identifier getIcon(Pony player) {
        Race race = player.getObservedSpecies();
        return Abilities.REGISTRY.getKey(this).withPath(p -> "textures/gui/ability/" + p
            + (player.getPhysics().isFlying() ? "_land" : "_takeoff")
            + "_" + (race.isHuman() ? Race.EARTH : race).getId().getPath()
            + ".png");
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 0;
    }

    @Override
    public boolean apply(Pony player, Hit data) {
        if (prepare(player).isEmpty()) {
            return false;
        }

        player.subtractEnergyCost(1);

        if (!player.getPhysics().isFlying()) {
            if (player.asEntity().isPassenger()) {
                Vec3 pos = player.asEntity().position();
                player.asEntity().stopRiding();
                player.asEntity().setPos(pos.x(), pos.y() + 0.25, pos.z());
            }
            player.asEntity().push(0, player.getPhysics().getGravitySignum() * 0.7F, 0);
            Living.updateVelocity(player.asEntity());
            player.getPhysics().startFlying(true);
        } else {
            player.getPhysics().cancelFlight(true);
        }
        player.setAnimation(Animation.SPREAD_WINGS, Animation.Recipient.ANYONE);
        return true;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(6);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
    }
}
