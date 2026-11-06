package com.minelittlepony.unicopia.diet.affliction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record StatusEffectAffliction(Holder<MobEffect> effect, Range seconds, Range amplifier, int chance) implements Affliction {
    public static final MapCodec<StatusEffectAffliction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(StatusEffectAffliction::effect),
            Range.CODEC.fieldOf("seconds").forGetter(StatusEffectAffliction::seconds),
            Range.CODEC.optionalFieldOf("amplifier", Range.of(0, -1)).forGetter(StatusEffectAffliction::amplifier),
            Codec.INT.optionalFieldOf("chance", 0).forGetter(StatusEffectAffliction::chance)
    ).apply(instance, StatusEffectAffliction::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, StatusEffectAffliction> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT), StatusEffectAffliction::effect,
            Range.PACKET_CODEC, StatusEffectAffliction::seconds,
            Range.PACKET_CODEC, StatusEffectAffliction::amplifier,
            ByteBufCodecs.INT, StatusEffectAffliction::chance,
            StatusEffectAffliction::new
    );

    @Override
    public AfflictionType<?> getType() {
        return AfflictionType.APPLY_STATUS_EFFECT;
    }

    @Override
    public void afflict(Player player, ItemStack stack) {
        if (player.level().isClientSide() || (chance > 0 && player.level().getRandom().nextInt(chance) > 0)) {
            return;
        }

        float health = player.getHealth();
        float oldMaxHealth = player.getMaxHealth();
        MobEffectInstance current = player.getEffect(effect);
        player.addEffect(new MobEffectInstance(effect,
                seconds.getClamped(current == null ? 0 : current.getDuration(), 20),
                amplifier.getClamped(current == null ? 0 : current.getAmplifier(), 1)
        ));
        // keep original health
        if (player.getMaxHealth() != oldMaxHealth) {
            player.setHealth(Mth.clamp(health, 0, player.getMaxHealth()));
        }
    }

    @Override
    public Component getName() {
        MutableComponent text = effect.value().getDisplayName().copy();

        if (amplifier.min() > 0) {
            text = Component.translatable("potion.withAmplifier", text, Component.translatable("potion.potency." + (amplifier.min())));
        }

        float tickRate = Minecraft.getInstance().level == null ? 20 : Minecraft.getInstance().level.tickRateManager().tickrate();
        text = Component.translatable("potion.withDuration", text, StringUtil.formatTickDuration(seconds.min() * 20, tickRate));

        if (chance > 0) {
            text = Component.translatable("potion.withChance", chance, text);
        }
        return text;
    }
}