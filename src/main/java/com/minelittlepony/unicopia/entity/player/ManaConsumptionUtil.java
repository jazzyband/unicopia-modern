package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.entity.damage.UDamageSources;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

public interface ManaConsumptionUtil {
    float MANA_PER_FOOD = 10F;
    float HEARTS_PER_FOOD = 0.5F;
    float SATURATION_PER_FOOD = 0.8F;

    static float consumeMana(MagicReserves.Bar mana, double foodSubtract) {

        if (foodSubtract <= 0) {
            return 0;
        }

        float availableMana = mana.get();
        float consumedMana = (float)foodSubtract *  MANA_PER_FOOD;

        if (consumedMana <= availableMana) {
            mana.set(availableMana - consumedMana);
            return 0;
        }

        mana.set(0);
        return (float)foodSubtract - (availableMana / MANA_PER_FOOD);
    }

    static float burnFood(Player entity, float foodSubtract) {
        FoodData hunger = entity.getFoodData();

        if (foodSubtract <= 0) {
            return 0;
        }

        float availableSaturation = hunger.getSaturationLevel();

        if (availableSaturation > 0 && foodSubtract > 0) {
            float consumedSaturation = Math.min(foodSubtract * SATURATION_PER_FOOD, availableSaturation);
            foodSubtract = addExhaustion(hunger, foodSubtract);
            if (consumedSaturation > 0) {
                foodSubtract -= (consumedSaturation / SATURATION_PER_FOOD);
                hunger.setSaturation(availableSaturation - consumedSaturation);
            }
        }

        int availableFood = hunger.getFoodLevel();
        if (availableFood > 0 && foodSubtract > 0) {
            int consumedFood = Math.min((int)Math.floor(foodSubtract), availableFood);
            foodSubtract = addExhaustion(hunger, foodSubtract);
            if (consumedFood > 0) {
                foodSubtract -= consumedFood;
                hunger.eat(-consumedFood, 0.3F);
            }
        }

        float availableHearts = entity.getHealth();
        if (foodSubtract > 0) {
            float consumedHearts = Math.max(0, Math.min(availableHearts - 1, foodSubtract * HEARTS_PER_FOOD));
            foodSubtract = addExhaustion(hunger, foodSubtract);
            foodSubtract -= (consumedHearts / HEARTS_PER_FOOD);
            if (consumedHearts > 0 && !entity.level().isClientSide()) {
                entity.hurtServer((ServerLevel)entity.level(), UDamageSources.of(entity.level()).damageOf(UDamageTypes.EXHAUSTION), consumedHearts);
            }
        }

        return Math.max(0, foodSubtract);
    }

    static float getCombinedTotalMana(Pony pony) {
        FoodData hunger = pony.asEntity().getFoodData();
        return hunger.getFoodLevel()
            + (pony.getMagicalReserves().getMana().get() / MANA_PER_FOOD)
            + (hunger.getSaturationLevel() / SATURATION_PER_FOOD)
            + ((pony.asEntity().getHealth() - 1) / HEARTS_PER_FOOD);
    }

    static float addExhaustion(FoodData hunger, float foodSubtract) {
        hunger.addExhaustion(0.1F);
        return Math.max(0, foodSubtract - 0.1F);
    }

    interface FloatSupplier {
        float get();
    }

    interface FloatConsumer {
        void accept(float f);
    }
}
