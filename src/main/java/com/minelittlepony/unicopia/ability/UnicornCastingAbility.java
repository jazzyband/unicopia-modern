package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.HomingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.component.Charges;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.TraceHelper;
import com.minelittlepony.unicopia.util.TypedActionResult;
import com.minelittlepony.unicopia.util.VecHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Casts magic onto the user directly, or uses the item in the main hand slot.
 * <p>
 * 1. If the player is holding nothing, defaults to toggling their equipped spell (currently only shield).
 * 2. If the player is holding a gem, consumes it and casts whatever spell is contained within onto the user.
 * 3. If the player is holding a amulet, charges it.
 */
public class UnicornCastingAbility extends AbstractSpellCastingAbility {

    @Override
    public int getWarmupTime(Pony player) {
        return (int)(20 - Math.min(17F, player.getLevel().get() * 0.75F));
    }

    @Override
    public Optional<Hit> prepare(Pony player) {
        TypedActionResult<CustomisedSpellType<?>> spell = player.getCharms().getSpellInHand(false);
        return Hit.of(player.canCast()
                && player.getMagicalReserves().getMana().get() >= getCostEstimate(player)
                && (!spell.result().consumesAction() || canCast(spell.value().type()))
        );
    }

    @Override
    public double getCostEstimate(Pony player) {
        TypedActionResult<ItemStack> amulet = getAmulet(player);

        if (amulet.result().consumesAction()) {
            float manaLevel = player.getMagicalReserves().getMana().get();

            return Math.min(manaLevel, Charges.of(amulet.value()).energy());
        }

        TypedActionResult<CustomisedSpellType<?>> spell = player.getCharms().getSpellInHand(false);

        return !spell.result().consumesAction() || spell.value().isOn(player) ? 2 : 4;
    }

    @Override
    public int getColor(Pony player) {
        TypedActionResult<ItemStack> amulet = getAmulet(player);
        if (amulet.result().consumesAction()) {
            return 0x000000;
        }

        return super.getColor(player);
    }

    @Override
    public boolean apply(Pony player, Hit data) {
        if (!player.canCast()) {
            return false;
        }

        TypedActionResult<ItemStack> amulet = getAmulet(player);

        if (amulet.result().consumesAction()) {
            ItemStack stack = amulet.value();
            if (Charges.of(stack).canHoldCharge()) {
                int amount = -(int)Math.min(player.getMagicalReserves().getMana().get(), Charges.of(stack).energy());

                if (amount < 0) {
                    Charges.discharge(stack, amount);
                    player.getMagicalReserves().getMana().add(amount);
                    player.asWorld().playSound(null, player.asEntity(), USounds.ITEM_AMULET_RECHARGE, SoundSource.PLAYERS, 1, 1);
                }
            }
        } else {
            TypedActionResult<CustomisedSpellType<?>> newSpell = player.getCharms().getSpellInHand(true);

            if (newSpell.result() != InteractionResult.FAIL && canCast(newSpell.value().type())) {
                CustomisedSpellType<?> spell = newSpell.value();
                if (newSpell.result() == InteractionResult.CONSUME) {
                    CustomisedSpellType<?> equippedType = player.getCharms().getEquippedSpell(player.getCharms().getHand());
                    if (equippedType.type() == spell.type()) {
                        player.getCharms().equipSpell(player.getCharms().getHand(), spell);
                    }
                }

                if (spell.isEmpty()) {
                    return false;
                }

                boolean has = !spell.isStackable() && player.getSpellSlot().contains(spell);
                boolean removed = !spell.isStackable() && player.getSpellSlot().removeWhere(spell.type());
                player.subtractEnergyCost(removed ? 2 : 4);
                if (!has) {
                    Spell s = spell.apply(player, CastingMethod.DIRECT);
                    if (s == null) {
                        player.spawnParticles(ParticleTypes.LARGE_SMOKE, 6);
                        player.playSound(USounds.SPELL_CAST_FAIL, 1, 0.5F);
                    } else {
                        player.setAnimation(Animation.ARMS_UP, Animation.Recipient.HUMAN);
                        if (s instanceof HomingSpell homer) {
                            TraceHelper.findEntity(player.asEntity(), homer.getRange(player), 1, EquinePredicates.EXCEPT_MAGIC_IMMUNE).ifPresent(homer::setTarget);
                        }
                        player.playSound(USounds.SPELL_CAST_SUCCESS, 0.05F, 2.2F);
                    }
                } else {
                    player.setAnimation(Animation.WOLOLO, Animation.Recipient.ANYONE);
                }
            }
        }

        return true;
    }

    protected boolean canCast(SpellType<?> type) {
        return true;
    }

    private TypedActionResult<ItemStack> getAmulet(Pony player) {

        ItemStack stack = player.asEntity().getItemInHand(InteractionHand.MAIN_HAND);

        if (stack.getItem() instanceof AmuletItem) {
            if (Charges.of(stack).canHoldCharge()) {
                return TypedActionResult.consume(stack);
            }

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExhaustion().multiply(3.3F);

        if (getAmulet(player).result() == InteractionResult.CONSUME) {
            Vec3 eyes = player.asEntity().getEyePosition(1);

            float i = player.getAbilities().getStat(slot).getFillProgress();

            RandomSource rng = player.asWorld().getRandom();
            player.addParticle(i > 0.5F ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CLOUD, eyes, VecHelper.supply(() -> (rng.nextGaussian() - 0.5) * 0.1));
            player.playSound(USounds.ITEM_AMULET_CHARGING, 1, i / 20);
        } else {
            player.spawnParticles(MagicParticleEffect.UNICORN, 5);
        }
    }
}
