package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.client.TextHelper;
import com.minelittlepony.unicopia.entity.effect.EffectUtils;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.util.TypedActionResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CustomisedSpellType<T extends Spell> (
        SpellType<T> type,
        SpellTraits traits,
        Supplier<SpellTraits> traitsDifferenceSupplier
    ) implements SpellPredicate<T>, TooltipProvider {
    public static final Codec<CustomisedSpellType<?>> CODEC = RecordCodecBuilder.create(i -> i.group(
        SpellType.CODEC.fieldOf("effect_id").forGetter(CustomisedSpellType::type),
        SpellTraits.CODEC.fieldOf("traits").forGetter(CustomisedSpellType::traits)
    ).apply(i, (type, traits) -> type.withTraits(traits)));

    public static <T extends Spell> CustomisedSpellType<T> empty() {
        return SpellType.<T>empty().withTraits();
    }

    public boolean isEmpty() {
        return type.isEmpty();
    }

    public boolean isStackable() {
        return type().isStackable();
    }

    public SpellTraits relativeTraits() {
        return traitsDifferenceSupplier.get();
    }

    @Nullable
    public T create() {
        try {
            return type.getFactory().create(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public T create(CompoundTag compound, Provider lookup) {
        T spell = create();
        if (spell != null) {
            spell.fromNBT(compound, lookup);
        }
        return spell;
    }

    @Nullable
    public T apply(Caster<?> caster, CastingMethod method) {
        if (isEmpty()) {
            return null;
        }

        T spell = create();
        if (spell != null) {
            Spell s = spell.prepareForCast(caster, method);
            if (s != null && s.apply(caster)) {
                return spell;
            }
        }

        return null;
    }

    @Override
    public boolean test(@Nullable Spell spell) {
        return spell != null && spell.getTypeAndTraits().equals(this);
    }

    public ItemStack getDefaultStack() {
        return traits.applyTo(type.getDefualtStack());
    }

    public ItemStack applyTo(ItemStack stack) {
        if (stack.isEmpty() || type.isEmpty()) {
            stack.remove(UDataComponentTypes.STORED_SPELL);
            stack.remove(UDataComponentTypes.SPELL_TRAITS);
            return stack;
        }
        stack.set(UDataComponentTypes.STORED_SPELL, type);
        return traits().applyTo(stack);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
        if (isEmpty()) {
            return;
        }
        MutableComponent lore = Component.translatable(type().getTranslationKey() + ".lore").withStyle(type().getAffinity().getColor());

        if (!InteractionManager.getInstance().getClientSpecies().canCast()) {
            lore = lore.withStyle(ChatFormatting.OBFUSCATED);
        }
        TextHelper.wrap(lore, 180).forEach(textConsumer);
        float corruption = ((int)traits().getCorruption() * 10) + type().getAffinity().getCorruption();
        List<Component> modifiers = new ArrayList<>();
        type().getTooltip().appendTooltip(this, modifiers);
        if (corruption != 0) {
            modifiers.add(EffectUtils.formatModifierChange("affinity.unicopia.corruption", corruption, true));
        }
        if (!modifiers.isEmpty()) {
            textConsumer.accept(Component.empty());
            textConsumer.accept(Component.translatable("affinity.unicopia.when_cast").withStyle(ChatFormatting.GRAY));
            modifiers.forEach(textConsumer);
        }
    }

    public TypedActionResult<CustomisedSpellType<?>> toAction() {
        return isEmpty() ? TypedActionResult.fail(this) : TypedActionResult.pass(this);
    }

    public CompoundTag toNbt(CompoundTag compound) {
        type.toNbt(compound);
        compound.put("traits", traits.toNbt());
        return compound;
    }

    public static <T extends Spell> CustomisedSpellType<T> fromNBT(CompoundTag compound) {
        SpellType<T> type = compound.read("effect_id", SpellType.<T>codec()).orElseGet(SpellType::empty);
        return type.withTraits(compound.read("traits", SpellTraits.CODEC).orElse(type.getTraits()));
    }
}
