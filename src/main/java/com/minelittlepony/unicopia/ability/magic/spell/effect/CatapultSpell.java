package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.Affects;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.duck.Hoverable;
import com.minelittlepony.unicopia.projectile.MagicBeamEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

/**
 * Picks up and throws an entity or block.
 */
public class CatapultSpell extends AbstractSpell implements ProjectileDelegate.BlockHitListener, ProjectileDelegate.EntityHitListener {
    public static final SpellTraits DEFAULT_TRAITS = SpellTraits.builder()
            .with(Trait.FOCUS, 50)
            .with(Trait.KNOWLEDGE, 1)
            .with(Trait.EARTH, 60)
            .with(Trait.STRENGTH, 50)
            .build();

    private static final float HORIZONTAL_VARIANCE = 0.25F;
    private static final float MAX_STRENGTH = 120;

    private static final SpellAttribute<Float> LAUNCH_SPEED = SpellAttribute.create(SpellAttributeType.VERTICAL_VELOCITY, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.STRENGTH, strength -> 0.1F + (Mth.clamp(strength, -MAX_STRENGTH, MAX_STRENGTH) - 40) / 16F);
    private static final SpellAttribute<Float> HANG_TIME = SpellAttribute.create(SpellAttributeType.HANG_TIME, AttributeFormat.TIME, AttributeFormat.PERCENTAGE, Trait.AIR, air -> 50 + (int)Mth.clamp(air, 0, 10) * 20F);
    private static final SpellAttribute<Float> PUSHING_POWER = SpellAttribute.create(SpellAttributeType.PUSHING_POWER, AttributeFormat.REGULAR, Trait.POWER, power -> 1 + Mth.clamp(power, 0, 10) / 10F);
    private static final SpellAttribute<Boolean> CAUSES_LEVITATION = SpellAttribute.createConditional(SpellAttributeType.CAUSES_LEVITATION, Trait.FOCUS, focus -> focus > 50);
    private static final SpellAttribute<Affects> AFFECTS = SpellAttribute.createEnumerated(SpellAttributeType.AFFECTS, Trait.ORDER, order -> {
        if (order <= 0) {
            return Affects.BOTH;
        } else if (order <= 10) {
            return Affects.ENTITIES;
        }
        return Affects.BLOCKS;
    });
    static final TooltipFactory TOOLTIP = TooltipFactory.of(LAUNCH_SPEED, HANG_TIME, PUSHING_POWER, CAUSES_LEVITATION, AFFECTS);

    static void appendTooltip(CustomisedSpellType<? extends CatapultSpell> type, List<Component> tooltip) {
        TOOLTIP.appendTooltip(type, tooltip);
    }

    protected CatapultSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockHitResult hit) {
        if (!AFFECTS.get(getTraits()).allowsBlocks()) {
            return;
        }

        if (!projectile.isClient() && projectile instanceof MagicBeamEntity source && source.canModifyAt(hit.getBlockPos())) {
            createBlockEntity(projectile.level(), hit.getBlockPos(), e -> {
                e.setOnGround(true);
                apply(source, e);
                e.setOnGround(false);
            });
        }
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        if (!projectile.isClient() && projectile instanceof MagicBeamEntity source) {
            Entity e = hit.getEntity();
            if (!(e instanceof FallingBlockEntity) && !AFFECTS.get(getTraits()).allowsEntities()) {
                return;
            }

            apply(source, hit.getEntity());
        }
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        if (situation == Situation.PROJECTILE) {
            return true;
        }

        getTypeAndTraits().create().toThrowable().throwProjectile(caster);
        setDead();
        return isDead();
    }

    protected void apply(Caster<?> caster, Entity e) {

        float power = 1 + getTraits().get(Trait.POWER, 0, 10) / 10F;

        if (!e.onGround()) {
            e.setDeltaMovement(caster.asEntity().getDeltaMovement().scale(power));
        } else {
            RandomSource rng = caster.asWorld().getRandom();
            e.push(
                rng.triangle(0, HORIZONTAL_VARIANCE) * 0.1F,
                LAUNCH_SPEED.get(getTraits()),
                rng.triangle(0, HORIZONTAL_VARIANCE) * 0.1F
            );

            int hoverDuration = HANG_TIME.get(getTraits()).intValue();
            boolean noGravity = CAUSES_LEVITATION.get(getTraits());

            if (e instanceof LivingEntity l) {
                if (l.hasEffect(MobEffects.SLOW_FALLING)) {
                    l.removeEffect(MobEffects.SLOW_FALLING);
                }
                l.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, hoverDuration, 1));
            }

            if (noGravity || e instanceof FallingBlockEntity && hasRoof(e.level(), e.blockPosition().above())) {
                if (e instanceof LivingEntity l) {
                    l.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200, 1));
                } else {
                    e.setNoGravity(true);
                    if (e instanceof Hoverable h) {
                        h.setTicksHovering(20);
                    }
                }
            }
        }

    }

    private boolean hasRoof(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !state.canBeReplaced() && !state.getCollisionShape(world, pos).isEmpty();
    }

    static void createBlockEntity(Level world, BlockPos bpos, @Nullable Consumer<Entity> apply) {

        if (world.isEmptyBlock(bpos)) {
            return;
        }

        BlockState state = world.getBlockState(bpos);
        if (state.is(UTags.Blocks.CATAPULT_IMMUNE)) {
            return;
        }

        Vec3 pos = Vec3.atBottomCenterOf(bpos);
        FallingBlockEntity e = new FallingBlockEntity(world, pos.x, pos.y, pos.z, state
                .trySetValue(BlockStateProperties.WATERLOGGED, false)
                .trySetValue(ChestBlock.TYPE, ChestType.SINGLE));
        if (state.hasBlockEntity()) {
            e.blockData = world.getChunk(bpos).getBlockEntityNbtForSaving(bpos, world.registryAccess());
        }

        world.removeBlockEntity(bpos);
        world.removeBlock(bpos, true);

        e.setOnGround(false);
        e.time = Integer.MIN_VALUE;
        e.setHurtsEntities(1 + (world.getRandom().nextFloat() * 10), 100);

        if (apply != null) {
            apply.accept(e);
        }
        world.addFreshEntity(e);
    }
}
