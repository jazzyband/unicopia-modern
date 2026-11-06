package com.minelittlepony.unicopia.ability;

import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;

/**
 * Pegasus ability to capture a storm and store it in a jar
 */
public class PegasusCaptureStormAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 9;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 6;
    }

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.of(player.asEntity().isCreative() || player.getMagicalReserves().getMana().getPercentFill() >= 0.2F);
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Hit> getSerializer() {
        return Hit.CODEC;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return player.getMagicalReserves().getMana().getMax() * 0.9F;
    }

    @Override
    public boolean apply(Pony player, Hit data) {

        Level w = player.asWorld();
        ItemStack stack = player.asEntity().getItemInHand(InteractionHand.MAIN_HAND);
        boolean thundering = w.isThundering();

        if (!stack.is(UItems.EMPTY_JAR)) {
            tell(player, "ability.unicopia.empty_hooves");
        } else if (!w.canSeeSky(player.getOrigin())) {
            tell(player, "ability.unicopia.indoors");
        } else if (!(w.isRaining() || thundering)) {
            tell(player, "ability.unicopia.clear_skies");
        } else if (player.getOrigin().getY() < 120) {
            tell(player, "ability.unicopia.too_low");
        } else {
            if (!player.asEntity().getAbilities().instabuild) {
                stack.shrink(1);
            }

            if (thundering && w.getRandom().nextBoolean()) {
                player.asEntity().addItem(UItems.STORM_CLOUD_JAR.getDefaultInstance());

                if (w instanceof ServerLevel) {
                    var props = ((ServerLevel)w).getWeatherData();

                    int timer = (int)Math.floor(props.getRainTime() * 0.1);

                    props.setThundering(timer > 0);
                    props.setClearWeatherTime((int)(w.getRandom().nextFloat() * 150000));
                    props.setThunderTime(timer);
                    ((ServerLevel)w).getServer().getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, w.getRainLevel(1)), w.dimension());
                }
            } else {
                player.asEntity().addItem(UItems.RAIN_CLOUD_JAR.getDefaultInstance());

                if (w instanceof ServerLevel) {
                    var props = ((ServerLevel)w).getWeatherData();

                    int timer = (int)Math.floor(props.getRainTime() * 0.1);

                    props.setRaining(timer > 0);
                    props.setClearWeatherTime((int)(w.getRandom().nextFloat() * 150000));
                    props.setRainTime(timer);
                    ((ServerLevel)w).getServer().getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, w.getRainLevel(1)), w.dimension());
                }
            }
        }

        return true;
    }

    private void tell(Pony player, String translation) {
        player.asEntity().sendOverlayMessage(Component.translatable(translation));
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(6);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
