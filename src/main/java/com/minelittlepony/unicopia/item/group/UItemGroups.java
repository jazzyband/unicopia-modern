package com.minelittlepony.unicopia.item.group;

import java.util.stream.Stream;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.component.Appearance;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;

public interface UItemGroups {
    ResourceKey<CreativeModeTab> ALL_ITEMS = ItemGroupRegistry.createDynamic("items", UItems.CRYSTAL_HEART, () -> {
        return Stream.concat(Stream.of(Items.APPLE), ItemGroupRegistry.ITEMS.stream()
                .filter(item -> !item.components().getOrDefault(UDataComponentTypes.APPEARANCE, Appearance.DEFAULT).replaceFully()));
    });
    ResourceKey<CreativeModeTab> FORAGING_ITEMS = ItemGroupRegistry.createGroupFromTag("foraging", UTags.Items.GROUP_FORAGING, Items.HAY_BLOCK);
    ResourceKey<CreativeModeTab> EARTH_PONY_ITEMS = ItemGroupRegistry.createGroupFromTag("earth_pony", UTags.Items.GROUP_EARTH_PONY, UItems.EARTH_BADGE);
    ResourceKey<CreativeModeTab> UNICORN_ITEMS = ItemGroupRegistry.createGroupFromTag("unicorn", UTags.Items.GROUP_UNICORN, UItems.UNICORN_BADGE);
    ResourceKey<CreativeModeTab> PEGASUS_ITEMS = ItemGroupRegistry.createGroupFromTag("pegasus", UTags.Items.GROUP_PEGASUS, UItems.PEGASUS_BADGE);
    ResourceKey<CreativeModeTab> BAT_PONY_ITEMS = ItemGroupRegistry.createGroupFromTag("bat_pony", UTags.Items.GROUP_BAT_PONY, UItems.BAT_BADGE);
    ResourceKey<CreativeModeTab> SEA_PON_ITEMS = ItemGroupRegistry.createGroupFromTag("sea_pony", UTags.Items.GROUP_SEA_PONY, UItems.PEARL_NECKLACE);
    ResourceKey<CreativeModeTab> CHANGELING_ITEMS = ItemGroupRegistry.createGroupFromTag("changeling", UTags.Items.GROUP_CHANGELING, UItems.CHANGELING_BADGE);

    static void bootstrap() {
        ItemGroupRegistry.bootstrap();
    }
}
