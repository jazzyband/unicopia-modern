package com.minelittlepony.unicopia.item.consume;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.util.VecHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ApplyToSurroundingEntitiesConsumeEffect(double range, List<ConsumeEffect> effects) implements DeathConsumeEffect {
    public static final MapCodec<ApplyToSurroundingEntitiesConsumeEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.DOUBLE.fieldOf("range").forGetter(ApplyToSurroundingEntitiesConsumeEffect::range),
        ConsumeEffect.CODEC.listOf().fieldOf("effect").forGetter(ApplyToSurroundingEntitiesConsumeEffect::effects)
    ).apply(i, ApplyToSurroundingEntitiesConsumeEffect::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ApplyToSurroundingEntitiesConsumeEffect> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE,
        ApplyToSurroundingEntitiesConsumeEffect::range,
        ConsumeEffect.STREAM_CODEC.apply(ByteBufCodecs.list()),
        ApplyToSurroundingEntitiesConsumeEffect::effects,
        ApplyToSurroundingEntitiesConsumeEffect::new
    );

    @Override
    public Type<? extends ConsumeEffect> getType() {
        return UDataComponentTypes.APPLY_TO_SURROUNDING_ENTITIES;
    }

    @Override
    public boolean onConsume(Level world, ItemStack stack, LivingEntity user, DamageSource damage) {
        var targets = VecHelper.findInRange(user, world, user.position(), range, e -> e instanceof LivingEntity & !SpellType.SHIELD.isOn(e));
        for (var target : targets) {
            for (var effect : effects) {
                if (effect instanceof ApplyToSurroundingEntitiesConsumeEffect) {
                    continue;
                }
                if (effect instanceof DeathConsumeEffect o) {
                    o.onConsume(world, stack, (LivingEntity)target, damage);
                } else {
                    effect.apply(world, stack, (LivingEntity)target);
                }
            }
        }

        return !targets.isEmpty();
    }
}
