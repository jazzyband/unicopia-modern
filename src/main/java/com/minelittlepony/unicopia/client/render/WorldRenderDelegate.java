package com.minelittlepony.unicopia.client.render;

import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.ModelPartHooks.EnqueudHeadRender;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.duck.LavaAffine;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.ColorHelper;
import com.minelittlepony.unicopia.util.Untyped;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer.FogMode;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.fog.FogRenderer;

public class WorldRenderDelegate {
    public static final WorldRenderDelegate INSTANCE = new WorldRenderDelegate();
    private final Map<EntityRenderState, Entity> extractedEntities = java.util.Collections.synchronizedMap(new WeakHashMap<>());
    private static final Optional<Vec3> RED_SKY_COLOR = Optional.of(new Vec3(1, 0, 0));

    private final EntityReplacementManager disguiseLookup = new EntityReplacementManager();
    private final EntityDisguiseRenderer disguiseRenderer = new EntityDisguiseRenderer(this);
    private final SmittenEyesRenderer smittenEyesRenderer = new SmittenEyesRenderer();
    private final FishBowlRenderer fishBowlRenderer = new FishBowlRenderer();

    private static final int[] DEFAULT_FOG_DIFF = { 0, 0 };
    private static final int[] SEAPONY_UNDERWATER_FOG_DIFF = { -30, 190 };
    private static final int[] SEAPONY_SURFACE_FOG_DIFF = { -130, 0 };

    final Minecraft client = Minecraft.getInstance();

    public Optional<Vec3> getSkyColor(float tickDelta) {
        if (EquinePredicates.RAGING.test(client.player)) {
            return RED_SKY_COLOR;
        }
        return Optional.empty();
    }

    public void applyFog(Camera camera, FogData fog) {
        if (EquinePredicates.PLAYER_SEAPONY.test(client.player)) {
            int[] change = switch (camera.getFluidInCamera()) {
                case WATER -> SEAPONY_UNDERWATER_FOG_DIFF;
                case NONE -> SEAPONY_SURFACE_FOG_DIFF;
                default -> DEFAULT_FOG_DIFF;
            };
            fog.environmentalStart += change[0];
            fog.renderDistanceStart += change[0];
            fog.environmentalEnd += change[1];
            fog.renderDistanceEnd += change[1];
        }
    }

    public void track(Entity entity, EntityRenderState state) {
        extractedEntities.put(state, entity);
    }

    @FunctionalInterface
    public interface EntitySubmitter {
        void submit(Entity entity, double x, double y, double z);
    }

    public void handleEntitySubmit(EntityRenderState state, CameraRenderState camera,
            double x, double y, double z, PoseStack matrices, SubmitNodeCollector vertices,
            Runnable original, EntitySubmitter replacement) {
        Entity entity = extractedEntities.get(state);
        if (entity == null) {
            original.run();
            return;
        }
        float tickDelta = camera.cameraEntityPartialTicks;
        Equine<?> equine = Equine.of(entity).orElse(null);
        boolean flipped = equine != null && equine.getPhysics().isGravityNegative();

        matrices.pushPose();
        if (equine instanceof Living living && !living.isBeingCarried()) {
            applyTransforms(living, entity, x, y, z, matrices);
        }
        if (flipped) {
            if (equine instanceof ItemImpl) {
                matrices.translate(0, -entity.getBbHeight() * 1.1, 0);
            }
            flipAngles(entity);
        }

        Disguise disguise = equine instanceof Living living ? disguiseLookup.getAppearanceFor(living).orElse(null) : null;
        if (disguise != null && equine instanceof Living living) {
            disguiseRenderer.prepare(living, disguise);
        }

        boolean smitten = equine instanceof Creature creature && smittenEyesRenderer.isSmitten(creature);
        boolean fishbowl = fishBowlRenderer.shouldRender(equine);
        if (smitten || fishbowl) {
            ModelPartHooks.startCollecting();
        }
        if (disguise == null) {
            original.run();
        } else {
            disguiseRenderer.render(replacement, disguise.getAppearance(), x, y, z, tickDelta, matrices, vertices, camera);
        }
        Set<EnqueudHeadRender> headParts = ModelPartHooks.stopCollecting();
        if (smitten) {
            smittenEyesRenderer.render(headParts, entity, matrices, vertices, state.lightCoords);
        }
        if (fishbowl) {
            fishBowlRenderer.render(headParts, matrices, vertices, state.lightCoords);
        }
        matrices.popPose();
        if (flipped) {
            flipAngles(entity);
        }
    }

    private void applyTransforms(Equine<?> equine, Entity entity, double x, double y, double z, PoseStack matrices) {
        boolean negative = equine.getPhysics().isGravityNegative();

        matrices.translate(x, y + entity.getBbHeight() / 2, z);

        if (equine instanceof Pony p) {
            boolean firstPerson = client.options.getCameraType().isFirstPerson();
            float fovEffectScale = client.options.fovEffectScale().get().floatValue();
            float sidewaysRoll = p.getCamera().calculateRoll(firstPerson, fovEffectScale);

            if (p.getAcrobatics().isFloppy()) {
                matrices.translate(0, -0.5, 0);
                p.asEntity().setYBodyRot(0);
                p.asEntity().setYRot(0);
                sidewaysRoll += 90;
            }

            matrices.rotate(Axis.YP.rotationDegrees(90));
            matrices.rotate(Axis.XP.rotationDegrees(sidewaysRoll));
            matrices.rotate(Axis.YP.rotationDegrees(-90));

            float forwardPitch = p.getInterpolator().interpolate("g_kdive", p.getMotion().isDiving() ? 80 : 0, 15);

            matrices.rotate(Axis.XP.rotationDegrees(forwardPitch));
        } else {
            float roll = negative ? 180 : 0;
            matrices.rotate(Axis.ZP.rotationDegrees(roll));
        }

        matrices.translate(-x, -y - entity.getBbHeight() / 2, -z);
    }

    private void flipAngles(Entity entity) {
        if (entity instanceof Player) {
            entity.yRotO *= -1;
            entity.setYRot(entity.getYRot() * -1);

            entity.xRotO *= -1;
            entity.setXRot(entity.getXRot() * -1);
        }

        if (entity instanceof LivingEntity living) {
            living.yBodyRot = -living.yBodyRot;
            living.yBodyRotO = -living.yBodyRotO;
            living.yHeadRot = -living.yHeadRot;
            living.yHeadRotO = -living.yHeadRotO;
        }
    }
}
