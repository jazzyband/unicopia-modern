package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.minelittlepony.unicopia.client.render.WorldRenderDelegate;
import com.minelittlepony.unicopia.client.render.entity.HitboxController;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;
import com.minelittlepony.unicopia.util.Untyped;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;

@Mixin(EntityRenderDispatcher.class)
abstract class MixinEntityRenderDispatcher implements SpellEffectsRenderDispatcher.RenderDispatcherAccessor {
    @Inject(method = "extractEntity", at = @At("RETURN"))
    private <E extends Entity> void unicopia_trackEntity(E entity, float tickDelta,
            CallbackInfoReturnable<EntityRenderState> info) {
        WorldRenderDelegate.INSTANCE.track(entity, info.getReturnValue());
    }

    @WrapMethod(method = "submit")
    private <S extends EntityRenderState> void unicopia_wrapSubmit(S state, CameraRenderState camera,
            double x, double y, double z, PoseStack matrices, SubmitNodeCollector vertices,
            Operation<Void> operation) {
        EntityRenderDispatcher dispatcher = (EntityRenderDispatcher)(Object)this;
        WorldRenderDelegate.INSTANCE.handleEntitySubmit(state, camera, x, y, z, matrices, vertices,
                () -> operation.call(state, camera, x, y, z, matrices, vertices),
                (entity, rx, ry, rz) -> operation.call(dispatcher.extractEntity(entity, camera.cameraEntityPartialTicks),
                        camera, rx, ry, rz, matrices, vertices));
    }

    @Override
    public boolean shouldRenderShadows() {
        return true;
    }
}
