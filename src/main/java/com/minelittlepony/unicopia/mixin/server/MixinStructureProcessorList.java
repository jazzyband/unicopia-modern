package com.minelittlepony.unicopia.mixin.server;

import java.util.List;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructureProcessorList.class)
public interface MixinStructureProcessorList {
    @Accessor
    @Mutable
    void setList(List<StructureProcessor> list);
}
