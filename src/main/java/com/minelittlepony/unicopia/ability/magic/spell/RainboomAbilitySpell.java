package com.minelittlepony.unicopia.ability.magic.spell;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.OrientedBillboardParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleSpawner;
import com.minelittlepony.unicopia.particle.TargetBoundParticleEffect;
import com.minelittlepony.unicopia.server.world.ModificationType;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Internal.
 * <p>
 * Used by the Rainboom ability.
 */
public class RainboomAbilitySpell extends AbstractSpell {

    private static final int RADIUS = 5;
    private static final Shape EFFECT_RANGE = new Sphere(false, RADIUS);

    @Nullable
    private ParticleSpawner boundParticle;

    private int age;

    public RainboomAbilitySpell(CustomisedSpellType<?> type) {
        super(type);
        setHidden(true);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation != Situation.BODY || !source.canUse(Abilities.RAINBOOM)) {
            return false;
        }

        if (source instanceof Pony pony && !pony.getPhysics().isFlying()) {
            return false;
        }

        if (source.isClient()) {
            if (boundParticle == null) {
                boundParticle = InteractionManager.getInstance().createBoundParticle(getUuid());
            }
            boundParticle.addParticle(new TargetBoundParticleEffect(UParticles.RAINBOOM_TRAIL, source.asEntity()), source.getOriginVector(), Vec3.ZERO);

            if (age == 0) {
                source.addParticle(new OrientedBillboardParticleEffect(UParticles.RAINBOOM_RING, source.getPhysics().getMotionAngle()), source.getOriginVector(), Vec3.ZERO);
            }
        } else {
            source.findAllEntitiesInRange(RADIUS, e -> !source.isOwnerOrFriend(e)).forEach(e -> {
                e.hurtServer((ServerLevel)source.asWorld(), source.damageOf(UDamageTypes.RAINBOOM, source), 6);
            });
        }
        EFFECT_RANGE.translate(source.getOrigin()).getBlockPositions().forEach(pos -> {
            BlockState state = source.asWorld().getBlockState(pos);
            if (state.is(UTags.Blocks.FRAGILE) && source.canModifyAt(pos, ModificationType.PHYSICAL)) {
                source.asWorld().destroyBlock(pos, true);
            }
        });

        Vec3 motion = source.asEntity().getViewVector(1).scale(1.5);
        Vec3 velocity = source.asEntity().getDeltaMovement().add(motion);

        while (velocity.length() > 3) {
            velocity = velocity.scale(0.6);
        }

        source.asEntity().setDeltaMovement(velocity);
        if (source instanceof Pony pony) {
            pony.getMagicalReserves().getExhaustion().multiply(0.2F);
        }

        return !source.asEntity().isRemoved() && age++ < 90 + 7 * source.getLevel().getScaled(9);
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        compound.putInt("age", age);
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        age = compound.getIntOr("age", 0);
        boundParticle = null;
    }
}
