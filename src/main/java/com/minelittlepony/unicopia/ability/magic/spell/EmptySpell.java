package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.UUID;
import net.minecraft.util.Util;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.network.track.DataTracker;

public final class EmptySpell implements Spell {
    public static final EmptySpell INSTANCE = new EmptySpell();

    private EmptySpell() {}

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) { }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) { }

    @Override
    public CustomisedSpellType<?> getTypeAndTraits() {
        return SpellType.EMPTY_KEY.withTraits();
    }

    @Override
    public UUID getUuid() {
        return Util.NIL_UUID;
    }

    @Override
    public void setDead() { }

    @Override
    public boolean isDead() {
        return true;
    }

    @Override
    public boolean isDying() {
        return false;
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        return false;
    }

    @Override
    public void tickDying(Caster<?> caster) { }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public void setHidden(boolean hidden) { }

    @Override
    public void destroy(Caster<?> caster) { }

    @Override
    public String toString() {
        return "EmptySpell{}";
    }

    @Override
    public DataTracker getDataTracker() {
        return null;
    }
}
