package com.minelittlepony.unicopia.block.state;

import java.io.*;
import java.util.*;
import org.slf4j.Logger;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.minelittlepony.unicopia.Unicopia;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

public class StateMapLoader extends SimpleJsonResourceReloadListener<ReversableBlockStateConverter> implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Unicopia.id("data/state_maps");

    public static final StateMapLoader INSTANCE = new StateMapLoader();

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_SUFFIX = ".json";
    private static final int FILE_SUFFIX_LENGTH = ".json".length();
    private static final String DATA_TYPE = "state_maps";

    Map<Identifier, ? extends ReversableBlockStateConverter> converters = Map.of();

    public StateMapLoader() {
        super(ReversableBlockStateConverterImpl.CODEC, FileToIdConverter.json("state_maps"));
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected Map<Identifier, ReversableBlockStateConverter> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        int i = DATA_TYPE.length() + 1;

        Map<Identifier, ReversableBlockStateConverter> map = Maps.newHashMap();

        resourceManager.listResourceStacks(DATA_TYPE, id -> id.getPath().endsWith(FILE_SUFFIX)).entrySet().stream().forEach(entry -> {
            Identifier resId = entry.getKey();
            Identifier id = resId.withPath(p -> p.substring(i, p.length() - FILE_SUFFIX_LENGTH));

            JsonArray entries = new JsonArray();
            for (var resource : entry.getValue()) {
                try (BufferedReader reader = resource.openAsReader()) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                    if (!json.has("entries")) {
                        LOGGER.error("Couldn't load data file {} from {} as it's null or empty", id, resId);
                        continue;
                    }

                    JsonArray incoming = GsonHelper.getAsJsonArray(json, "entries");
                    if (json.has("replace") && json.get("replace").getAsBoolean()) {
                        entries = incoming;
                    } else {
                        entries.addAll(incoming);
                    }
                } catch (JsonParseException | IOException | IllegalArgumentException e) {
                    LOGGER.error("Couldn't parse data file {} from {}", id, resId, e);
                }
            }

            ReversableBlockStateConverterImpl.CODEC.decode(JsonOps.INSTANCE, entries).result().ifPresent(pair -> {
                map.put(id, pair.getFirst());
            });
        });
        return map;
    }

    @Override
    protected void apply(Map<Identifier, ReversableBlockStateConverter> data, ResourceManager manager, ProfilerFiller profiler) {
        converters = data;
    }
}
