package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * An ability to screeeeeeeeEeEeEeeee!
 */
public class BatEeeeAbility extends ScreechAbility {
    public static final int SELF_SPOOK_PROBABILITY = 20000;

    @Override
    protected void playSounds(Pony player, RandomSource rng, float strength) {
        int count = 1 + rng.nextInt(10) + (int)(strength * 10);

        for (int i = 0; i < count; i++) {
            player.playSound(USounds.ENTITY_PLAYER_BATPONY_SCREECH,
                    (0.9F + (rng.nextFloat() - 0.5F) / 2F) * strength,
                    1.6F + (rng.nextFloat() - 0.5F)
            );
        }
        player.asWorld().gameEvent(player.asEntity(), GameEvent.ENTITY_ACTION, player.asEntity().getEyePosition());
        for (int j = 0; j < (int)(strength * 2); j++) {
            for (int k = 0; k < count; k++) {
                AwaitTickQueue.scheduleTask(player.asWorld(), w -> {
                    player.playSound(USounds.ENTITY_PLAYER_BATPONY_SCREECH,
                            (0.9F + (rng.nextFloat() - 0.5F) / 2F) * strength,
                            1.6F + (rng.nextFloat() - 0.5F)
                    );
                    player.asWorld().gameEvent(player.asEntity(), GameEvent.ENTITY_ACTION, player.asEntity().getEyePosition());
                }, rng.nextInt(3));
            }
        }

        if (player.asWorld() instanceof ServerLevel sw && strength > 0.5F && rng.nextInt(SELF_SPOOK_PROBABILITY) == 0) {
            player.asEntity().hurtServer(sw, player.damageOf(UDamageTypes.BAT_SCREECH, player), 0.1F);
            UCriteria.SCREECH_SELF.trigger(player.asEntity());
        }
    }
}
