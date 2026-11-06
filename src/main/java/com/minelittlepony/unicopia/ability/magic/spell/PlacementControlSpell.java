package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.AbstractSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;

public class PlacementControlSpell extends AbstractSpell implements OrientedSpell {
    private static final Codec<ResourceKey<Level>> DIMENSION_CODEC = ResourceKey.codec(Registries.DIMENSION);
    private static final Codec<Optional<ResourceKey<Level>>> OPTIONAL_DIMENSION_CODEC = ExtraCodecs.optionalEmptyMap(DIMENSION_CODEC);

    private final DataTracker.Entry<Optional<UUID>> placedEntityId = dataTracker.startTracking(TrackableDataType.UUID, Optional.empty());
    private final DataTracker.Entry<Optional<ResourceKey<Level>>> dimension = dataTracker.startTracking(TrackableDataType.ofRegistryKey(), Optional.empty());
    private final DataTracker.Entry<Optional<Vec3>> position = dataTracker.startTracking(TrackableDataType.OPTIONAL_VECTOR, Optional.empty());
    private final DataTracker.Entry<Optional<Vec3>> orientation = dataTracker.startTracking(TrackableDataType.OPTIONAL_VECTOR, Optional.empty());

    @Nullable
    private Spell delegate;

    public PlacementControlSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    PlacementControlSpell(Spell delegate) {
        this(SpellType.PLACE_CONTROL_SPELL.withTraits(delegate.getTypeAndTraits().traits()));
        this.delegate = delegate;
    }

    @Nullable
    public Spell getDelegate() {
        return delegate;
    }

    public Optional<Vec3> getPosition() {
        return position.get();
    }

    public void setDimension(ResourceKey<Level> dimension) {
        this.dimension.set(Optional.of(dimension));
    }

    public void setPosition(Vec3 position) {
        this.position.set(Optional.of(position));
    }

    @Override
    public void setOrientation(Caster<?> caster, float pitch, float yaw) {
        this.orientation.set(Optional.of(new Vec3(pitch, yaw, 0)));
        if (delegate instanceof OrientedSpell o) {
            o.setOrientation(caster, pitch, yaw);
        }
        if (!caster.isClient()) {
            var entry = getConnection(caster);
            if (entry != null) {
                entry.setPitch(pitch);
                entry.setYaw(yaw);
            }
        }
    }

    @Override
    public boolean apply(Caster<?> caster) {
        return delegate != null && super.apply(caster);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (!source.isClient()) {

            if (placedEntityId.get().isEmpty()) {
                if (dimension.get().isEmpty()) {
                    setDimension(source.asWorld().dimension());
                }
                if (getPosition().isEmpty()) {
                    setPosition(source.asEntity().position());
                }

                CastSpellEntity entity = new CastSpellEntity(source.asWorld(), source, this);

                Vec3 pos = getPosition().get();
                Vec3 rot = orientation.get().orElse(Vec3.ZERO);

                entity.absSnapTo(pos.x, pos.y, pos.z, (float)rot.y, (float)rot.x);
                entity.level().addFreshEntity(entity);

                placedEntityId.set(Optional.of(entity.getUUID()));
            } else {
                if (getConnection(source) == null) {
                    setDead();
                }
            }
        }

        return !isDead();
    }

    @Nullable
    private Ether.MutableEntry<?> getConnection(Caster<?> source) {
        return delegate == null || placedEntityId.get().isEmpty() ? null : getWorld(source)
                .map(world -> Ether.get(world).get(getDelegate().getTypeAndTraits().type(), placedEntityId.get().get(), delegate.getUuid()))
                .orElse(null);
    }

    private Optional<Level> getWorld(Caster<?> source) {
        return dimension.get().map(source.asWorld().getServer()::getLevel);
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        compound.put("spell", Spell.writeNbt(delegate, lookup));
        compound.store("position", CodecUtils.OPTIONAL_VECTOR, position.get());
        compound.store("orientation", CodecUtils.OPTIONAL_VECTOR, orientation.get());
        compound.store("dimension", OPTIONAL_DIMENSION_CODEC, dimension.get());
        compound.store("placedEntityId", CodecUtils.OPTIONAL_UUID, placedEntityId.get());
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        delegate = Spell.readNbt(compound.getCompoundOrEmpty("spell"), lookup);
        placedEntityId.set(compound.read("placedEntityId", CodecUtils.OPTIONAL_UUID).orElse(Optional.empty()));
        position.set(compound.read("position", CodecUtils.OPTIONAL_VECTOR).orElse(Optional.empty()));
        orientation.set(compound.read("orientation", CodecUtils.OPTIONAL_VECTOR).orElse(Optional.empty()));
        dimension.set(compound.read("dimension", OPTIONAL_DIMENSION_CODEC).orElse(Optional.empty()));
    }

    public interface PlacementDelegate {
        void onPlaced(Caster<?> source, PlacementControlSpell parent);
    }
}
