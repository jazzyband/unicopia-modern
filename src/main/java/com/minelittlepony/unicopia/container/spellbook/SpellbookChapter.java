package com.minelittlepony.unicopia.container.spellbook;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SpellbookChapter (
    TabSide side,
    int tabY,
    int color,
    List<Page> pages
) {
    public static final Codec<SpellbookChapter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TabSide.CODEC.fieldOf("side").forGetter(SpellbookChapter::side),
            Codec.INT.fieldOf("y_position").forGetter(SpellbookChapter::tabY),
            Codec.INT.optionalFieldOf("color", 0).forGetter(SpellbookChapter::color),
            Contents.CODEC.xmap(Contents::pages, Contents::new).fieldOf("content").forGetter(SpellbookChapter::pages)
    ).apply(instance, SpellbookChapter::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellbookChapter> PACKET_CODEC = StreamCodec.composite(
            TabSide.PACKET_CODEC, SpellbookChapter::side,
            ByteBufCodecs.INT, SpellbookChapter::tabY,
            ByteBufCodecs.INT, SpellbookChapter::color,
            Page.PACKET_CODEC.apply(ByteBufCodecs.list()), SpellbookChapter::pages,
            SpellbookChapter::new
    );

    record Contents(List<Page> pages) {
        public static final Codec<Contents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Page.CODEC.listOf().fieldOf("pages").forGetter(Contents::pages)
        ).apply(instance, SpellbookChapter.Contents::new));
    }

    public record Page (
            Component title,
            int level,
            int color,
            List<ChapterPageElement> elements
        ) {
        public static final Codec<Page> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ComponentSerialization.CODEC.optionalFieldOf("title", Component.empty()).forGetter(Page::title),
                Codec.INT.optionalFieldOf("level", 0).forGetter(Page::level),
                TextColor.CODEC.xmap(TextColor::getValue, TextColor::fromRgb).optionalFieldOf("color", 0).forGetter(Page::color),
                ChapterPageElement.CODEC.listOf().optionalFieldOf("elements", List.of()).forGetter(Page::elements)
        ).apply(instance, Page::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Page> PACKET_CODEC = StreamCodec.composite(
            ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC, Page::title,
            ByteBufCodecs.INT, Page::level,
            ByteBufCodecs.INT, Page::color,
            ChapterPageElement.PACKET_CODEC.apply(ByteBufCodecs.list()), Page::elements,
            Page::new
        );

        public static final class Builder {
            private Component title = Component.empty();
            private int level = 0;
            private int color = 0;
            private final List<ChapterPageElement> elements = new ArrayList<>();

            private Builder() {}

            public com.minelittlepony.unicopia.container.spellbook.SpellbookChapter.Page.Builder title(Component title) {
                this.title = title;
                return this;
            }

            public com.minelittlepony.unicopia.container.spellbook.SpellbookChapter.Page.Builder level(int level) {
                this.level = level;
                return this;
            }

            public com.minelittlepony.unicopia.container.spellbook.SpellbookChapter.Page.Builder color(int color) {
                this.color = color;
                return this;
            }

            public com.minelittlepony.unicopia.container.spellbook.SpellbookChapter.Page.Builder element(ChapterPageElement...elements) {
                this.elements.addAll(List.of(elements));
                return this;
            }

            public com.minelittlepony.unicopia.container.spellbook.SpellbookChapter.Page.Builder apply(UnaryOperator<com.minelittlepony.unicopia.container.spellbook.SpellbookChapter.Page.Builder> action) {
                return action.apply(this);
            }

            public Page build() {
                return new Page(title, level, color, List.copyOf(elements));
            }
        }
    }

    public static Page.Builder page() {
        return new Page.Builder();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private TabSide side = TabSide.LEFT;
        private int color;
        private int tabY;
        private final List<Page> pages = new ArrayList<>();

        private Builder() {}

        public Builder side(TabSide side) {
            this.side = side;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder tabY(int tabY) {
            this.tabY = tabY;
            return this;
        }

        public Builder page(Page.Builder page) {
            this.pages.add(page.build());
            return this;
        }

        public Builder apply(UnaryOperator<Builder> action) {
            return action.apply(this);
        }

        public SpellbookChapter build() {
            return new SpellbookChapter(side, tabY, color, List.copyOf(pages));
        }
    }
}