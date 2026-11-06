package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.projectile.ProjectileImpactListener;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

/**
 * Base implementation for a spell that changes the player's appearance.
 */
public abstract class AbstractDisguiseSpell extends AbstractSpell implements Disguise, ProjectileImpactListener {

    private final EntityAppearance disguise = dataTracker.startTracking(new EntityAppearance());

    public AbstractDisguiseSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    protected void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        caster.asEntity().refreshDimensions();
        caster.asEntity().setInvisible(false);
        if (caster instanceof Pony pony) {
            pony.setInvisible(false);
        }
        disguise.remove();
    }

    @Override
    public EntityAppearance getAppearance() {
        return disguise;
    }

    @Override
    public boolean onProjectileImpact(Projectile projectile) {
        return disguise.getEntity() == projectile;
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        return situation == Situation.BODY && update(source, true);
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        disguise.toNBT(compound, lookup);
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        disguise.fromNBT(compound, lookup);
    }

    public static Entity getAppearance(Entity e) {
        return e instanceof Player ? Pony.of((Player)e)
                .getSpellSlot()
                .get(SpellPredicate.IS_DISGUISE)
                .map(AbstractDisguiseSpell::getAppearance)
                .map(EntityAppearance::getEntity)
                .orElse(e) : e;
    }
}
