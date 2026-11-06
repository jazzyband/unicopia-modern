package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.EntityReference.EntityValues;
import com.minelittlepony.unicopia.entity.mob.FairyEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.VecHelper;

public class LightSpell extends AbstractSpell implements TimedSpell, ProjectileDelegate.HitListener {
    public static final SpellTraits DEFAULT_TRAITS = SpellTraits.builder()
            .with(Trait.LIFE, 10)
            .with(Trait.AIR, 0.3F)
            .with(Trait.KINDNESS, 12)
            .with(Trait.ORDER, 25)
            .build();

    private static final SpellAttribute<Integer> ORB_COUNT = SpellAttribute.create(SpellAttributeType.ORB_COUNT, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.LIFE, life -> 2 + (int)(Mth.clamp(life, 10, 20) / 10F));

    static final TooltipFactory TOOLTIP = TooltipFactory.of(TIME, ORB_COUNT);

    private final Timer timer = new Timer(TIME.get(getTraits()));

    private final List<EntityReference<FairyEntity>> lights = new ArrayList<>();

    protected LightSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {

        if (situation == Situation.PROJECTILE) {
            return false;
        }

        timer.tick();

        if (timer.getTicksRemaining() <= 0) {
            return false;
        }

        if (!caster.isClient()) {
            if (lights.isEmpty()) {
                int size = caster.asWorld().getRandom().nextInt(2) + ORB_COUNT.get(getTraits());
                while (lights.size() < size) {
                    lights.add(new EntityReference<>());
                }
            }

            lights.forEach(ref -> {
                if (ref.getOrEmpty(caster.asWorld()).isEmpty()) {
                    FairyEntity entity = UEntities.TWITTERMITE.create(caster.asWorld(), EntitySpawnReason.EVENT);
                    entity.setPos(ref.getTarget().map(EntityValues::pos).orElseGet(() -> {
                        return caster.getOriginVector().add(VecHelper.supply(() -> caster.asWorld().getRandom().nextInt(3) - 1));
                    }));
                    entity.setMaster(caster);
                    entity.level().addFreshEntity(entity);

                    ref.set(entity);
                }
            });
        }

        return true;
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile) {
        Caster.of(projectile.getMaster()).ifPresent(caster -> getTypeAndTraits().apply(caster, CastingMethod.INDIRECT));
    }

    @Override
    protected void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        if (caster.isClient()) {
            return;
        }
        lights.forEach(ref -> {
            ref.ifPresent(caster.asWorld(), e -> {
                e.level().broadcastEntityEvent(e, (byte)60);
                e.discard();
            });
        });
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        timer.toNBT(compound, lookup);
        if (!lights.isEmpty()) {
            ListTag list = new ListTag();
            lights.forEach(light -> {
                list.add(light.toNBT(lookup));
            });
            compound.put("lights", list);
        }
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        timer.fromNBT(compound, lookup);
        lights.clear();
        compound.read("lights", EntityReference.<FairyEntity>listCodec()).ifPresent(lights::addAll);
    }
}
