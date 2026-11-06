package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import com.minelittlepony.unicopia.client.ClientBlockDestructionManager;
import net.minecraft.client.renderer.LevelRenderer;

@Mixin(value = LevelRenderer.class, priority = 1001)
abstract class MixinWorldRenderer implements ClientBlockDestructionManager.Source {
    private final ClientBlockDestructionManager destructions = new ClientBlockDestructionManager();

    @Override
    public ClientBlockDestructionManager getDestructionManager() {
        return destructions;
    }
}
