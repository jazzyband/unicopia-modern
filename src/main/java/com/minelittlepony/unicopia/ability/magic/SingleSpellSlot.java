package com.minelittlepony.unicopia.ability.magic;

import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.network.track.Trackable;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;

/**
 * Container for a single spell
 *
 * @param <T> The owning entity
 */
class SingleSpellSlot implements SpellSlots, NbtSerialisable {
    private final Caster<?> owner;
    private final MultiSpellSlot.Entry<Spell> entry;

    public SingleSpellSlot(Caster<?> owner) {
        this.owner = owner;
        this.entry = new MultiSpellSlot.Entry<>(owner);
        Trackable.of(owner.asEntity()).getDataTrackers().getPrimaryTracker().startTracking(entry);
    }

    @Override
    public boolean contains(UUID id) {
        return entry.spell.equalsOrContains(id);
    }

    @Override
    public void put(@Nullable Spell effect) {
        entry.spell.set(effect, owner);
    }

    @Override
    public void remove(UUID id, boolean force) {
        if (contains(id)) {
            entry.discard(force);
        }
    }

    @Override
    public <T extends Spell> Stream<T> stream(@Nullable SpellPredicate<T> type) {
        return entry.spell.findMatches(type);
    }

    @Override
    public boolean clear(boolean force) {
        if (entry.spell.get() != null) {
            entry.discard(force);
            return true;
        }
        return false;
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        compound.put("effect", entry.spell.toNBT(lookup));
    }

    @Override
    public void toValue(net.minecraft.world.level.storage.ValueOutput output, Provider lookup) {
        output.store("effect", CompoundTag.CODEC, entry.spell.toNBT(lookup));
    }

    @Override
    public void fromValue(net.minecraft.world.level.storage.ValueInput input, Provider lookup) {
        entry.spell.fromNBT(input.read("effect", CompoundTag.CODEC).orElseGet(CompoundTag::new), lookup);
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        entry.spell.fromNBT(compound.getCompoundOrEmpty("effect"), lookup);
    }

    @Override
    public void copyFrom(SpellSlots other, boolean alive) {
        other.get().ifPresent(this::put);
    }
}
