package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractAreaEffectSpell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.shape.Sphere;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Simple fire spell that triggers an effect when used on a block.
 */
public class FireSpell extends AbstractAreaEffectSpell implements ProjectileDelegate.BlockHitListener, ProjectileDelegate.EntityHitListener {
    public static final SpellTraits DEFAULT_TRAITS = SpellTraits.builder()
            .with(Trait.FIRE, 15)
            .build();

    protected FireSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockHitResult hit) {
        if (!projectile.isClient()) {
            Vec3 pos = hit.getLocation();
            projectile.asWorld().explode(projectile.getOwner(), pos.x(), pos.y(), pos.z(), 2, ExplosionInteraction.MOB);
        }
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        if (!projectile.isClient()) {
            Entity entity = hit.getEntity();
            projectile.asWorld().explode(projectile.getOwner(), entity.getX(), entity.getY(), entity.getZ(), 2, ExplosionInteraction.MOB);
        }
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (source.isClient()) {
            generateParticles(source);
        }

        return AbstractAreaEffectSpell.randomBlockPositions(source, false, RANGE.get(getTraits())).reduce(false,
                (r, i) -> source.canModifyAt(i) && applyBlocks(source.asWorld(), i),
                (a, b) -> a || b)
                || applyEntities(source, source.getOriginVector());
    }

    protected void generateParticles(Caster<?> source) {
        source.spawnParticles(new Sphere(false, RANGE.get(getTraits())), (int)(1 + source.getLevel().getScaled(8)) * 6, pos -> {
            source.addParticle(ParticleTypes.LARGE_SMOKE, pos, Vec3.ZERO);
        });
    }

    protected boolean applyBlocks(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (!state.isAir()) {
            if (state.is(Blocks.NETHERRACK)) {
                if (world.isEmptyBlock(pos.above())) {

                    if (world.getRandom().nextInt(300) == 0) {
                        world.setBlockAndUpdate(pos.above(), Blocks.FIRE.defaultBlockState());
                    }

                    return true;
                }
            } else if (state.is(Blocks.REDSTONE_WIRE)) {
                int power = world.getRandom().nextInt(5) == 3 ? 15 : 3;

                sendPower(world, pos, power, 3, 0);

                return true;
            } else if (state.is(BlockTags.SAND) && world.getRandom().nextInt(10) == 0) {
                if (isSurroundedBySand(world, pos)) {
                    world.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());

                    playEffect(world, pos);
                    return true;
                }
            } else if (state.is(BlockTags.LEAVES)) {
                if (world.isEmptyBlock(pos.above())) {
                    world.setBlockAndUpdate(pos.above(), Blocks.FIRE.defaultBlockState());

                    playEffect(world, pos);
                    return true;
                }
            } else if (StateMaps.FIRE_AFFECTED.convert(world, pos)) {
                playEffect(world, pos);
                return true;
            }
        }

        return false;
    }

    protected float getEntityEffectRange() {
        return Math.max(0, RANGE.get(getTraits()) - 1);
    }

    protected boolean applyEntities(Caster<?> source, Vec3 pos) {
        return source.findAllEntitiesInRange(getEntityEffectRange(), e -> {
            LivingEntity master = source.getMaster();
            return (!(e.equals(source.asEntity()) || e.equals(master)) ||
                    (master instanceof Player && !EquinePredicates.PLAYER_UNICORN.test(master))) && !(e instanceof ItemEntity)
                    && !(e instanceof Caster<?>);
        }).filter(e -> {
            e.igniteForSeconds(60);
            if (!source.isClient()) {
                e.hurtServer(source.asServerWorld(), getDamageCause(source, e), 0.1f);
            }
            playEffect(source.asWorld(), e.blockPosition());
            return true;
        })
        .count() > 0;
    }

    protected DamageSource getDamageCause(Caster<?> source, Entity target) {
        return source.damageOf(DamageTypes.IN_FIRE, source);
    }

    /**
     * Transmits power to a piece of redstone
     */
    private void sendPower(Level w, BlockPos pos, int power, int max, int i) {
        BlockState state = w.getBlockState(pos);
        Block id = state.getBlock();

        if (i < max && id == Blocks.REDSTONE_WIRE) {
            i++;

            w.setBlockAndUpdate(pos, state.setValue(RedstoneWireBlock.POWER, power));

            sendPower(w, pos.above(), power, max, i);
            sendPower(w, pos.below(), power, max, i);
            sendPower(w, pos.north(), power, max, i);
            sendPower(w, pos.south(), power, max, i);
            sendPower(w, pos.east(), power, max, i);
            sendPower(w, pos.west(), power, max, i);
        }
    }

    protected void playEffect(Level world, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        world.playSound(null, pos, USounds.SPELL_FIRE_CRACKLE, SoundSource.AMBIENT, 0.5F, 2.6F + (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.8F);

        for (int i = 0; i < 8; ++i) {
            ParticleUtils.spawnParticle(world, ParticleTypes.LARGE_SMOKE, new Vec3(
                    x + Math.random(),
                    y + Math.random(),
                    z + Math.random()
            ), Vec3.ZERO);
        }
    }

    public static boolean isSurroundedBySand(Level w, BlockPos pos) {
        return isSand(w, pos.above()) && isSand(w, pos.below()) &&
                isSand(w, pos.north()) && isSand(w, pos.south()) &&
                isSand(w, pos.east()) && isSand(w, pos.west());
    }

    public static boolean isSand(Level world, BlockPos pos) {
        Block id = world.getBlockState(pos).getBlock();
        return id == Blocks.SAND || id == Blocks.GLASS;
    }

}
