package com.minelittlepony.unicopia.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record Appearance(ItemStack item, boolean replaceFully) {
    public static final Appearance DEFAULT = new Appearance(ItemStack.EMPTY, false);
    public static final Appearance DEFAULT_FULLY_DISGUISED = new Appearance(ItemStack.EMPTY, true);
    public static final Codec<Appearance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.withAlternative(ItemStack.CODEC, BuiltInRegistries.ITEM.byNameCodec(), Item::getDefaultInstance).fieldOf("item").forGetter(Appearance::item),
            Codec.BOOL.fieldOf("replace_fully").forGetter(Appearance::replaceFully)
    ).apply(instance, Appearance::of));
    public static final StreamCodec<RegistryFriendlyByteBuf, Appearance> PACKET_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, Appearance::item,
            ByteBufCodecs.BOOL, Appearance::replaceFully,
            Appearance::of
    );

    public Appearance {
        if (UDataComponentTypes.APPEARANCE != null) {
            item.remove(UDataComponentTypes.APPEARANCE);
        }
    }

    public ItemStack unwrap(ItemStack stack) {
        return item().isEmpty() ? stack : item();
    }

    public static Appearance of(ItemStack stack, boolean complete) {
        return stack.isEmpty() ? (complete ? DEFAULT_FULLY_DISGUISED : DEFAULT) : new Appearance(stack, complete);
    }

    public static Appearance get(ItemStack stack) {
        return stack.getOrDefault(UDataComponentTypes.APPEARANCE, DEFAULT);
    }

    public static boolean hasAppearance(ItemStack stack) {
        return !get(stack).item().isEmpty();
    }

    public static ItemStack upwrapAppearance(ItemStack stack) {
        return get(stack).unwrap(stack);
    }

    public static ItemStack set(ItemStack stack, ItemStack appearance) {
        Appearance component = createAppearanceFor(stack, appearance);
        stack.set(UDataComponentTypes.APPEARANCE, component);
        if (stack.getItem() instanceof AppearanceChangeCallback callback) {
            callback.onAppearanceSet(stack, component);
        }
        return stack;
    }

    private static Appearance createAppearanceFor(ItemStack stack, ItemStack appearance) {
        if (appearance.isEmpty()) {
            return stack.getPrototype().getOrDefault(UDataComponentTypes.APPEARANCE, DEFAULT);
        }

        appearance = appearance.copy();
        appearance.setCount(stack.getCount());
        appearance.setDamageValue(stack.getDamageValue());
        return of(appearance, stack.getOrDefault(UDataComponentTypes.APPEARANCE, DEFAULT).replaceFully());
    }

    public interface AppearanceChangeCallback {
        void onAppearanceSet(ItemStack stack, Appearance appearance);
    }
}
