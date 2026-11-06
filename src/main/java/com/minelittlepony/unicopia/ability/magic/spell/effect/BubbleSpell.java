package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Map;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.shape.Sphere;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.HolderLookup;

public class BubbleSpell extends AbstractSpell implements TimedSpell, ProjectileDelegate.EntityHitListener {
    private static final Identifier EFFECT_ID = Unicopia.id("bubble_floating_modifier");
    private static final AttributeModifier GRAVITY_MODIFIER = new AttributeModifier(EFFECT_ID, 0.02D - 1D, Operation.ADD_MULTIPLIED_TOTAL);
    private static final AttributeModifier SPEED_MODIFIER = new AttributeModifier(EFFECT_ID, 0.01D - 1D, Operation.ADD_MULTIPLIED_TOTAL);

    private static final Map<Holder<Attribute>, AttributeModifier> MODIFIERS = Map.of(
            UEntityAttributes.ENTITY_GRAVITY_MODIFIER, GRAVITY_MODIFIER,
            UEntityAttributes.EXTENDED_REACH_DISTANCE, GRAVITY_MODIFIER,
            UEntityAttributes.EXTENDED_ATTACK_DISTANCE, GRAVITY_MODIFIER,
            Attributes.MOVEMENT_SPEED, SPEED_MODIFIER,
            Attributes.FLYING_SPEED, SPEED_MODIFIER
    );

    public static final SpellTraits DEFAULT_TRAITS = SpellTraits.builder()
            .with(Trait.FOCUS, 6)
            .with(Trait.POWER, 1)
            .build();

    private static final SpellAttribute<Integer> SOAPINESS = SpellAttribute.create(SpellAttributeType.SOAPINESS, AttributeFormat.REGULAR, Trait.POWER, power -> (int)(power * 2));

    static final TooltipFactory TOOLTIP = TooltipFactory.of(TimedSpell.TIME, SOAPINESS);

    private final Timer timer = new Timer(TIME.get(getTraits()));

    private float prevRadius;
    private DataTracker.Entry<Float> radius = dataTracker.startTracking(TrackableDataType.FLOAT, 0F);
    private DataTracker.Entry<Integer> struggles = dataTracker.startTracking(TrackableDataType.INT, SOAPINESS.get(getTraits()));

    protected BubbleSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    public float getRadius(float tickDelta) {
        return Mth.lerp(tickDelta, prevRadius, radius.get());
    }

    @Override
    public boolean apply(Caster<?> source) {

        if (source.getSpellSlot().removeWhere(getType())) {
            return false;
        }

        Entity entity = source.asEntity();

        if (source instanceof AttributeContainer l) {
            l.applyAttributeModifiers(MODIFIERS, false, true);
        }
        radius.set(Math.max(entity.getBbHeight(), entity.getBbWidth()) * 1.2F);
        source.playSound(USounds.ENTITY_PLAYER_UNICORN_TELEPORT, 1);
        entity.push(0, 0.2F * source.getPhysics().getGravitySignum(), 0);
        Living.updateVelocity(entity);

        return super.apply(source);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation == Situation.PROJECTILE) {
            source.spawnParticles(UParticles.BUBBLE, 2);
            return true;
        }

        timer.tick();

        boolean done = timer.getTicksRemaining() <= 0;

        source.spawnParticles(source.getOriginVector().add(0, 1, 0), new Sphere(true, radius.get() * (done ? 0.25F : 0.5F)), done ? 13 : 1, pos -> {
            source.addParticle(done ? ParticleTypes.BUBBLE_POP : UParticles.BUBBLE, pos, Vec3.ZERO);
        });

        if (done) {
            return false;
        }

        source.asEntity().push(
                Mth.sin(source.asEntity().tickCount / 6F) / 50F,
                Mth.sin(source.asEntity().tickCount / 6F) / 50F,
                Mth.sin(source.asEntity().tickCount / 6F) / 50F
        );

        source.asEntity().fallDistance = 0;

        prevRadius = radius.get();

        if (source instanceof Pony pony && pony.sneakingChanged() && pony.asEntity().isShiftKeyDown()) {
            radius.set(radius.get() + 0.5F);
            source.playSound(USounds.SPELL_BUBBLE_DISTURB, 1);
            int s = struggles.get() - 1;
            struggles.set(s);
            if (s <= 0) {
                setDead();
                return false;
            }
        }

        return !isDead();
    }

    @Override
    protected void onDestroyed(Caster<?> source) {
        super.onDestroyed(source);
        if (source instanceof AttributeContainer l) {
            l.applyAttributeModifiers(MODIFIERS, false, false);
        }
        source.playSound(USounds.ENTITY_PLAYER_UNICORN_TELEPORT, 1);
    }


    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        Caster.of(hit.getEntity()).ifPresent(caster -> {
            getTypeAndTraits().apply(caster, CastingMethod.INDIRECT);
        });
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        compound.putInt("struggles", struggles.get());
        compound.putFloat("radius", radius.get());
        timer.toNBT(compound, lookup);
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        struggles.set(compound.getIntOr("struggles", 0));
        radius.set(compound.getFloatOr("radius", 0));
        timer.fromNBT(compound, lookup);
    }
}
