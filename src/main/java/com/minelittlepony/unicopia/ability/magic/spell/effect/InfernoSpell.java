package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.block.state.BlockStateConverter;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Converts surrounding blocks and entities into their nether equivalent
 */
public class InfernoSpell extends FireSpell {

    protected InfernoSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (source.isClient()) {
            generateParticles(source);
        }

        Level w = source.asWorld();

        if (!w.isClientSide()) {
            float radius = 4 + (source.getLevel().getScaled(4) * 4);
            Shape shape = new Sphere(false, radius);

            Vec3 origin = source.getOriginVector();

            BlockStateConverter converter = w.dimension().equals(net.minecraft.world.level.Level.NETHER) ? StateMaps.HELLFIRE_AFFECTED.getInverse() : StateMaps.HELLFIRE_AFFECTED;

            for (int i = 0; i < radius; i++) {
                BlockPos pos = BlockPos.containing(shape.computePoint(w.getRandom()).add(origin));

                if (source.canModifyAt(pos) && converter.convert(w, pos)) {
                    playEffect(w, pos);
                }
            }

            shape = new Sphere(false, radius - 1);
            for (int i = 0; i < radius * 2; i++) {
                if (w.getRandom().nextInt(12) == 0) {
                    Vec3 vec = shape.computePoint(w.getRandom()).add(origin);
                    BlockPos pos = BlockPos.containing(vec);

                    if (source.canModifyAt(pos) && !applyBlocks(w, pos)) {
                        applyEntities(source, vec);
                    }
                }
            }
        }
        return true;
    }
}
