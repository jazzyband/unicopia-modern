package com.minelittlepony.unicopia.diet.affliction;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CompoundAffliction (List<Affliction> afflictions) implements Affliction {
    public static final MapCodec<CompoundAffliction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            AfflictionType.CODEC.listOf().fieldOf("afflictions").forGetter(CompoundAffliction::afflictions)
    ).apply(instance, CompoundAffliction::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CompoundAffliction> PACKET_CODEC = StreamCodec.composite(
            AfflictionType.PACKET_CODEC.apply(ByteBufCodecs.list()), CompoundAffliction::afflictions,
            CompoundAffliction::new
    );

    public static CompoundAffliction of(Affliction...afflictions) {
        return new CompoundAffliction(List.of(afflictions));
    }

    @Override
    public AfflictionType<?> getType() {
        return AfflictionType.MANY;
    }

    @Override
    public boolean isEmpty() {
        return afflictions.isEmpty();
    }

    @Override
    public void appendTooltip(Consumer<Component> tooltip) {
        afflictions.forEach(i -> i.appendTooltip(tooltip));
    }

    @Override
    public Component getName() {
        return afflictions.stream().map(Affliction::getName).reduce(null, (a, b) -> {
            return a == null ? b : a.copy().append(" + ").append(b);
        });
    }

    @Override
    public void afflict(Player player, ItemStack stack) {
        afflictions.forEach(i -> i.afflict(player, stack));
    }
}
