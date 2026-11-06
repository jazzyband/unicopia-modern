package com.minelittlepony.unicopia.datagen.providers;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.util.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;

public class AdvancementDisplayBuilder {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png");

    public static AdvancementDisplayBuilder create(ItemLike icon) {
        AdvancementDisplayBuilder builder = new AdvancementDisplayBuilder(icon, Advancement.Builder.advancement(), false, false, false);
        builder.root = true;
        return builder;
    }

    @Nullable
    private Identifier background = BACKGROUND;

    private boolean toast;
    private boolean hidden;
    private boolean announce;
    private final ItemLike icon;
    private boolean root;
    private AdvancementType frame = AdvancementType.TASK;
    @Nullable
    private String group;

    private final Advancement.Builder advancementBuilder;

    AdvancementDisplayBuilder(ItemLike icon, Advancement.Builder advancementBuilder, boolean toast, boolean announce, boolean hidden) {
        this.icon = icon;
        this.advancementBuilder = advancementBuilder;
        this.toast = toast;
        this.announce = announce;
        this.hidden = hidden;
    }

    public AdvancementDisplayBuilder frame(AdvancementType frame) {
        this.frame = frame;
        return this;
    }

    public AdvancementDisplayBuilder background(Identifier background) {
        this.background = background;
        return this;
    }

    public AdvancementDisplayBuilder showToast() {
        this.toast = true;
        return this;
    }

    public AdvancementDisplayBuilder hidden() {
        this.hidden = true;
        return this;
    }

    public AdvancementDisplayBuilder visible() {
        this.hidden = false;
        return this;
    }

    public AdvancementDisplayBuilder announce() {
        this.announce = true;
        return this;
    }

    public AdvancementDisplayBuilder doNotAnnounce() {
        this.announce = false;
        return this;
    }

    public AdvancementDisplayBuilder group(String group) {
        this.group = group;
        return this;
    }

    public AdvancementDisplayBuilder rewards(AdvancementRewards.Builder builder) {
        advancementBuilder.rewards(builder.build());
        return this;
    }

    public AdvancementDisplayBuilder criterion(String name, Criterion<?> criterion) {
        advancementBuilder.addCriterion(name, criterion);
        return this;
    }

    public AdvancementDisplayBuilder criteriaMerger(AdvancementRequirements.Strategy merger) {
        advancementBuilder.requirements(merger);
        return this;
    }

    public AdvancementDisplayBuilder parent(Identifier parent) {
        root = false;
        advancementBuilder.parent(Advancement.Builder.recipeAdvancement().build(parent));
        return this;
    }

    public AdvancementDisplayBuilder apply(Consumer<AdvancementDisplayBuilder> consumer) {
        consumer.accept(this);
        return this;
    }

    public Parent build(Consumer<AdvancementHolder> exporter, String name) {
        Identifier id = Unicopia.id(group == null ? name : group + "/" + name);
        String key = Util.makeDescriptionId("advancements", Unicopia.id(name));
        Component title = Component.translatable(key + ".title");
        Component description = Component.translatable(key + ".description");
        if (root) {
            advancementBuilder.rootDisplay(icon.asItem(), title, description, background, frame, toast, announce, hidden);
        } else {
            advancementBuilder.display(icon.asItem(), title, description, frame, toast, announce, hidden);
        }
        AdvancementHolder advancement = advancementBuilder.build(id);
        exporter.accept(advancement);
        return new Parent(advancement, group);
    }

    public record Parent(AdvancementHolder parent, @Nullable String group) {
        public AdvancementDisplayBuilder child(ItemLike icon) {
            DisplayInfo display = parent.value().display().orElseThrow();
            return new AdvancementDisplayBuilder(icon, Advancement.Builder.advancement().parent(parent),
                    display.showToast(),
                    display.announceToChat(),
                    display.hidden()
            ).frame(display.type()).group(group);
        }

        public void children(Consumer<Parent> children) {
            children.accept(this);
        }

        public void children(Consumer<AdvancementHolder> exporter, BiConsumer<Consumer<AdvancementHolder>, Parent> children) {
            children.accept(exporter, this);
        }
    }
}
