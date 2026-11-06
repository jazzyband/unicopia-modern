package com.minelittlepony.unicopia.client.render;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;

class EntityReplacementManager implements Disguise {
    @Nullable
    private EntityType<?> currentType;
    private final EntityAppearance disguise = new EntityAppearance();
    private final List<EntityType<?>> defaultTypes = List.of(
            EntityTypes.ZOMBIE,
            EntityTypes.CREEPER,
            EntityTypes.HUSK,
            EntityTypes.SKELETON,
            EntityTypes.BLAZE,
            EntityTypes.DROWNED
    );
    private final Map<EntityType<?>, List<EntityType<?>>> pools = Map.of(
            EntityTypes.VILLAGER, List.of(EntityTypes.PILLAGER),
            EntityTypes.PLAYER, List.of(EntityTypes.ZOMBIE),
            EntityTypes.PIGLIN, List.of(EntityTypes.ZOMBIFIED_PIGLIN, EntityTypes.PIGLIN_BRUTE),
            EntityTypes.SQUID, List.of(EntityTypes.DROWNED),
            EntityTypes.DOLPHIN, List.of(EntityTypes.DROWNED),
            EntityTypes.ENDER_DRAGON, List.of(EntityTypes.CHICKEN)
    );

    public Optional<Disguise> getAppearanceFor(Caster<?> caster) {

        if (isRageApplicable(caster.asEntity()) && EquinePredicates.RAGING.test(Minecraft.getInstance().player)) {
            List<EntityType<?>> mobTypes = getMobTypePool(caster.asEntity().getType());
            EntityType<?> type = mobTypes.get(Math.abs((int)caster.asEntity().getUUID().getMostSignificantBits()) % mobTypes.size());
            if (type != currentType) {
                currentType = type;
                disguise.setAppearance(type.create(caster.asWorld(), EntitySpawnReason.LOAD));
            }
            return Optional.of(this);
        }

        return caster.getSpellSlot().get(SpellPredicate.IS_DISGUISE).map(Disguise.class::cast);
    }

    private List<EntityType<?>> getMobTypePool(EntityType<?> type) {
        return pools.getOrDefault(type, defaultTypes);
    }

    private boolean isRageApplicable(Entity entity) {
        return entity != Minecraft.getInstance().player && (entity instanceof AgeableMob || entity instanceof Player || pools.containsKey(entity.getType()));
    }

    @Override
    public EntityAppearance getAppearance() {
        return disguise;
    }

    @Override
    public boolean isDead() {
        return false;
    }
}
