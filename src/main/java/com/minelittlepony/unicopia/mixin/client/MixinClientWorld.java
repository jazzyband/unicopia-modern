package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import com.minelittlepony.unicopia.client.minelittlepony.EntityLookupAccessor;
import com.minelittlepony.unicopia.client.ClientBlockDestructionManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.SortedSet;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;

@Mixin(ClientLevel.class)
abstract class MixinClientWorld implements EntityLookupAccessor {
    @Shadow @Final private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    private ClientBlockDestructionManager unicopia$destructions() {
        return ((ClientBlockDestructionManager.Source)Minecraft.getInstance().levelRenderer).getDestructionManager();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void unicopia$tickDestructions(BooleanSupplier shouldKeepTicking, CallbackInfo info) {
        unicopia$destructions().tick(destructionProgress);
    }

    @ModifyReturnValue(method = "destructionProgress", at = @At("RETURN"))
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> unicopia$combineDestructions(Long2ObjectMap<SortedSet<BlockDestructionProgress>> original) {
        return unicopia$destructions().getCombinedDestructions(original);
    }

    @Override
    @Invoker("getEntities")
    public abstract LevelEntityGetter<Entity> callGetEntityLookup();

}
