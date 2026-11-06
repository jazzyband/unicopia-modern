package com.minelittlepony.unicopia.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = { HolderSet.Named.class })
public interface RegistryEntryListOwnerAccessor<T> {
    @Accessor("owner")
    HolderOwner<T> getOwner();
}

@Mixin(value = { Holder.Reference.class })
abstract class MixinRegistryEntryReference<T> implements RegistryEntryListOwnerAccessor<T> {
    @Override
    @Accessor("owner")
    public abstract HolderOwner<T> getOwner();
}