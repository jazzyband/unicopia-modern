package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public interface UWoodTypes {
    WoodType PALM = register("palm");
    WoodType ZAP = register("zap");
    WoodType GOLDEN_OAK = register("golden_oak");

    BlockSetType CLOUD = new BlockSetTypeBuilder()
            .soundType(SoundType.WOOL)
            .doorCloseSound(USounds.Vanilla.WOOL_HIT)
            .doorOpenSound(USounds.Vanilla.WOOL_BREAK)
            .register(Unicopia.id("cloud"));
    BlockSetType CRYSTAL = BlockSetTypeBuilder.copyOf(BlockSetType.IRON)
            .soundType(SoundType.AMETHYST)
            .openableByHand(true)
            .register(Unicopia.id("crystal"));

    static WoodType register(String name) {
        Identifier id = Unicopia.id(name);
        return new WoodTypeBuilder().register(id, new BlockSetTypeBuilder().register(id));
    }
}
