package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.shape.Sphere;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class GroundPoundParticle extends Particle {

    public GroundPoundParticle(SimpleParticleType effect, ClientLevel world, double x, double y, double z, double dX, double dY, double dZ) {
        super(world, x, y, z, dX, dY, dZ);
        lifetime = 10;
    }

    @Override
    public ParticleRenderType getGroup() {
        return ParticleRenderType.NO_RENDER;
    }

    protected Vec3 getPos() {
        return new Vec3(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        spawnChildParticles();
    }

    protected void spawnChildParticles() {
        Vec3 vel = new Vec3(0, (0.5 + (Math.sin(age) * 2.5)) * 5, 0);

        new Sphere(true, age, 1, 0, 1).translate(getPos()).randomPoints(random).forEach(point -> {
            BlockPos pos = BlockPos.containing(point).below();

            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                state = level.getBlockState(pos.below());
                if (state.isAir()) {
                    state = Blocks.DIRT.defaultBlockState();
                }
            }

            ParticleUtils.spawnParticle(level, new BlockParticleOption(ParticleTypes.BLOCK, state), point, vel);
        });
    }
}
