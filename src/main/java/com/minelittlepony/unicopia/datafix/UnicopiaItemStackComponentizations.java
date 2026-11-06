package com.minelittlepony.unicopia.datafix;

import java.util.Set;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import com.mojang.serialization.Dynamic;

final class UnicopiaItemStackComponentizations {
    private static final Set<String> ENCHANTED_ITEMS = Set.of(
            "unicopia:gemstone",
            "unicopia:magic_staff"
    );

    static void run(ItemStackComponentizationFix.ItemStackData data, Dynamic<?> dynamic) {
        if (data.is("unicopia:friendship_bracelet")) {
            data.moveTagToComponent("glowing", "unicopia:glowing");
            data.removeTag("issuer_id").result().ifPresent(id -> {
                data.setComponent("unicopia:issuer", dynamic.emptyMap()
                        .set("id", id)
                        .setFieldIfPresent("name", data.removeTag("issuer").result()));
            });
        }
        if (data.is("unicopia:grogars_bell")) {
            fixEnergy(data, dynamic, 1000, 1000);
        }
        if (data.is("unicopia:magic_staff")) {
            fixEnergy(data, dynamic, 3, 3);
        }
        if (data.is("unicopia:pegasus_amulet")) {
            fixEnergy(data, dynamic, 900, 450);
        }
        if (data.is("unicopia:zap_apple")) {
            fixAppearance(data, dynamic, true);
        }
        if (data.is("unicopia:filled_jar")) {
            fixAppearance(data, dynamic, false);
        }
        if (data.is(ENCHANTED_ITEMS)) {
            data.moveTagToComponent("spell", "unicopia:stored_spell");
            data.moveTagToComponent("spell_traits", "unicopia:spell_traits");
        }
        if (data.is("unicopia:butterfly")) {
            data.removeTag("variant").result().ifPresent(variant -> {
                data.setComponent("unicopia:butterfly_variant", dynamic.emptyMap()
                        .set("variant", variant)
                        .set("show_in_tooltip", dynamic.createBoolean(true)));
            });
        }
        if (data.is("unicopia:giant_balloon")) {
            data.moveTagToComponent("design", "unicopia:balloon_design");
        }
        if (data.is("unicopia:spellbook")) {
            data.moveTagToComponent("spellbookState", "unicopia:spellbook_state");
        }
    }

    private static void fixAppearance(ItemStackComponentizationFix.ItemStackData data, Dynamic<?> dynamic, boolean replaceFully) {
        data.removeTag("appearance").result().ifPresent(appearance -> {
            data.setComponent("unicopia:appearance", dynamic.emptyMap()
                    .set("item", appearance)
                    .set("replace_fully", dynamic.createBoolean(replaceFully)));
        });
    }

    private static void fixEnergy(ItemStackComponentizationFix.ItemStackData data, Dynamic<?> dynamic, int maximum, int baseline) {
        data.removeTag("energy").result().ifPresent(energy -> {
            data.setComponent("unicopia:charges", dynamic.emptyMap()
                    .set("energy", dynamic.createInt(energy.get("energy").asInt(energy.asInt(baseline))))
                    .set("maximum", dynamic.createInt(maximum))
                    .set("baseline", dynamic.createInt(baseline))
                    .set("show_in_tooltip", dynamic.createBoolean(true)));
        });
    }
}
