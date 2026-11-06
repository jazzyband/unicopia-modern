package com.minelittlepony.unicopia.datagen;

import com.minelittlepony.unicopia.block.UBlocks;
import net.minecraft.data.BlockFamily;

public interface UBlockFamilies {
    BlockFamily PALM = new BlockFamily.Builder(UBlocks.PALM_PLANKS)
            .log(UBlocks.PALM_LOG).strippedLog(UBlocks.STRIPPED_PALM_LOG)
            .slab(UBlocks.PALM_SLAB).stairs(UBlocks.PALM_STAIRS).fence(UBlocks.PALM_FENCE).fenceGate(UBlocks.PALM_FENCE_GATE)
            .button(UBlocks.PALM_BUTTON).pressurePlate(UBlocks.PALM_PRESSURE_PLATE).sign(UBlocks.PALM_SIGN, UBlocks.PALM_WALL_SIGN)
            .hangingSign(UBlocks.PALM_HANGING_SIGN, UBlocks.PALM_WALL_HANGING_SIGN)
            .door(UBlocks.PALM_DOOR).trapdoor(UBlocks.PALM_TRAPDOOR)
            .recipeGroupPrefix("wooden").recipeUnlockedBy("has_planks")
            .getFamily();
    BlockFamily GOLDEN_OAK = new BlockFamily.Builder(UBlocks.GOLDEN_OAK_PLANKS)
            .slab(UBlocks.GOLDEN_OAK_SLAB).stairs(UBlocks.GOLDEN_OAK_STAIRS)//.fence(UBlocks.PALM_FENCE).fenceGate(UBlocks.PALM_FENCE_GATE)
            //.button(UBlocks.PALM_BUTTON).pressurePlate(UBlocks.PALM_PRESSURE_PLATE).sign(UBlocks.PALM_SIGN, UBlocks.PALM_WALL_SIGN)
            //.door(UBlocks.PALM_DOOR).trapdoor(UBlocks.PALM_TRAPDOOR)
            .recipeGroupPrefix("wooden").recipeUnlockedBy("has_planks")
            .getFamily();
    BlockFamily ZAP = new BlockFamily.Builder(UBlocks.ZAP_PLANKS)
            .slab(UBlocks.ZAP_SLAB).stairs(UBlocks.ZAP_STAIRS).fence(UBlocks.ZAP_FENCE).fenceGate(UBlocks.ZAP_FENCE_GATE)
            .recipeGroupPrefix("wooden").recipeUnlockedBy("has_planks")
            .getFamily();
    BlockFamily WAXED_ZAP = new BlockFamily.Builder(UBlocks.WAXED_ZAP_PLANKS)
            .slab(UBlocks.WAXED_ZAP_SLAB).stairs(UBlocks.WAXED_ZAP_STAIRS).fence(UBlocks.WAXED_ZAP_FENCE).fenceGate(UBlocks.WAXED_ZAP_FENCE_GATE)
            .recipeGroupPrefix("wooden").recipeUnlockedBy("has_planks")
            .getFamily();
    BlockFamily CHISELED_CHITIN = new BlockFamily.Builder(UBlocks.CHISELLED_CHITIN)
            .slab(UBlocks.CHISELLED_CHITIN_SLAB).stairs(UBlocks.CHISELLED_CHITIN_STAIRS)
            .wall(UBlocks.CHISELLED_CHITIN_WALL)
            .polished(UBlocks.POLISHED_CHITIN)
            .recipeGroupPrefix("chitin").recipeUnlockedBy("has_chiselled_chitin")
            .getFamily();
    BlockFamily POLISHED_CHITIN = new BlockFamily.Builder(UBlocks.POLISHED_CHITIN)
            .slab(UBlocks.POLISHED_CHITIN_SLAB).stairs(UBlocks.POLISHED_CHITIN_STAIRS)
            .wall(UBlocks.POLISHED_CHITIN_WALL)
            .recipeGroupPrefix("chitin").recipeUnlockedBy("has_polished_chitin")
            .getFamily();
    BlockFamily CLOUD = new BlockFamily.Builder(UBlocks.CLOUD)
            .slab(UBlocks.CLOUD_SLAB).stairs(UBlocks.CLOUD_STAIRS)
            .recipeGroupPrefix("cloud").recipeUnlockedBy("has_cloud_lump")
            .getFamily();
    BlockFamily ETCHED_CLOUD = new BlockFamily.Builder(UBlocks.ETCHED_CLOUD)
            .slab(UBlocks.ETCHED_CLOUD_SLAB).stairs(UBlocks.ETCHED_CLOUD_STAIRS)
            .recipeGroupPrefix("etched_cloud").recipeUnlockedBy("has_cloud_lump")
            .getFamily();
    BlockFamily CLOUD_PLANKS = new BlockFamily.Builder(UBlocks.CLOUD_PLANKS)
            .slab(UBlocks.CLOUD_PLANK_SLAB).stairs(UBlocks.CLOUD_PLANK_STAIRS)
            .recipeGroupPrefix("cloud").recipeUnlockedBy("has_cloud")
            .getFamily();
    BlockFamily CLOUD_BRICKS = new BlockFamily.Builder(UBlocks.CLOUD_BRICKS)
            .slab(UBlocks.CLOUD_BRICK_SLAB).stairs(UBlocks.CLOUD_BRICK_STAIRS)
            .recipeGroupPrefix("cloud").recipeUnlockedBy("has_cloud_bricks")
            .getFamily();
    BlockFamily DENSE_CLOUD = new BlockFamily.Builder(UBlocks.DENSE_CLOUD)
            .slab(UBlocks.DENSE_CLOUD_SLAB).stairs(UBlocks.DENSE_CLOUD_STAIRS)
            .recipeGroupPrefix("cloud").recipeUnlockedBy("has_dense_cloud")
            .getFamily();
}
