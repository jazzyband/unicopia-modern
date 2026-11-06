package com.minelittlepony.unicopia.entity.player;

import java.lang.ref.WeakReference;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

public interface ExperienceGroup {
    String[] EXPERIENCES = {
            "magical_kindergartner",
            "friendship_student",
            "senior_friendship_student",
            "junior_mage",
            "mage",
            "archmage",
            "archmagus",
            "senior_archmagus",
            "ascended_senior_archmagus",
            "demi_god",
            "arch_demi_god",
            "alicorn_princess",
            "polycorn_princess",
            "faustian_legend"
    };
    String[] CORRUPTIONS = {
            "pure",
            "impure",
            "tainted",
            "twisted",
            "corrupt",
            "monstrous"
    };
    Int2ObjectArrayMap<WeakReference<Component>> CACHE = new Int2ObjectArrayMap<>();

    static Component forLevel(float level, float corruption) {
        int c = get(CORRUPTIONS, corruption);
        int x = get(EXPERIENCES, level);
        int i = x * c;

        Component value;
        if (!CACHE.containsKey(i) || (value = CACHE.get(i).get()) == null) {
            CACHE.put(i, new WeakReference<>(value = Component.translatable("experience.unicopia." + CORRUPTIONS[c] + "." + EXPERIENCES[x])));
        }
        return value;
    }

    private static int get(String[] values, float level) {
        return Mth.clamp(Mth.floor(level * values.length), 0, values.length - 1);
    }
}
