package com.minelittlepony.unicopia.datafix;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.AttributeModifierIdFix;

public class AttributeIdsFixBuilder {
    private final Map<UUID, String> uuids = new HashMap<>();
    private final Map<String, String> names = new HashMap<>();

    public AttributeIdsFixBuilder add(String uuid, String name, String id) {
        uuids.put(UUID.fromString(uuid), id);
        names.put(name, id);
        return this;
    }

    public AttributeIdsFixBuilder add(String uuid, String id) {
        uuids.put(UUID.fromString(uuid), id);
        return this;
    }

    public void register() {
        AttributeModifierIdFix.ID_MAP = Map.copyOf(Util.make(new HashMap<>(AttributeModifierIdFix.ID_MAP), map -> map.putAll(uuids)));
        AttributeModifierIdFix.NAME_MAP = Map.copyOf(Util.make(new HashMap<>(AttributeModifierIdFix.NAME_MAP), map -> map.putAll(names)));
    }
}
