package com.minelittlepony.unicopia.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.minelittlepony.common.util.settings.ToStringAdapter;
import com.minelittlepony.unicopia.Unicopia;

@Deprecated
public interface Resources {
    Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new ToStringAdapter<>(Identifier::parse))
            .create();

    static Stream<Resource> getResources(ResourceManager manager, Identifier id) {
        return manager.getResourceStack(id).stream();
    }

    static Stream<Identifier> loadFile(Resource res, Type type, String msg) throws JsonParseException {
        try (InputStreamReader reader = new InputStreamReader(res.open())) {
            return (GSON.<List<Identifier>>fromJson(reader, type)).stream();
        } catch (JsonParseException e) {
            Unicopia.LOGGER.warn(msg + res.sourcePackId(), e);
        } catch (IOException ignored) {}

        return Stream.empty();
    }
}
