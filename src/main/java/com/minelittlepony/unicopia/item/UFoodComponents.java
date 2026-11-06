package com.minelittlepony.unicopia.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;

public interface UFoodComponents {
    FoodProperties OATS = builder(1, 0.7F).build();
    FoodProperties IMPORTED_OATS = builder(3, 1.3F).build();
    FoodProperties OATMEAL = builder(0, 1.3F).build();
    FoodProperties DAFODIL_DAISY_SANDWICH = builder(3, 1.4F).build();
    FoodProperties BURGER = builder(7, 1.4F).build();
    FoodProperties HAY_FRIES = builder(4, 2).build();
    FoodProperties CRISPY_HAY_FRIES = builder(6, 7).build();

    FoodProperties PIE = builder(3, 1.26F).build();
    FoodProperties CIDER = builder(2, 1.7F).alwaysEdible().build();

    FoodProperties JUICE = builder(2, 1.8F).alwaysEdible().build();
    FoodProperties BURNED_JUICE = builder(3, 1).build();

    FoodProperties NUT_BOWL = builder(5, 0.6F).build();//FoodComponents.BAKED_POTATO;

    FoodProperties OATMEAL_COOKIE = Foods.COOKIE; //builder(2, 0.1F).build();
    FoodProperties CHOCOLATE_OATMEAL_COOKIE = builder(3, 0.4F).build();
    FoodProperties SCONE = builder(2, 0.2F).build();
    FoodProperties FRIED_EGG = builder(4, 0.4F).build();

    FoodProperties ROTTEN_PUFFERFISH = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.1F)
            .build();
    FoodProperties COOKED_PUFFERFISH = builder(5, 0.6F).build();

    FoodProperties WORMS = builder(1, 1.5F).alwaysEdible().build();
    FoodProperties INSECTS = builder(1, 0).alwaysEdible().build();

    FoodProperties TOAST = builder(1, 0.6F).alwaysEdible().build();
    FoodProperties BURNED_TOAST = builder(1, -0.8F).alwaysEdible().build();
    FoodProperties JAM_TOAST = builder(4, 0.6F).alwaysEdible().build();
    FoodProperties ZAP_APPLE = builder(4, 0.3F).alwaysEdible().build();
    FoodProperties ZAP_BULB = builder(-2, -0.8f).alwaysEdible().build();

    FoodProperties LOVE_BOTTLE = builder(2, 0.125F).alwaysEdible().build();
    FoodProperties LOVE_MUG = builder(4, 0.125F).build();
    FoodProperties LOVE_BUCKET = builder(8, 0.125F).build();

    FoodProperties PINECONE = builder(0, 0.01F).alwaysEdible().build();
    FoodProperties ACORN = builder(1, 0.01F).alwaysEdible().build();
    FoodProperties MANGO = builder(8, 0.8F).alwaysEdible().build();
    FoodProperties BANANA = builder(6, 0.9F).build();
    FoodProperties SEEDS = builder(1, 0.2F).build();

    FoodProperties CANDY = builder(7, 0.9F).alwaysEdible().build();
    FoodProperties SALT_CUBE = builder(0, 2.9F).alwaysEdible().build();

    FoodProperties POISON_JOKE = builder(0, 0F).alwaysEdible().build();

    FoodProperties SHELL = builder(3, 1.5F).build();
    FoodProperties SHELLY = builder(6, 0.7F).build();
    FoodProperties ROCK = builder(3, 0.5F).build();
    FoodProperties ROCK_STEW = builder(6, 0.6F).build();

    static FoodProperties.Builder builder(int hunger, float saturation) {
        return new FoodProperties.Builder()
                .nutrition(hunger)
                .saturationModifier(saturation);
    }
}
