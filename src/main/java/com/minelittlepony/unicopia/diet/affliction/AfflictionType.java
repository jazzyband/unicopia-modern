package com.minelittlepony.unicopia.diet.affliction;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.RegistryUtils;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record AfflictionType<T extends Affliction>(Identifier id, MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> packetCodec) {
    public static final String DEFAULT_ID = "unicopia:apply_status_effect";
    public static final Registry<AfflictionType<?>> REGISTRY = RegistryUtils.createSynced(Unicopia.id("affliction_type"), DEFAULT_ID);
    public static final Codec<Affliction> CODEC = CodecUtils.apply(REGISTRY.byNameCodec()
            .dispatch("type", Affliction::getType, AfflictionType::codec), elementCodec -> Codec
            .withAlternative(elementCodec, Codec.list(elementCodec), afflictions -> {
                afflictions = afflictions.stream().filter(f -> !f.isEmpty()).toList();
                return switch (afflictions.size()) {
                    case 0 -> EmptyAffliction.INSTANCE;
                    case 1 -> afflictions.get(0);
                    default -> new CompoundAffliction(afflictions);
                };
            }));
    public static final StreamCodec<RegistryFriendlyByteBuf, Affliction> PACKET_CODEC = ByteBufCodecs.registry(REGISTRY.key())
            .dispatch(Affliction::getType, AfflictionType::packetCodec);

    public static final AfflictionType<EmptyAffliction> EMPTY = register("empty", EmptyAffliction.CODEC, EmptyAffliction.PACKET_CODEC);
    public static final AfflictionType<CompoundAffliction> MANY = register("many", CompoundAffliction.CODEC, CompoundAffliction.PACKET_CODEC);
    public static final AfflictionType<StatusEffectAffliction> APPLY_STATUS_EFFECT = register("apply_status_effect", StatusEffectAffliction.CODEC, StatusEffectAffliction.PACKET_CODEC);
    public static final AfflictionType<LoseHungerAffliction> LOSE_HUNGER = register("lose_hunger", LoseHungerAffliction.CODEC, LoseHungerAffliction.PACKET_CODEC);
    public static final AfflictionType<HealingAffliction> HEALING = register("healing", HealingAffliction.CODEC, HealingAffliction.PACKET_CODEC);
    public static final AfflictionType<ClearLoveSicknessAffliction> CURE_LOVE_SICKNESS = register("cure_love_sickness", ClearLoveSicknessAffliction.CODEC, ClearLoveSicknessAffliction.PACKET_CODEC);

    static <T extends Affliction> AfflictionType<T> register(String name, MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> packetCodec) {
        return Registry.register(REGISTRY, Unicopia.id(name), new AfflictionType<>(Unicopia.id(name), codec, packetCodec));
    }

    public String getTranslationKey() {
        return Util.makeDescriptionId("affliction", id());
    }

    public static void bootstrap() { }
}
