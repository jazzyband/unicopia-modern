package com.minelittlepony.unicopia.client;

import org.jetbrains.annotations.Nullable;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.event.ScreenInitCallback;
import com.minelittlepony.common.event.ScreenInitCallback.ButtonList;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.gui.LanSettingsScreen;
import com.minelittlepony.unicopia.client.gui.ShapingBenchScreen;
import com.minelittlepony.unicopia.client.gui.UHud;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookScreen;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;
import com.minelittlepony.unicopia.container.*;
import com.minelittlepony.unicopia.entity.player.PlayerCamera;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.handler.ClientNetworkHandlerImpl;
import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;
import com.minelittlepony.unicopia.util.Lerp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class UnicopiaClient implements ClientModInitializer {
    private static final Component PROGRAMMER_ART_NAME = Component.translatable("resourcepack.unicopia.programmer_art");

    private static UnicopiaClient instance;

    public static UnicopiaClient getInstance() {
        return instance;
    }

    @Nullable
    public static Pony getClientPony() {
        return Pony.of(Minecraft.getInstance().player);
    }

    public final Lerp tangentalSkyAngle = new Lerp(0, true);
    public final Lerp skyAngle = new Lerp(0, true);

    private ZapAppleStageStore.Stage zapAppleStage = ZapAppleStageStore.Stage.HIBERNATING;

    public static PlayerCamera getCamera() {
        Player player = Minecraft.getInstance().player;

        if (player != null && Minecraft.getInstance().getCameraEntity() == player) {
            return Pony.of(player).getCamera();
        }

        return PlayerCamera.DEFAULT;
    }


    public static Vec3 getAdjustedSoundPosition(Vec3 pos, boolean isPositionRelative) {
        PlayerCamera cam = getCamera();
        if (cam == PlayerCamera.DEFAULT) {
            return pos;
        }
        var camera = Minecraft.getInstance().gameRenderer.mainCamera();
        boolean firstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
        float fovEffectScale = Minecraft.getInstance().options.fovEffectScale().get().floatValue();

        float roll = Mth.wrapDegrees(cam.calculateRoll(firstPerson, fovEffectScale));
        if (Mth.equal(Math.abs(roll), 0)) {
            return pos;
        }

        Vec3 cameraPos = camera.position();
        Vec3 relativePos = isPositionRelative ? pos : pos.subtract(cameraPos);
        if (Mth.equal(relativePos.length(), 0)) {
            return pos;
        }
        Vec3 newPos = new Vec3(relativePos.toVector3f().rotateAxis(roll * Mth.DEG_TO_RAD, 0, 1, 0));
        if (!isPositionRelative) {
            newPos = newPos.add(cameraPos);
        }

        return newPos;
    }

    public static Race getPreferredRace() {
        if (!Unicopia.getConfig().ignoreMineLP.get()
                && Minecraft.getInstance().player != null) {
            Race race = MineLPDelegate.getInstance().getPlayerPonyRace();

            if (race.isEquine()) {
                return race;
            }
        }

        return Unicopia.getConfig().preferredRace.get();
    }

    public UnicopiaClient() {
        instance = this;
    }

    public void setZapAppleStage(ZapAppleStageStore.Stage stage) {
        zapAppleStage = stage;
    }

    public ZapAppleStageStore.Stage getZapAppleStage() {
        return zapAppleStage;
    }

    public float getSkyAngleDelta(float tickDelta) {
        if (Minecraft.getInstance().level == null) {
            return 0;
        }
        float skyAngle = (Minecraft.getInstance().level.clockManager().getInstance(
                Minecraft.getInstance().level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_CLOCK)
                        .getOrThrow(net.minecraft.world.clock.WorldClocks.OVERWORLD)).totalTicks() + tickDelta) / 24000F;
        this.skyAngle.update(skyAngle, 200);
        return this.skyAngle.getValue() - skyAngle;
    }

    @Override
    public void onInitializeClient() {
        new ClientInteractionManager();
        new ClientNetworkHandlerImpl();

        KeyBindingsHandler.bootstrap();
        URenderers.bootstrap();

        MenuScreens.register(UScreenHandlers.SPELL_BOOK, SpellbookScreen::new);
        MenuScreens.register(UScreenHandlers.SHAPING_BENCH, ShapingBenchScreen::new);

        Unicopia.getConfig().skinFeatures.onChanged(features -> {
            Pony pony = Pony.of(Minecraft.getInstance().player);
            if (pony != null) {
                pony.setSkinFeatures(features);
            }
        });
        Unicopia.getConfig().onChangedExternally(config -> {
            Pony pony = Pony.of(Minecraft.getInstance().player);
            if (pony != null) {
                pony.setSkinFeatures(Unicopia.getConfig().skinFeatures.get());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

        FabricLoader.getInstance().getModContainer("unicopia").ifPresent(container -> {
            ResourceManagerHelper.registerBuiltinResourcePack(Unicopia.id("unicopia_programmer_art"), container, PROGRAMMER_ART_NAME, ResourcePackActivationType.NORMAL);
        });

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(SpellEffectsRenderDispatcher.INSTANCE);
    }

    private void onTick(Minecraft client) {
        KeyBindingsHandler.INSTANCE.tick(client);
        UHud.INSTANCE.tick();
    }

}
