package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.gui.UHud;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

@Mixin(KeyboardInput.class)
abstract class MixinKeyboardInput extends ClientInput {
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void onTick(CallbackInfo info) {
        Pony player = Pony.of(Minecraft.getInstance().player);

        if (player != null) {
            if (player.getPhysics().isGravityNegative()) {
                keyPresses = new Input(
                    keyPresses.forward(),
                    keyPresses.backward(),
                    keyPresses.right(),
                    keyPresses.left(),
                    keyPresses.jump(),
                    keyPresses.shift(),
                    keyPresses.sprint()
                );
                moveVector = new Vec2(-moveVector.x, moveVector.y).normalized();
            }

            if (player.getAcrobatics().isImmobile()) {
                moveVector = Vec2.ZERO;
            }

            if (UHud.INSTANCE.handleInput(player, this)) {
                moveVector = Vec2.ZERO;
                if (keyPresses.jump()) {
                    keyPresses = new Input(
                        keyPresses.forward(),
                        keyPresses.backward(),
                        keyPresses.left(),
                        keyPresses.right(),
                        false,
                        keyPresses.shift(),
                        keyPresses.sprint()
                    );
                }
            }
        }
    }
}
