package com.minelittlepony.unicopia.entity.damage;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;

public class MagicalDamageSource extends DamageSource {
    @Nullable
    private final Caster<?> spell;

    public MagicalDamageSource(Holder<DamageType> type, @Nullable Entity source, @Nullable Entity attacker, @Nullable Caster<?> caster) {
        super(type, source, attacker);
        spell = caster;
    }

    public MagicalDamageSource(Holder<DamageType> type, Vec3 position, @Nullable Caster<?> caster) {
        super(type, position);
        spell = caster;
    }

    public MagicalDamageSource(Holder<DamageType> type, @Nullable Entity attacker, @Nullable Caster<?> caster) {
        super(type, attacker);
        spell = caster;
    }

    public MagicalDamageSource(Holder<DamageType> type, @Nullable Caster<?> caster) {
        super(type);
        spell = caster;
    }

    @Nullable
    public Caster<?> getSpell() {
        return spell;
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity target) {

        String basic = "death.attack." + getMsgId();

        List<Component> params = new ArrayList<>();
        params.add(target.getDisplayName());

        @Nullable
        Entity attacker = getDirectEntity() != null ? getDirectEntity() : target.getKillCredit();
        ItemStack item = attacker instanceof LivingEntity ? ((LivingEntity)attacker).getMainHandItem() : ItemStack.EMPTY;

        if (attacker == target) {
            basic += ".self";

            if (!item.isEmpty() && item.has(DataComponents.CUSTOM_NAME)) {
                basic += ".item";
                params.add(item.getDisplayName());
            }
        } else if (!item.isEmpty() && item.has(DataComponents.CUSTOM_NAME)) {
            basic += ".item";
            params.add(attacker.getDisplayName());
            params.add(item.getDisplayName());
        } else if (attacker != null) {
            basic += ".player";
            params.add(attacker.getDisplayName());
        }

        Component message = Component.translatable(basic, params.toArray());
        return Pony.of(target).filter(e -> e.getCompositeRace().canFly()).map(pony -> {
            if (pony.getPhysics().isFlying()) {
                return Component.translatable("death.attack.unicopia.generic.whilst_flying", message);
            }
            return message;
        }).orElse(message);
    }
}
