package com.minelittlepony.unicopia.container.spellbook;

import java.util.*;
import java.util.concurrent.*;
import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgServerResources;
import com.minelittlepony.unicopia.util.Untyped;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.util.Util;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.InactiveProfiler;

public class SpellbookChapterLoader extends SimpleJsonResourceReloadListener<SpellbookChapter> implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Unicopia.id("spellbook/chapters");
    private static final Executor EXECUTOR = CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS);

    public static final SpellbookChapterLoader INSTANCE = new SpellbookChapterLoader();

    private boolean dirty;
    private Map<Identifier, SpellbookChapter> chapters = new HashMap<>();

    public SpellbookChapterLoader() {
        super(SpellbookChapter.CODEC, FileToIdConverter.json(ID.getPath()));
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    public Map<Identifier, SpellbookChapter> getChapters() {
        return Untyped.cast(chapters);
    }

    public void sendUpdate(MinecraftServer server) {
        if (dirty) {
            dirty = false;
            Channel.SERVER_RESOURCES.sendToAllPlayers(new MsgServerResources(server), server);
        }
    }

    @Override
    protected void apply(Map<Identifier, SpellbookChapter> data, ResourceManager manager, ProfilerFiller profiler) {
        chapters = data;

        if (Debug.SPELLBOOK_CHAPTERS) {
            CompletableFuture.runAsync(() -> {
                apply(prepare(manager, InactiveProfiler.INSTANCE), manager, InactiveProfiler.INSTANCE);
                dirty = true;
            }, EXECUTOR);
        }
    }
}
