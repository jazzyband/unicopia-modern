package com.minelittlepony.unicopia.item.component;

import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.minelittlepony.unicopia.diet.DietProfile;
import com.minelittlepony.unicopia.item.consume.ApplyToSurroundingEntitiesConsumeEffect;
import com.minelittlepony.unicopia.item.consume.UseTotemOfDying;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public interface UDataComponentTypes {
    DataComponentType<SpellType<?>> STORED_SPELL = register("stored_spell", builder -> builder.persistent(SpellType.CODEC).networkSynchronized(SpellType.PACKET_CODEC));
    DataComponentType<SpellTraits> SPELL_TRAITS = register("spell_traits", builder -> builder.persistent(SpellTraits.CODEC).networkSynchronized(SpellTraits.PACKET_CODEC).cacheEncoding());
    DataComponentType<SpellbookState> SPELLBOOK_STATE = register("spellbook_state", builder -> builder.persistent(SpellbookState.CODEC).networkSynchronized(SpellbookState.PACKET_CODEC).cacheEncoding());
    DataComponentType<Boolean> GLOWING = register("glowing", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    DataComponentType<BufferflyVariantComponent> BUTTERFLY_VARIANT = register("butterfly_variant", builder -> builder.persistent(BufferflyVariantComponent.CODEC).networkSynchronized(BufferflyVariantComponent.PACKET_CODEC));
    DataComponentType<BalloonDesignComponent> BALLOON_DESIGN = register("balloon_design", builder -> builder.persistent(BalloonDesignComponent.CODEC).networkSynchronized(BalloonDesignComponent.PACKET_CODEC));
    DataComponentType<Issuer> ISSUER = register("issuer", builder -> builder.persistent(Issuer.CODEC).networkSynchronized(Issuer.PACKET_CODEC).cacheEncoding());
    DataComponentType<Charges> CHARGES = register("charges", builder -> builder.persistent(Charges.CODEC).networkSynchronized(Charges.PACKET_CODEC));
    DataComponentType<Appearance> APPEARANCE = register("appearance", builder -> builder.persistent(Appearance.CODEC).networkSynchronized(Appearance.PACKET_CODEC));
    DataComponentType<DietProfile> DIET_PROFILE = register("diet_profile", builder -> builder.persistent(DietProfile.CODEC).networkSynchronized(DietProfile.PACKET_CODEC));
    DataComponentType<BreaksIntoItemComponent> ITEM_AFTER_BREAKING = register("item_after_breaking", builder -> builder.persistent(BreaksIntoItemComponent.CODEC).networkSynchronized(BreaksIntoItemComponent.PACKET_CODEC));
    DataComponentType<ConversionComponent> ITEM_AFTER_DRAGON_BREATH = register("item_after_dragon_breath", builder -> builder.persistent(ConversionComponent.CODEC).networkSynchronized(ConversionComponent.PACKET_CODEC));
    DataComponentType<MimicComponent> MIMIC = register("mimic", builder -> builder.persistent(MimicComponent.CODEC).networkSynchronized(MimicComponent.PACKET_CODEC));
    DataComponentType<DeathProtection> DEATH_CAUSING = register("death_causing", builder -> builder.persistent(DeathProtection.CODEC).networkSynchronized(DeathProtection.STREAM_CODEC));

    ConsumeEffect.Type<ApplyToSurroundingEntitiesConsumeEffect> APPLY_TO_SURROUNDING_ENTITIES = register("apply_to_surrounding_entities", ApplyToSurroundingEntitiesConsumeEffect.CODEC, ApplyToSurroundingEntitiesConsumeEffect.STREAM_CODEC);
    ConsumeEffect.Type<UseTotemOfDying> USE_TOTEM_OF_DYING = register("use_totem_of_dying", UseTotemOfDying.CODEC, UseTotemOfDying.STREAM_CODEC);

    DeathProtection TOTEM_OF_DYING = new DeathProtection(List.of(
            new UseTotemOfDying(true),
            new ApplyToSurroundingEntitiesConsumeEffect(10, List.of(new UseTotemOfDying(false)))
    ));

    private static <T extends ConsumeEffect> ConsumeEffect.Type<T> register(String id, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> packetCodec) {
        return Registry.register(BuiltInRegistries.CONSUME_EFFECT_TYPE, id, new ConsumeEffect.Type<>(codec, packetCodec));
    }

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Unicopia.id(name), builderOperator.apply(DataComponentType.builder()).build());
    }

    static void bootstrap() { }
}
