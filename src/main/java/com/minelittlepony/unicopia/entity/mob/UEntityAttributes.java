package com.minelittlepony.unicopia.entity.mob;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import com.minelittlepony.unicopia.Unicopia;

public interface UEntityAttributes {
    List<Attribute> REGISTRY = new ArrayList<>();

    Holder<Attribute> EXTENDED_REACH_DISTANCE = Attributes.BLOCK_INTERACTION_RANGE;
    Holder<Attribute> EXTENDED_ATTACK_DISTANCE = Attributes.ENTITY_INTERACTION_RANGE;
    Holder<Attribute> EXTRA_MINING_SPEED = register("earth.mining_speed", new RangedAttribute("player.miningSpeed", 1, 0, 5).setSyncable(true));
    Holder<Attribute> ENTITY_GRAVITY_MODIFIER = register("player.gravity", (new Attribute("player.gravityModifier", 1) {}).setSyncable(true));

    private static Holder<Attribute> register(String name, Attribute attribute) {
        REGISTRY.add(attribute);
        return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, Unicopia.id(name), attribute);
    }

    static void bootstrap() {}
}
