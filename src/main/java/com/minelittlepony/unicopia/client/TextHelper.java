package com.minelittlepony.unicopia.client;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public interface TextHelper {
    static Stream<Component> wrap(Component text, int maxWidth) {
        return Minecraft.getInstance().font.getSplitter().splitLines(text, maxWidth, Style.EMPTY).stream().map(line -> {
            MutableComponent compiled = Component.literal("");
            line.visit((s, t) -> {
                compiled.append(Component.literal(t).setStyle(s));
                return Optional.empty();
            }, text.getStyle());
            return compiled;
        });
    }

    static Component join(Component delimiter, Iterable<? extends MutableComponent> elements) {
        MutableComponent initial = Component.empty();
        return StreamSupport.stream(elements.spliterator(), false).collect(Collectors.reducing(initial, (a, b) -> {
            return a == initial ? b : a.append(delimiter).append(b);
        }));
    }
}
