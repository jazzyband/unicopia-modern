package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.Util;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.RegistryUtils;

/**
 * Transforms whatever entity it strikes into a random other entity.
 */
public class TransformationSpell extends AbstractSpell implements ProjectileDelegate.EntityHitListener {
    protected TransformationSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        return situation == Situation.PROJECTILE;
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        if (projectile.level().isClientSide()) {
            return;
        }
        Entity entity = hit.getEntity();
        pickType(entity.getType(), entity.level()).flatMap(type -> convert(entity, type)).ifPresentOrElse(e -> {
            entity.playSound(USounds.SPELL_TRANSFORM_TRANSMUTE_ENTITY, 1, 1);
        }, () -> {
            ParticleUtils.spawnParticles(ParticleTypes.SMOKE, entity, 20);
            entity.playSound(USounds.GUI_ABILITY_FAIL, 1, 1);
        });
    }

    private Optional<? extends Mob> convert(Entity entity, EntityType<? extends Mob> type) {
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            try {
                return Optional.ofNullable(mob.convertTo(type, ConversionParams.single(mob, true, true), i -> {}));
            } catch (Exception e) {
                return Optional.ofNullable(mob.convertTo(UEntities.BUTTERFLY, ConversionParams.single(mob, true, true), i -> {}));
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <T extends Mob> Optional<EntityType<T>> pickType(EntityType<?> except, Level world) {
        Set<EntityType<?>> options = RegistryUtils.valuesForTag(world, UTags.Entities.TRANSFORMABLE).collect(Collectors.toSet());
        if (except.getCategory() == MobCategory.MONSTER) {
            options.removeIf(t -> t.getCategory() == MobCategory.MONSTER);
        } else {
            options.remove(except);
        }
        if (options.size() <= 1) {
            return options.stream().findFirst().map(t -> (EntityType<T>)t);
        }
        return Optional.ofNullable((EntityType<T>)Util.getRandom(new ArrayList<>(options), world.getRandom()));
    }
}
