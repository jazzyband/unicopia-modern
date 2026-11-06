package com.minelittlepony.unicopia.mixin;

import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.server.world.gen.BiomeSelectionInjector;
import com.mojang.datafixers.util.Pair;

@Mixin(OverworldBiomeBuilder.class)
abstract class MixinVanillaBiomeParameters {
    @ModifyVariable(method = "addBiomes", at = @At("HEAD"))
    private Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> onWriteOverworldBiomeParameters(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> parametersCollector) {
        return new BiomeSelectionInjector(parametersCollector);
    }
}
