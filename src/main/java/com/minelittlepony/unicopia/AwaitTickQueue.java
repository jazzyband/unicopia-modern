package com.minelittlepony.unicopia;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public interface AwaitTickQueue {
    static void scheduleTask(Level reference, Consumer<Level> task, int ticksLater) {
        if (reference instanceof ServerLevel serverWorld) {
            CompletableFuture.runAsync(() -> {
                task.accept(serverWorld);
            }, CompletableFuture.delayedExecutor(ticksLater * 100, TimeUnit.MILLISECONDS, serverWorld.getServer()));
        }
    }

    static void scheduleTask(Level reference, Consumer<Level> task) {
        if (reference instanceof ServerLevel serverWorld) {
            CompletableFuture.runAsync(() -> {
                task.accept(serverWorld);
            }, CompletableFuture.delayedExecutor(1, TimeUnit.MILLISECONDS, serverWorld.getServer()));
        }
    }
}
