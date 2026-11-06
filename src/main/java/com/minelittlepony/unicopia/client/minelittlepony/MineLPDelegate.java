package com.minelittlepony.unicopia.client.minelittlepony;

import com.minelittlepony.unicopia.Race;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class MineLPDelegate {
    static MineLPDelegate INSTANCE = new MineLPDelegate();

    public static MineLPDelegate getInstance() {
        return INSTANCE;
    }

    public final Race getPlayerPonyRace() {
        if (Minecraft.getInstance().player == null) {
            return Race.HUMAN;
        }
        return getPlayerPonyRace(Minecraft.getInstance().player);
    }

    public Race getPlayerPonyRace(Player player) {
        return Race.HUMAN;
    }

    public float getPonyHeight(Entity entity) {
        return entity.getBbHeight();
    }

    public Race getRace(Entity entity) {
        return Race.HUMAN;
    }

    public Race getRace(EntityRenderState state) {
        return Race.HUMAN;
    }

    public int getMagicColor(Entity entity) {
        return 0;
    }
}
