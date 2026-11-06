package com.minelittlepony.unicopia.mixin.server;

import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.server.world.BlockDestructionManager;
import com.minelittlepony.unicopia.server.world.NocturnalSleepManager;

@Mixin(ServerLevel.class)
abstract class MixinServerWorld implements WorldGenLevel, NocturnalSleepManager.Source {

    private NocturnalSleepManager nocturnalSleepManager;

    @Inject(method = "sendBlockUpdated", at = @At("HEAD"))
    private void onOnBlockChanged(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo info) {
        ((BlockDestructionManager.Source)this).getDestructionManager().onBlockChanged(pos, oldState, newState);
    }

    @ModifyConstant(method = "announceSleepStatus()V", constant = @Constant(stringValue = "sleep.skipping_night"))
    private String modifySleepingMessage(String initial) {
        return getNocturnalSleepManager().getTimeSkippingMessage(initial);
    }

    @Override
    public NocturnalSleepManager getNocturnalSleepManager() {
        if (nocturnalSleepManager == null) {
            nocturnalSleepManager = new NocturnalSleepManager((ServerLevel)(Object)this);
        }
        return nocturnalSleepManager;
    }

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerLevel;wakeUpAllPlayers()V"
    ))
    public void beforeWakeup(BooleanSupplier shouldKeepTicking, CallbackInfo info) {
        getNocturnalSleepManager().skipTime();
    }
}
