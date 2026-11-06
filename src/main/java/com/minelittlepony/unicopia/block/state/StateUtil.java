package com.minelittlepony.unicopia.block.state;

import java.util.Iterator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

public interface StateUtil {
    Splitter COMMA_SPLITTER = Splitter.on(',');
    Splitter KEY_VALUE_SPLITTER = Splitter.on('=').limit(2);
    Splitter STATE_SPLITTER = Splitter.on(CharMatcher.anyOf("[]")).limit(2);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static BlockState copyState(BlockState from, @Nullable BlockState to) {
        if (to == null) {
            return to;
        }
        for (var property : from.getProperties()) {
            to = to.trySetValue((Property)property, from.getValue(property));
        }
        return to;
    }

    static BlockState stateFromString(String string) {
        Iterator<String> pair = Splitter.on(CharMatcher.anyOf("[]")).limit(3).split(string).iterator();
        if (!pair.hasNext()) {
            return Blocks.AIR.defaultBlockState();
        }
        Block block = Identifier.read(pair.next()).result().map(BuiltInRegistries.BLOCK::getValue).orElse(null);
        if (block == null) {
            return Blocks.AIR.defaultBlockState();
        }
        if (!pair.hasNext()) {
            return block.defaultBlockState();
        }
        return stateFromKeyMap(block, pair.next());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static BlockState stateFromKeyMap(Block block, String keyMap) {
        var stateFactory = block.getStateDefinition();
        var state = block.defaultBlockState();
        for (String pair : COMMA_SPLITTER.split(keyMap)) {
            Iterator<String> iterator = KEY_VALUE_SPLITTER.split(pair).iterator();
            if (!iterator.hasNext()) continue;
            String propertyName = iterator.next();
            var property = stateFactory.getProperty(propertyName);
            if (property != null && iterator.hasNext()) {
                String value = iterator.next();
                var comparable = property.getValue(value).orElse(null);
                if (comparable != null) {
                    state = state.setValue((Property)property, (Comparable)comparable);
                    continue;
                }
                throw new RuntimeException("Unknown value: '" + value + "' for blockstate property: '" + propertyName + "' " + property.getPossibleValues());
            }
            if (!propertyName.isEmpty()) {
                throw new RuntimeException("Unknown blockstate property: '" + propertyName + "'");
            }
        }
        return state;
    }
}
