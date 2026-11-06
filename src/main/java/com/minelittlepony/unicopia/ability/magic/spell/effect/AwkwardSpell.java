package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.util.shape.Sphere;

public class AwkwardSpell extends AbstractSpell implements TimedSpell {

    private final Timer timer = new Timer(20);

    protected AwkwardSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation != Situation.PROJECTILE) {
            timer.tick();

            if (timer.getTicksRemaining() <= 0) {
                return false;
            }
        }

        if (source.isClient()) {
            source.spawnParticles(new Sphere(false, (1 + source.getLevel().getScaled(8)) * 8), 10, pos -> {

                List<Identifier> names = new ArrayList<>(BuiltInRegistries.PARTICLE_TYPE.keySet());

                int index = (int)Mth.nextDouble(source.asWorld().getRandom(), 0, names.size());

                Identifier id = names.get(index);
                ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getValue(id);

                if (type instanceof ParticleOptions && shouldSpawnParticle(type)) {
                    source.addParticle((ParticleOptions)type, pos, Vec3.ZERO);
                }
            });
        }

        return true;
    }

    protected boolean shouldSpawnParticle(ParticleType<?> type) {
        return type != ParticleTypes.BLOCK_MARKER
            && type != ParticleTypes.SMOKE
            && type != ParticleTypes.EXPLOSION
            && type != ParticleTypes.EXPLOSION_EMITTER
            && type != ParticleTypes.ENTITY_EFFECT
            && type != ParticleTypes.EFFECT
            && type != ParticleTypes.INSTANT_EFFECT;
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        timer.toNBT(compound, lookup);
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        timer.fromNBT(compound, lookup);
    }
}
