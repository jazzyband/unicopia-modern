package com.minelittlepony.unicopia.ability.data.tree;

import java.util.*;
import java.util.function.Supplier;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.Weighted;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class TreeTypeLoader extends SimpleJsonResourceReloadListener<TreeTypeLoader.TreeTypeDef> implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Unicopia.id("data/tree_type");

    public static final TreeTypeLoader INSTANCE = new TreeTypeLoader();

    private Map<Identifier, TreeTypeDef> entries = new HashMap<>();

    TreeTypeLoader() {
        super(TreeTypeDef.CODEC, FileToIdConverter.json("tree_types"));
    }

    public Map<Identifier, TreeTypeDef> getEntries() {
        return entries;
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected void apply(Map<Identifier, TreeTypeDef> resources, ResourceManager manager, ProfilerFiller profiler) {
        entries = resources;

        TreeTypes.load(entries);
    }

    public record TreeTypeDef (
            Set<ResourceKey<Block>> logs,
            Set<ResourceKey<Block>> leaves,
            Set<Drop> drops,
            boolean wideTrunk,
            int rarity,
            float leavesRatio
    ) {
        private static final Codec<Set<ResourceKey<Block>>> BLOCK_KEY_SET_CODEC = CodecUtils.setOf(ResourceKey.codec(Registries.BLOCK));
        private static final StreamCodec<ByteBuf, Set<ResourceKey<Block>>> BLOCK_KEY_SET_PACKET_CODEC = ResourceKey.streamCodec(Registries.BLOCK).apply(ByteBufCodecs.collection(HashSet::new));
        public static final Codec<TreeTypeDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BLOCK_KEY_SET_CODEC.fieldOf("logs").forGetter(TreeTypeDef::logs),
                BLOCK_KEY_SET_CODEC.fieldOf("leaves").forGetter(TreeTypeDef::leaves),
                CodecUtils.setOf(Drop.CODEC).fieldOf("drops").forGetter(TreeTypeDef::drops),
                Codec.BOOL.optionalFieldOf("wideTrunk", false).forGetter(TreeTypeDef::wideTrunk),
                Codec.INT.optionalFieldOf("rarity", 0).forGetter(TreeTypeDef::rarity),
                Codec.FLOAT.fieldOf("leavesRatio").forGetter(TreeTypeDef::leavesRatio)
        ).apply(instance, TreeTypeDef::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, TreeTypeDef> PACKET_CODEC = StreamCodec.composite(
                BLOCK_KEY_SET_PACKET_CODEC, TreeTypeDef::logs,
                BLOCK_KEY_SET_PACKET_CODEC, TreeTypeDef::leaves,
                Drop.PACKET_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), TreeTypeDef::drops,
                ByteBufCodecs.BOOL, TreeTypeDef::wideTrunk,
                ByteBufCodecs.INT, TreeTypeDef::rarity,
                ByteBufCodecs.FLOAT, TreeTypeDef::leavesRatio,
                TreeTypeDef::new
        );

        public static Builder builder() {
            return new Builder();
        }

        public TreeType toTreeType(Identifier id) {
            return new TreeTypeImpl(
                    id,
                    wideTrunk,
                    Objects.requireNonNull(logs, "TreeType must have logs"),
                    Objects.requireNonNull(leaves, "TreeType must have leaves"),
                    Weighted.of(drops),
                    rarity,
                    leavesRatio
            );
        }

        static record Drop (
                int weight,
                Optional<Identifier> tag,
                Optional<Identifier> item
        ) implements Weighted.Buildable<Supplier<ItemStack>> {
            public static final Codec<Drop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("weight").forGetter(Drop::weight),
                    Identifier.CODEC.optionalFieldOf("tag").forGetter(Drop::tag),
                    Identifier.CODEC.optionalFieldOf("item").forGetter(Drop::item)
            ).apply(instance, Drop::new));
            public static final StreamCodec<RegistryFriendlyByteBuf, Drop> PACKET_CODEC = StreamCodec.composite(
                    ByteBufCodecs.INT, Drop::weight,
                    ByteBufCodecs.optional(Identifier.STREAM_CODEC), Drop::tag,
                    ByteBufCodecs.optional(Identifier.STREAM_CODEC), Drop::item,
                    Drop::new
            );

            @Override
            public void appendTo(Weighted.Builder<Supplier<ItemStack>> weighted) {
                if (item.isPresent()) {
                    BuiltInRegistries.ITEM.getOptional(item.get()).ifPresent(item -> {
                        weighted.put(weight, item::getDefaultInstance);
                    });
                } else {
                    weighted.put(weight, () -> {
                        return BuiltInRegistries.ITEM.getRandomElementOf(TagKey.create(Registries.ITEM, tag.get()), Weighted.getRng())
                                .map(entry -> entry.value().getDefaultInstance())
                                .orElse(ItemStack.EMPTY);
                    });
                }
            }
        }

        public static class Builder {
            private final Set<ResourceKey<Block>> logs = new HashSet<>();
            private final Set<ResourceKey<Block>> leaves = new HashSet<>();
            private final Set<Drop> drops = new HashSet<>();

            private boolean wideTrunk = false;
            private int rarity;
            private float leavesRatio = 0.5F;

            public Builder drop(int weight, TagKey<Item> tag) {
                drops.add(new Drop(weight, Optional.of(tag.location()), Optional.empty()));
                return this;
            }

            public Builder drop(int weight, ItemLike item) {
                drops.add(new Drop(weight, Optional.empty(), Optional.of(BuiltInRegistries.ITEM.getKey(item.asItem()))));
                return this;
            }

            @SuppressWarnings("deprecation")
            public Builder logs(Block...blocks) {
                Arrays.stream(blocks).map(block -> block.builtInRegistryHolder().key()).forEach(logs::add);
                return this;
            }

            @SuppressWarnings("deprecation")
            public Builder leaves(Block...blocks) {
                Arrays.stream(blocks).map(block -> block.builtInRegistryHolder().key()).forEach(leaves::add);
                return this;
            }

            public Builder wideTrunk() {
                wideTrunk = true;
                return this;
            }

            public Builder rarity(int rarity) {
                this.rarity = rarity;
                return this;
            }

            public Builder leavesRatio(float leavesRatio) {
                this.leavesRatio = leavesRatio;
                return this;
            }

            public TreeTypeDef build() {
                return new TreeTypeDef(logs, leaves, drops, wideTrunk, rarity, leavesRatio);
            }
        }

    }
}
