package com.minelittlepony.unicopia.item.component;

import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;

public record Issuer(String name, UUID id) implements TooltipProvider {
    public static final Codec<Issuer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(Issuer::name),
            UUIDUtil.AUTHLIB_CODEC.fieldOf("id").forGetter(Issuer::id)
    ).apply(instance, Issuer::new));
    public static final StreamCodec<ByteBuf, Issuer> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Issuer::name,
            UUIDUtil.STREAM_CODEC, Issuer::id,
            Issuer::new
    );

    public static ItemStack set(ItemStack stack, Entity signer) {
        stack.set(UDataComponentTypes.ISSUER, new Issuer(signer.getDisplayName().getString(), signer.getUUID()));
        return stack;
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
        textConsumer.accept(Component.translatable("item.unicopia.friendship_bracelet.issuer", name()));
    }

    public boolean isSignedBy(Player player) {
        return isSignedBy(player.getUUID());
    }

    public boolean isSignedBy(UUID player) {
        return player.equals(id());
    }

    @Nullable
    public static String getSignatorName(ItemStack stack) {
        Issuer issuer = stack.get(UDataComponentTypes.ISSUER);
        return issuer == null ? null : issuer.name();
    }

    @Nullable
    public static UUID getSignatorId(ItemStack stack) {
        Issuer issuer = stack.get(UDataComponentTypes.ISSUER);
        return issuer == null ? null : issuer.id();
    }

    public static boolean isSigned(ItemStack stack) {
        return stack.has(UDataComponentTypes.ISSUER);
    }

    public static boolean isSignedBy(ItemStack stack, Player player) {
        Issuer issuer = stack.get(UDataComponentTypes.ISSUER);
        return issuer != null && issuer.isSignedBy(player);
    }

    public static boolean isSignedBy(ItemStack stack, UUID player) {
        Issuer issuer = stack.get(UDataComponentTypes.ISSUER);
        return issuer != null && issuer.isSignedBy(player);
    }
}
