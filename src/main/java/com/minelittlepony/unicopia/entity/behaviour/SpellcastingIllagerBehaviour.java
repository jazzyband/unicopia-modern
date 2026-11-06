package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.world.entity.monster.illager.SpellcasterIllager;

public class SpellcastingIllagerBehaviour extends EntityBehaviour<SpellcasterIllager> {
    @Override
    public void update(Pony player, SpellcasterIllager entity, Disguise s) {
        if (player.sneakingChanged()) {
            SpellCastAccess.setSpell(player, entity, s);
        }
    }

    private static abstract class SpellCastAccess extends SpellcasterIllager {
        SpellCastAccess() {super(null, null);}

        static void setSpell(Pony player, SpellcasterIllager entity, Disguise s) {
            if (player.asEntity().isShiftKeyDown()) {
                SpellcasterIllager.IllagerSpell[] spells = SpellcasterIllager.IllagerSpell.values();
                SpellcasterIllager.IllagerSpell spell = spells[entity.level().getRandom().nextInt(spells.length - 1) + 1];

                entity.setIsCastingSpell(spell);
                entity.setTarget(entity);
            } else {
                entity.setIsCastingSpell(SpellcasterIllager.IllagerSpell.NONE);
                entity.setTarget(null);
            }
        }
    }
}
