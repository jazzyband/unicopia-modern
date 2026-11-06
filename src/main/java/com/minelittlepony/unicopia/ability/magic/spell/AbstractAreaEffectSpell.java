package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

public abstract class AbstractAreaEffectSpell extends AbstractSpell {

    public static final int MIN_RANGE = 3;
    public static final int MID_RANGE = 15;
    public static final int MAX_RANGE = 32;

    protected static final SpellAttribute<Float> RANGE = range(4);
    public static final TooltipFactory TOOLTIP = RANGE;

    public static SpellAttribute<Float> range(int base) {
        return SpellAttribute.create(SpellAttributeType.RANGE, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.POWER, power -> Mth.clamp(base + power, MIN_RANGE, MAX_RANGE));
    }

    public static Stream<BlockPos> randomBlockPositions(Caster<?> source, boolean hollow, float radius) {
        var shape = new Sphere(hollow, radius).translate(source.getOrigin());
        if (radius > MID_RANGE) {
            // limit updates to 15 blocks per tick if the range is larger than 15 blocks.
            return shape.randomBlockPositions(source.asWorld().getRandom()).limit(15);
        }
        return shape.getBlockPositions();
    }

    public static Stream<BlockPos> allBlockPositions(Caster<?> source, Shape area) {
        var lowerBound = area.getLowerBound();
        var upperBound = area.getUpperBound();
        BlockPos.MutableBlockPos from = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos to = new BlockPos.MutableBlockPos();
        return getSectionPositions(lowerBound, upperBound).flatMap(section -> {
            if (!source.asWorld().hasChunk(section.getX(), section.getZ())) {
                return Stream.empty();
            }

            var chunk = source.asWorld().getChunk(section.getX(), section.getZ());

            var sec = chunk.getSection(chunk.getSectionIndex((section.getY() * 16) + 1));
            if (sec.hasOnlyAir()) {
                return Stream.empty();
            }

            return BlockPos.betweenClosedStream(from.set(
                    Math.max((int)lowerBound.x, section.getX() * 16),
                    Math.max((int)lowerBound.y, section.getY() * 16),
                    Math.max((int)lowerBound.z, section.getZ() * 16)
            ), to.set(
                    Math.min((int)upperBound.x, (section.getX() + 1) * 16),
                    Math.min((int)upperBound.y, (section.getY() + 1) * 16),
                    Math.min((int)upperBound.z, (section.getZ() + 1) * 16)
            ))
                    .filter(pos -> area.isPointInside(Vec3.atCenterOf(pos)) && source.canModifyAt(pos));
        });
    }

    public static Stream<BlockPos> getSectionPositions(Vec3 lowerBound, Vec3 upperBound) {
        return BlockPos.betweenClosedStream(
                BlockPos.containing(lowerBound.scale(1/16D)),
                BlockPos.containing(upperBound.scale(1/16D))
        );
    }

    protected AbstractAreaEffectSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return toPlaceable();
    }
}
