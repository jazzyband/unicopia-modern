package com.minelittlepony.unicopia.mixin.server;


import java.util.List;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@Mixin(StructureTemplatePool.class)
public interface MixinStructurePool {
    @Accessor
    ObjectArrayList<StructurePoolElement> getTemplates();

    @Accessor
    @Mutable
    void setTemplates(ObjectArrayList<StructurePoolElement> elements);

    @Accessor("rawTemplates")
    List<Pair<StructurePoolElement, Integer>> getElementCounts();

    @Accessor("rawTemplates")
    @Mutable
    void setElementCounts(List<Pair<StructurePoolElement, Integer>> elementWeights);
}
