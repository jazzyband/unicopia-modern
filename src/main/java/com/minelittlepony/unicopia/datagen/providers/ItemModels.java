package com.minelittlepony.unicopia.datagen.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.conditional.FishingRodCast;
import net.minecraft.client.renderer.item.properties.numeric.Damage;
import net.minecraft.client.renderer.item.properties.numeric.Time;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import com.google.common.base.Strings;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.item.BalloonDesignProperty;
import com.minelittlepony.unicopia.client.render.item.ButterflyVariantProperty;
import com.minelittlepony.unicopia.client.render.item.GemShapeProperty;
import com.minelittlepony.unicopia.client.render.item.JarContentsModelRenderer;
import com.minelittlepony.unicopia.client.render.item.PolearmModelRenderer;
import com.minelittlepony.unicopia.client.render.item.SpellTintSource;
import com.minelittlepony.unicopia.client.render.item.ZapAppleCycleProperty;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.minelittlepony.unicopia.item.GemstoneItem;
import com.minelittlepony.unicopia.item.UItems;

import static net.minecraft.client.data.models.model.ItemModelUtils.*;

interface ItemModels {
    ModelTemplate GENERATED = net.minecraft.client.data.models.model.ModelTemplates.FLAT_ITEM;
    ModelTemplate CHEST = item(Identifier.withDefaultNamespace("chest"), TextureSlot.PARTICLE);
    ModelTemplate BUILTIN_ENTITY = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("builtin/entity")), Optional.empty());
    ModelTemplate TEMPLATE_AMULET = item("template_amulet", TextureSlot.LAYER0);
    ModelTemplate TEMPLATE_EYEWEAR = item("template_eyewear", TextureSlot.LAYER0);
    ModelTemplate TEMPLATE_MUG = item("template_mug", TextureSlot.LAYER0);
    ModelTemplate TEMPLATE_PILLAR = item("template_pillar", TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE, TextureSlot.END);
    ModelTemplate HANDHELD_STAFF = item("handheld_staff", TextureSlot.LAYER0);
    ModelTemplate TRIDENT_THROWING = item(Identifier.withDefaultNamespace("trident_throwing"), TextureSlot.LAYER0);
    ModelTemplate TRIDENT_IN_HAND = item(Identifier.withDefaultNamespace("trident_in_hand"), TextureSlot.LAYER0);

    static ModelTemplate item(String parent, TextureSlot ... requiredTextureKeys) {
        return item(Unicopia.id(parent), requiredTextureKeys);
    }

    static ModelTemplate item(Identifier parent, TextureSlot ... requiredTextureKeys) {
        return new ModelTemplate(Optional.of(parent.withPrefix("item/")), Optional.empty(), requiredTextureKeys);
    }

    static ItemTintSource spellTintSource(int defaultColor) {
        return new SpellTintSource(defaultColor);
    }

    static void register(ItemModelGenerators itemModelGenerator, Item... items) {
        register(itemModelGenerator, GENERATED, items);
    }

    static void register(ItemModelGenerators itemModelGenerator, ModelTemplate model, Item... items) {
        for (Item item : items) {
            itemModelGenerator.generateFlatItem(item, model);
        }
    }

    static void registerDyeable(ItemModelGenerators itemModelGenerator, Item item, int defaultColor) {
        itemModelGenerator.itemModelOutput.accept(item, tintedModel(itemModelGenerator.createFlatItemModel(item, GENERATED), new Dye(defaultColor)));
    }

    static void registerParented(ItemModelGenerators itemModelGenerator, Item item, ItemLike parent) {
        item(BuiltInRegistries.ITEM.getKey(parent.asItem())).create(ModelLocationUtils.getModelLocation(item), new TextureMapping(), itemModelGenerator.modelOutput);
    }

    static void registerPolearm(ItemModelGenerators itemModelGenerator, Item item) {
        TextureMapping textures = TextureMapping.layer0(TextureMapping.getItemTexture(item));

        var inInventory = plainModel(GENERATED.create(ModelLocationUtils.getModelLocation(item, "_in_inventory"), textures, itemModelGenerator.modelOutput));
        var throwing = TRIDENT_THROWING.create(ModelLocationUtils.getModelLocation(item, "_throwing"), textures, itemModelGenerator.modelOutput);
        var inHand = TRIDENT_IN_HAND.create(ModelLocationUtils.getModelLocation(item), textures, itemModelGenerator.modelOutput);

        itemModelGenerator.itemModelOutput.accept(item, select(new DisplayContext(), conditional(isUsingItem(),
                specialModel(throwing, new PolearmModelRenderer.Unbaked(ModelLocationUtils.getModelLocation(item))),
                specialModel(inHand, new PolearmModelRenderer.Unbaked(ModelLocationUtils.getModelLocation(item)))
        ), when(List.of(ItemDisplayContext.GUI, ItemDisplayContext.GROUND, ItemDisplayContext.FIXED), inInventory)));
    }

    @SuppressWarnings("unchecked")
    static void registerButterfly(ItemModelGenerators itemModelGenerator, Item item) {
        var base = plainModel(itemModelGenerator.createFlatItemModel(item, GENERATED));
        itemModelGenerator.itemModelOutput.accept(item, select(
                ButterflyVariantProperty.INSTANCE,
                base,
                Arrays.stream(ButterflyEntity.Variant.VALUES)
                    .map(variant -> when(variant, variant == ButterflyEntity.Variant.BUTTERFLY ? base : createVariantItemModel(item, itemModelGenerator, variant)))
                    .toArray(SelectItemModel.SwitchCase[]::new)));
    }

    static void registerStagedFoodItem(ItemModelGenerators itemModelGenerator, Item item, int minBites, int maxBites, String suffex) {
        itemModelGenerator.itemModelOutput.accept(item, rangeSelect(new Damage(true),
                plainModel(itemModelGenerator.createFlatItemModel(item, GENERATED)),
                IntStream.range(minBites, maxBites + 1)
                    .mapToObj(count -> override(plainModel(itemModelGenerator.createFlatItemModel(item, "_" + suffex + count, GENERATED)), count / (maxBites + 1F)))
                    .toList()
        ));
    }

    static void registerMagicStaff(ItemModelGenerators itemModelGenerator, Item item) {
        var base = plainModel(itemModelGenerator.createFlatItemModel(item, "_base", HANDHELD_STAFF));
        var magicOverlay = tintedModel(itemModelGenerator.createFlatItemModel(item, "_magic", HANDHELD_STAFF), spellTintSource(CommonColors.WHITE));
        itemModelGenerator.itemModelOutput.accept(item, composite(base, magicOverlay));
    }

    static void registerGemstone(ItemModelGenerators itemModelGenerator, Item item) {
        itemModelGenerator.itemModelOutput.accept(item, select(new GemShapeProperty(),
            tintedModel(itemModelGenerator.createFlatItemModel(item, GENERATED), spellTintSource(CommonColors.WHITE)),
            Arrays.stream(GemstoneItem.Shape.values())
                .map(shape -> when(shape, tintedModel(itemModelGenerator.createFlatItemModel(item, "_" + shape.getSerializedName(), GENERATED), spellTintSource(CommonColors.WHITE))))
                .toList()
        ));
    }

    @SuppressWarnings("unchecked")
    static void registerBalloonDesigns(ItemModelGenerators itemModelGenerator, Item item) {
        var base = plainModel(itemModelGenerator.createFlatItemModel(item, GENERATED));
        itemModelGenerator.itemModelOutput.accept(item, select(
                BalloonDesignProperty.INSTANCE,
                base,
                Arrays.stream(AirBalloonEntity.BalloonDesign.VALUES)
                    .map(variant -> when(variant, variant == AirBalloonEntity.BalloonDesign.NONE ? base : createVariantItemModel(item, itemModelGenerator, variant)))
                    .toArray(SelectItemModel.SwitchCase[]::new)));
    }

    static void registerCustomFishingRod(ItemModelGenerators itemModelGenerator, Item item) {
        itemModelGenerator.generateBooleanDispatch(item, new FishingRodCast(),
                plainModel(ModelLocationUtils.getModelLocation(Items.FISHING_ROD, "_cast")),
                plainModel(itemModelGenerator.createFlatItemModel(item, ModelTemplates.FLAT_HANDHELD_ROD_ITEM))
        );
    }

    private static <T extends StringRepresentable> ItemModel.Unbaked createVariantItemModel(Item item, ItemModelGenerators itemModelGenerator, T variant) {
        Identifier subModelId = BuiltInRegistries.ITEM.getKey(item).withPath(p -> "item/" + variant.getSerializedName() + "_" + p);
        return plainModel(GENERATED.create(subModelId, TextureMapping.layer0(new net.minecraft.client.resources.model.sprite.Material(subModelId)), itemModelGenerator.modelOutput));
    }

    static void registerFilledJar(ItemModelGenerators itemModelGenerator, Item jar) {
        itemModelGenerator.itemModelOutput.accept(jar.asItem(), composite(
                plainModel(ModelLocationUtils.getModelLocation(UItems.EMPTY_JAR)),
                specialModel(ModelTemplates.CHEST_INVENTORY.create(jar.asItem(), TextureMapping.particle(Blocks.GLASS), itemModelGenerator.modelOutput), new JarContentsModelRenderer.Unbaked())
            )
        );
    }

    static void registerSpectralClock(ItemModelGenerators itemModelGenerator, Item clock) {
        List<RangeSelectItemModel.Entry> list = new ArrayList<>();
        ItemModel.Unbaked defaultModel = plainModel(itemModelGenerator.createFlatItemModel(clock, "_00", ModelTemplates.FLAT_ITEM));
        list.add(override(defaultModel, 0F));

        String[] suffexes = { "", "_greening", "_flowering", "_fruiting", "_ripe", "" };

        for (int index = 1; index < 40; index++) {
            Identifier subModelId = itemModelGenerator.createFlatItemModel(clock, suffexes[index / 8] + "_" + Strings.padStart((index % 8) * 5 + "", 2, '0'), ModelTemplates.FLAT_ITEM);
            list.add(override(plainModel(subModelId), index / 40F - 0.5F));
        }

        list.add(override(defaultModel, 1F));
        itemModelGenerator.itemModelOutput.accept(
                clock,
                inOverworld(
                    rangeSelect(new ZapAppleCycleProperty(true), 64.0F, list),
                    rangeSelect(new Time(true, Time.TimeSource.RANDOM), 1F, list)
                )
            );
    }
}
