package com.minelittlepony.unicopia.client.render.model;

import org.joml.Vector4f;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class VertexLightSource {
    private final ClientLevel world;
    private final Long2ObjectMap<Integer> lightCache = new Long2ObjectOpenHashMap<>();

    public VertexLightSource(ClientLevel world) {
        this.world = world;
    }

    public void tick() {
        lightCache.clear();
    }

    public int getLight(Vector4f vertexPosition, int light) {
        return lightCache.computeIfAbsent(getBlockPosition(vertexPosition), this::getLight);
    }

    @SuppressWarnings("deprecation")
    private int getLight(long p) {
        final BlockPos pos = BlockPos.of(p);
        return world.hasChunkAt(pos) ? LightCoordsUtil.getLightCoords(world, pos) : 0;
    }

    private long getBlockPosition(Vector4f vertexPosition) {
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.mainCamera().position();
        return BlockPos.asLong(
                Mth.floor(cameraPos.x + vertexPosition.x),
                Mth.floor(cameraPos.y + vertexPosition.y),
                Mth.floor(cameraPos.z + vertexPosition.z)
        );
    }
}
