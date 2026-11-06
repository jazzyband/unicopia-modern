package com.minelittlepony.unicopia.item.component;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;

public record BalloonDesignComponent(AirBalloonEntity.BalloonDesign design, boolean showInTooltip) implements TooltipProvider {
    public static final BalloonDesignComponent DEFAULT = new BalloonDesignComponent(AirBalloonEntity.BalloonDesign.NONE, false);
    public static final Codec<BalloonDesignComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AirBalloonEntity.BalloonDesign.CODEC.fieldOf("design").forGetter(BalloonDesignComponent::design),
            Codec.BOOL.fieldOf("show_in_tooltip").forGetter(BalloonDesignComponent::showInTooltip)
    ).apply(instance, BalloonDesignComponent::new));
    public static final StreamCodec<ByteBuf, BalloonDesignComponent> PACKET_CODEC = StreamCodec.composite(
            AirBalloonEntity.BalloonDesign.PACKET_CODEC, BalloonDesignComponent::design,
            ByteBufCodecs.BOOL, BalloonDesignComponent::showInTooltip,
            BalloonDesignComponent::new
    );

    public static BalloonDesignComponent get(ItemStack stack) {
        return stack.getOrDefault(UDataComponentTypes.BALLOON_DESIGN, DEFAULT);
    }

    public static ItemStack set(ItemStack stack, BalloonDesignComponent variant) {
        stack.set(UDataComponentTypes.BALLOON_DESIGN, variant);
        return stack;
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
        if (showInTooltip() && design != AirBalloonEntity.BalloonDesign.NONE) {
            textConsumer.accept(Component.literal(design().name()).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
