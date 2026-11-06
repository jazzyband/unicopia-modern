package com.minelittlepony.unicopia;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gameevent.GameEvent;

public interface UGameEvents {

    Holder<GameEvent> PIE_STOMP = register("pie_stomp", 5);

    static Holder<GameEvent> register(String name, int range) {
        Identifier id = Unicopia.id(name);
        return Registry.registerForHolder(BuiltInRegistries.GAME_EVENT, id, new GameEvent(range));
    }

    static void bootstrap() {
    }
}
