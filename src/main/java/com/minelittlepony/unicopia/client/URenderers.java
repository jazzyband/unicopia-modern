package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.block.*;
import com.minelittlepony.unicopia.client.particle.ChangelingMagicParticle;
import com.minelittlepony.unicopia.client.particle.CloudsEscapingParticle;
import com.minelittlepony.unicopia.client.particle.CustomParticleGroup;
import com.minelittlepony.unicopia.client.particle.DiskParticle;
import com.minelittlepony.unicopia.client.particle.DustCloudParticle;
import com.minelittlepony.unicopia.client.particle.FloatingBubbleParticle;
import com.minelittlepony.unicopia.client.particle.FootprintParticle;
import com.minelittlepony.unicopia.client.particle.GroundPoundParticle;
import com.minelittlepony.unicopia.client.particle.HealthDrainParticle;
import com.minelittlepony.unicopia.client.particle.LightningBoltParticle;
import com.minelittlepony.unicopia.client.particle.MagicParticle;
import com.minelittlepony.unicopia.client.particle.RainboomParticle;
import com.minelittlepony.unicopia.client.particle.RainbowTrailParticle;
import com.minelittlepony.unicopia.client.particle.RaindropsParticle;
import com.minelittlepony.unicopia.client.particle.ShockwaveParticle;
import com.minelittlepony.unicopia.client.particle.SphereParticle;
import com.minelittlepony.unicopia.client.particle.SpiralParticle;
import com.minelittlepony.unicopia.client.particle.WindParticle;
import com.minelittlepony.unicopia.client.render.*;
import com.minelittlepony.unicopia.client.render.entity.*;
import com.minelittlepony.unicopia.client.render.item.BlockTintSource;
import com.minelittlepony.unicopia.client.render.item.UItemPredicates;
import com.minelittlepony.unicopia.client.render.shader.URenderPipelines;
import com.minelittlepony.unicopia.client.render.spell.SpellRendererFactory;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.particle.UParticles;

import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.PendingParticleProvider;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.Block;

public interface URenderers {

    static void bootstrap() {
        CustomParticleGroup.register();
        ParticleProviderRegistry.getInstance().register(UParticles.UNICORN_MAGIC, createFactory(MagicParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.CHANGELING_MAGIC, createFactory(ChangelingMagicParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.BUBBLE, createFactory(FloatingBubbleParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.RAIN_DROPS, createFactory(RaindropsParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.HEALTH_DRAIN, createFactory(HealthDrainParticle::create));
        ParticleProviderRegistry.getInstance().register(UParticles.FOOTPRINT, createFactory(FootprintParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.SPIRAL, createFactory(SpiralParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.RAINBOOM_RING, createProvider(RainboomParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.RAINBOOM_TRAIL, createProvider(RainbowTrailParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.WIND, createProvider(WindParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.SHOCKWAVE, createProvider(ShockwaveParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.SPHERE, createProvider(SphereParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.DISK, createProvider(DiskParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.GROUND_POUND, createProvider(GroundPoundParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.CLOUDS_ESCAPING, createProvider(CloudsEscapingParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.LIGHTNING_BOLT, createProvider(LightningBoltParticle::new));
        ParticleProviderRegistry.getInstance().register(UParticles.DUST_CLOUD, createProvider(DustCloudParticle::new));

        AccessoryFeatureRenderer.register(
                BraceletFeatureRenderer::new, AmuletFeatureRenderer::new, GlassesFeatureRenderer::new,
                WingsFeatureRenderer::new, HornFeatureRenderer::new, IcarusWingsFeatureRenderer::new, BatWingsFeatureRenderer::new,
                HeldEntityFeatureRenderer::new, DisguisedArmsFeatureRenderer::new
        );

        EntityRendererRegistry.register(UEntities.THROWN_ITEM, ThrownItemRenderer::new);
        EntityRendererRegistry.register(UEntities.MUFFIN, ThrownItemRenderer::new);
        EntityRendererRegistry.register(UEntities.MAGIC_BEAM, MagicBeamEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.BUTTERFLY, ButterflyEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.FLOATING_ARTEFACT, FloatingArtefactEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.CAST_SPELL, CastSpellEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.TWITTERMITE, FairyEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.SPELLBOOK, SpellbookEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.SOMBRA, SombraEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.CRYSTAL_SHARDS, CrystalShardsEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.STORM_CLOUD, StormCloudEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.AIR_BALLOON, AirBalloonEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.FRIENDLY_CREEPER, FriendlyCreeperEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.LOOT_BUG, LootBugEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.TENTACLE, TentacleEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.IGNOMINIOUS_BULB, IgnominiousBulbEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.SPECTER, SpecterEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.MIMIC, MimicEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.PALM_BOAT, ctx -> new CustomBoatEntityRenderer<>(ctx, UEntities.PALM_BOAT, false));
        EntityRendererRegistry.register(UEntities.PALM_CHEST_BOAT, ctx -> new CustomBoatEntityRenderer<>(ctx, UEntities.PALM_BOAT, true));
        EntityRendererRegistry.register(UEntities.LEVITATING_ITEM, LevitatingItemEntityRenderer::new);

        BlockEntityRenderers.register(UBlockEntities.WEATHER_VANE, WeatherVaneBlockEntityRenderer::new);
        BlockEntityRenderers.register(UBlockEntities.FANCY_BED, CloudBedBlockEntityRenderer::new);
        BlockEntityRenderers.register(UBlockEntities.CLOUD_CHEST, CloudChestBlockEntityRenderer::new);
        BlockEntityRenderers.register(UBlockEntities.ITEM_JAR, ItemJarBlockEntityRenderer::new);

        BlockColorRegistry.register((state, view, pos, tints) ->
                tints.add(BlockTintSource.getColor(state, view, pos, 0)),
                TintedBlock.REGISTRY.stream().toArray(Block[]::new));
        LeavesAdditionsModel.bootstrap();

        UItemPredicates.bootstrap();
        SpellRendererFactory.bootstrap();
        URenderPipelines.bootstrap();
    }

    static <T extends ParticleOptions> PendingParticleProvider<T> createFactory(ParticleSupplier<T> supplier) {
        return provider -> (effect, world, x, y, z, dx, dy, dz, random) -> supplier.get(effect, provider, world, x, y, z, dx, dy, dz);
    }

    static <T extends ParticleOptions> ParticleProvider<T> createProvider(DirectParticleSupplier<T> supplier) {
        return (effect, world, x, y, z, dx, dy, dz, random) -> supplier.get(effect, world, x, y, z, dx, dy, dz);
    }

    interface DirectParticleSupplier<T extends ParticleOptions> {
        Particle get(T effect, ClientLevel world, double x, double y, double z, double dx, double dy, double dz);
    }

    interface ParticleSupplier<T extends ParticleOptions> {
        Particle get(T effect, SpriteSet provider, ClientLevel world, double x, double y, double z, double dx, double dy, double dz);
    }
}
