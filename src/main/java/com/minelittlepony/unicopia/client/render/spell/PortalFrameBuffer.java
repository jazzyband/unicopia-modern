package com.minelittlepony.unicopia.client.render.spell;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.RenderUtil;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.mixin.client.GameRendererAccessor;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;

/** A destination view rendered after the main world pass for use on the next frame. */
public final class PortalFrameBuffer implements AutoCloseable {
    private static final int MAX_CACHED_PORTALS = 8;
    private static final Map<UUID, PortalFrameBuffer> CACHE = new LinkedHashMap<>(16, 0.75F, true);
    private static final LinkedHashSet<PortalFrameBuffer> PENDING = new LinkedHashSet<>();
    private static boolean capturing;
    private static ClientLevel cachedLevel;
    private static Camera activeCamera;

    public static boolean isPortalCamera(Camera camera) {
        return camera == activeCamera;
    }

    static PortalFrameBuffer get(UUID id) {
        Minecraft client = Minecraft.getInstance();
        if (cachedLevel != client.level) {
            CACHE.values().forEach(PortalFrameBuffer::close);
            CACHE.clear();
            cachedLevel = client.level;
            PENDING.clear();
        }
        PortalFrameBuffer buffer = CACHE.get(id);
        if (buffer == null) {
            if (CACHE.size() >= MAX_CACHED_PORTALS) {
                UUID oldest = CACHE.keySet().iterator().next();
                CACHE.remove(oldest).close();
            }
            buffer = new PortalFrameBuffer(id);
            CACHE.put(id, buffer);
        }
        return buffer;
    }

    public static void capturePending() {
        if (PENDING.isEmpty() || capturing) {
            return;
        }
        List<PortalFrameBuffer> requested = List.copyOf(PENDING);
        PENDING.clear();
        capturing = true;
        try {
            for (PortalFrameBuffer buffer : requested) {
                if (buffer.request != null) {
                    buffer.capture();
                }
            }
        } finally {
            capturing = false;
        }
    }

    private final Minecraft client = Minecraft.getInstance();
    private final Identifier textureId;
    private PortalTexture texture;
    private TextureTarget swapBuffer;
    private PortalRequest request;
    private long lastCapture;

    private PortalFrameBuffer(UUID id) {
        textureId = Unicopia.id("portal_surface/" + id);
    }

    Identifier textureId() {
        return textureId;
    }

    boolean hasTexture() {
        return texture != null;
    }

    void request(PortalSpellRenderer.State state) {
        if (capturing || client.level == null) {
            return;
        }
        long refreshRate = Unicopia.getConfig().fancyPortalRefreshRate.get();
        if (texture != null && refreshRate > 0 && System.currentTimeMillis() - lastCapture < refreshRate) {
            return;
        }
        request = new PortalRequest(new Matrix4f(state.positionMatrix), state.pitchChange, state.yawChange);
        PENDING.add(this);
    }

    private void capture() {
        if (client.level == null || client.player == null || client.gameMode == null) {
            return;
        }
        var renderer = client.gameRenderer;
        var mainTarget = renderer.mainRenderTarget();
        int width = mainTarget.width;
        int height = mainTarget.height;
        if (width <= 0 || height <= 0) {
            return;
        }
        if (texture == null) {
            texture = new PortalTexture(textureId, width, height, mainTarget.getColorTexture().getFormat());
        } else {
            texture.resize(width, height);
        }
        if (swapBuffer == null) {
            swapBuffer = new TextureTarget("Unicopia Portal Swap", width, height,
                    mainTarget.getColorTexture().getFormat(), mainTarget.getDepthTexture().getFormat());
        } else {
            swapBuffer.resize(width, height);
        }

        Entity previousCamera = client.getCameraEntity();
        if (previousCamera == null) {
            return;
        }
        var mainCamera = renderer.mainCamera();
        Vector4f transformed = request.transform.transform(new Vector4f(mainCamera.position().toVector3f(), 1));
        VirtualCameraEntity portalCamera = new VirtualCameraEntity(client.level);
        portalCamera.setPos(transformed.x, transformed.y, transformed.z);
        portalCamera.setXRot(Mth.clamp(mainCamera.xRot() + request.pitchChange, -90, 90));
        portalCamera.setYRot(Mth.wrapDegrees(mainCamera.yaw() + request.yawChange));
        portalCamera.setOldPosAndRot();

        var tick = client.getDeltaTracker();
        RenderSystem.backupProjectionMatrix();
        RenderUtil.copyBufferToBuffer(mainTarget, swapBuffer);
        try {
            Camera destinationCamera = new Camera();
            destinationCamera.setLevel(client.level);
            destinationCamera.setEntity(portalCamera);
            activeCamera = destinationCamera;
            destinationCamera.update(tick);
            destinationCamera.tick();
            ((GameRendererAccessor)renderer).unicopia$setMainCamera(destinationCamera);
            renderer.extract(tick, true);
            renderer.renderLevel();
            RenderUtil.copyBufferToTexture(mainTarget, texture.getTexture());
            lastCapture = System.currentTimeMillis();
        } finally {
            ((GameRendererAccessor)renderer).unicopia$setMainCamera(mainCamera);
            activeCamera = null;
            try {
                RenderUtil.copyBufferToBuffer(swapBuffer, mainTarget);
                renderer.extract(tick, true);
            } finally {
                RenderSystem.restoreProjectionMatrix();
            }
        }
    }

    @Override
    public void close() {
        if (texture != null) {
            client.getTextureManager().release(textureId);
            texture = null;
        }
        if (swapBuffer != null) {
            swapBuffer.destroyBuffers();
            swapBuffer = null;
        }
    }

    private record PortalRequest(Matrix4f transform, float pitchChange, float yawChange) {}

    private static final class PortalTexture extends AbstractTexture {
        private int width;
        private int height;
        private final GpuFormat format;

        PortalTexture(Identifier id, int width, int height, GpuFormat format) {
            this.format = format;
            this.sampler = RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE,
                    AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, 1, OptionalDouble.empty());
            resize(width, height);
            Minecraft.getInstance().getTextureManager().register(id, this);
        }

        void resize(int width, int height) {
            if (this.width == width && this.height == height) {
                return;
            }
            if (textureView != null) {
                textureView.close();
            }
            if (texture != null) {
                texture.close();
            }
            this.width = width;
            this.height = height;
            texture = RenderSystem.getDevice().createTexture("Unicopia Portal Color",
                    GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
                    format, width, height, 1, 1);
            textureView = RenderSystem.getDevice().createTextureView(texture);
        }
    }

    private static final class VirtualCameraEntity extends Entity {
        private static final EntityDimensions DIMENSIONS = EntityDimensions.fixed(0, 0);

        VirtualCameraEntity(ClientLevel level) {
            super(UEntities.CAST_SPELL, level);
        }

        @Override
        public EntityDimensions getDimensions(Pose pose) { return DIMENSIONS; }

        @Override
        protected void defineSynchedData(Builder builder) {}

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }

        @Override
        protected void readAdditionalSaveData(ValueInput input) {}

        @Override
        protected void addAdditionalSaveData(ValueOutput output) {}
    }
}
