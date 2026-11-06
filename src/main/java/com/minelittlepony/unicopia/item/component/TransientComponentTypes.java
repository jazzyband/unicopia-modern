package com.minelittlepony.unicopia.item.component;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import com.minelittlepony.unicopia.diet.DietProfile;
import com.minelittlepony.unicopia.diet.PonyDiets;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.component.TransientComponentMap.Entry;

public interface TransientComponentTypes {
    Map<DataComponentType<?>, Entry<?>> ROOT = new HashMap<>();

    Entry<DietProfile> PROFILE = register(UDataComponentTypes.DIET_PROFILE, (s, comps, original) -> {
        return original != null && !original.isEmpty() ? original : comps.getCarrier()
                .flatMap(Pony::of)
                .map(PonyDiets.getInstance()::getDiet)
                .orElse(DietProfile.EMPTY);
    }, (s, comps, original) -> original || !comps.getCarrier()
            .flatMap(Pony::of)
            .map(PonyDiets.getInstance()::getDiet)
            .orElse(DietProfile.EMPTY).isEmpty());

    Entry<FoodProperties> FOOD_COMPONENT = register(DataComponents.FOOD,
            (s, comps, original) -> s.getOrDefault(UDataComponentTypes.DIET_PROFILE, DietProfile.EMPTY).getAdjustedFoodComponent(s, comps, original),
            (s, comps, original) -> s.getOrDefault(UDataComponentTypes.DIET_PROFILE, DietProfile.EMPTY).hasFoodAttributes(s, comps, original)
    );

    Entry<Consumable> CONSUMABLE_COMPONENT = register(DataComponents.CONSUMABLE,
            (s, comps, original) -> s.getOrDefault(UDataComponentTypes.DIET_PROFILE, DietProfile.EMPTY).getAdjustedConsumableComponent(s, comps, original),
            (s, comps, original) -> s.getOrDefault(UDataComponentTypes.DIET_PROFILE, DietProfile.EMPTY).hasFoodAttributes(s, comps, original)
    );

    static <T> TransientComponentMap.Entry<T> register(DataComponentType<T> type,
            TransientComponentMap.Entry.Func<T> getter,
            TransientComponentMap.Entry.Func<Boolean> checker) {
        var entry = new Entry<>(getter, checker);
        ROOT.put(type, entry);
        return entry;
    }
}
