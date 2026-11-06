package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractAreaEffectSpell;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.CastOn;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.minelittlepony.unicopia.util.shape.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public class HydrophobicSpell extends AbstractSpell {
    public static final SpellTraits DEFAULT_TRAITS = SpellTraits.builder()
            .with(Trait.FOCUS, 5)
            .with(Trait.KNOWLEDGE, 1)
            .build();

    static final TooltipFactory TOOLTIP = TooltipFactory.of(ShieldSpell.CAST_ON, ShieldSpell.RANGE);

    private final TagKey<Fluid> affectedFluid;

    private final Set<Entry> storedFluidPositions = new HashSet<>();

    protected HydrophobicSpell(CustomisedSpellType<?> type, TagKey<Fluid> affectedFluid) {
        super(type);
        this.affectedFluid = affectedFluid;
    }

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        if ((method == CastingMethod.DIRECT || method == CastingMethod.STAFF) && ShieldSpell.CAST_ON.get(getTraits()) == CastOn.LOCATION) {
            return toPlaceable();
        }
        if (method == CastingMethod.STAFF) {
            return toThrowable();
        }
        return this;
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (!source.isClient()) {
            Level world = source.asWorld();

            double range = getRange(source);
            Shape area = new Sphere(false, range).translate(source.getOriginVector());

            storedFluidPositions.removeIf(entry -> {
               if (!area.isPointInside(Vec3.atCenterOf(entry.pos()))) {
                   if (source.canModifyAt(entry.pos())) {
                       entry.restore(world);
                   }
                   return true;
               }

               return false;
            });

            AbstractAreaEffectSpell.allBlockPositions(source, area).forEach(pos -> {
                BlockState state = world.getBlockState(pos);

                if (state.getFluidState().is(affectedFluid)) {
                    Block block = state.getBlock();

                    if (block instanceof LiquidBlock) {
                        world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                        storedFluidPositions.add(new Entry(pos.immutable(), state));
                    } else if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                        world.setBlock(pos, state.cycle(BlockStateProperties.WATERLOGGED), Block.UPDATE_CLIENTS);
                        storedFluidPositions.add(new Entry(pos.immutable(), state));
                    }
                }
            });

            if (!source.subtractEnergyCost(storedFluidPositions.isEmpty() ? 0.001F : 0.02F)) {
                setDead();
            }

            Ether.get(source.asWorld()).getOrCreate(this, source).setRadius((float)range);

            source.spawnParticles(new Sphere(true, range), 10, pos -> {
                BlockPos bp = BlockPos.containing(pos);
                if (source.canModifyAt(bp) && source.asWorld().getFluidState(bp.above()).is(affectedFluid)) {
                    source.addParticle(UParticles.RAIN_DROPS, pos, Vec3.ZERO);
                }
            });

            if (source.asEntity().tickCount % 200 == 0) {
                source.playSound(USounds.SPELL_AMBIENT, 0.5F);
            }

            if (storedFluidPositions.size() >= 100) {
                UCriteria.SPLIT_SEA.trigger(source.asEntity());
            }
        }

        return !isDead();
    }

    @Override
    protected void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        storedFluidPositions.removeIf(entry -> {
            if (caster.canModifyAt(entry.pos())) {
                entry.restore(caster.asWorld());
            }
            return true;
         });
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        compound.store("storedFluidPositions", Entry.SET_CODEC, lookup.createSerializationContext(NbtOps.INSTANCE), storedFluidPositions);
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        storedFluidPositions.clear();
        compound.read("storedFluidPositions", Entry.SET_CODEC, lookup.createSerializationContext(NbtOps.INSTANCE)).ifPresent(storedFluidPositions::addAll);
    }
    /**
     * Calculates the maximum radius of the shield. aka The area of effect.
     */
    public double getRange(Caster<?> source) {
        float multiplier = 1;
        float min = (source instanceof Pony ? 0 : 2) + ShieldSpell.RANGE.get(getTraits());
        boolean isLimitedRange = source instanceof Pony || source instanceof MagicProjectileEntity;
        double range = (min + (source.getLevel().getScaled(isLimitedRange ? 4 : 40) * (isLimitedRange ? 2 : 10))) / multiplier;
        return range;
    }

    record Entry (BlockPos pos, BlockState blockState) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
            BlockState.CODEC.optionalFieldOf("blockState", Blocks.AIR.defaultBlockState()).forGetter(Entry::blockState)
        ).apply(instance, Entry::new));
        public static final Codec<Set<Entry>> SET_CODEC = CodecUtils.setOf(CODEC);

        void restore(Level world) {
            BlockState state = world.getBlockState(pos);

            if (state.isAir()) {
                if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    world.setBlock(pos, blockState.getFluidState().createLegacyBlock(), Block.UPDATE_CLIENTS);
                } else {
                    world.setBlock(pos, blockState, Block.UPDATE_CLIENTS);
                }
            } else if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                world.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, true), Block.UPDATE_CLIENTS);
            }
        }
    }

    public boolean blocksFlow(Ether.MutableEntry<?> entry, Vec3 center, BlockPos pos, FluidState fluid) {
        return fluid.is(affectedFluid) && pos.closerToCenterThan(center, (double)entry.radius() + 1);
    }

    public static boolean blocksFluidFlow(BlockGetter world, BlockPos pos, FluidState state) {
        if (!(world instanceof ServerLevel sw)) {
            return false;
        }

        return Ether.get(sw).anyMatch(SpellType.HYDROPHOBIC, entry -> {
            var target = entry.entity.getTarget().orElse(null);
            if (target == null || !pos.closerToCenterThan(target.pos(), entry.radius() + 2)) {
                return false;
            }
            var spell = entry.getSpell();
            return spell != null && target != null && spell.blocksFlow(entry, target.pos(), pos, state);
        });
    }
}
