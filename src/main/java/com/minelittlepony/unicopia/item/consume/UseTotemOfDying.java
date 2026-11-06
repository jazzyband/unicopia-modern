package com.minelittlepony.unicopia.item.consume;

import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgEntityStatus;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;

public record UseTotemOfDying(boolean perpetrator) implements DeathConsumeEffect {
    public static final MapCodec<UseTotemOfDying> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.BOOL.fieldOf("perpetrator").forGetter(UseTotemOfDying::perpetrator)
    ).apply(i, UseTotemOfDying::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, UseTotemOfDying> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        UseTotemOfDying::perpetrator,
        UseTotemOfDying::new
    );

    @Override
    public Type<? extends ConsumeEffect> getType() {
        return UDataComponentTypes.USE_TOTEM_OF_DYING;
    }

    @Override
    public boolean onConsume(Level world, ItemStack stack, LivingEntity user, DamageSource damage) {
        Channel.ENTITY_STATUS.sendToSurroundingPlayers(new MsgEntityStatus(user.getId(), MsgEntityStatus.USE_TOTEM_OF_DYING), user);
        if (!perpetrator && world instanceof ServerLevel sw) {
            user.hurtServer(sw, damage, Integer.MAX_VALUE);
        }
        return true;
    }
}
