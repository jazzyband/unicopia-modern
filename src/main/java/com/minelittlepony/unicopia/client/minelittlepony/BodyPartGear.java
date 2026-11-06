package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.minelittlepony.api.model.*;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.api.pony.PonyData;
import com.minelittlepony.client.model.ClientPonyModel;
import com.minelittlepony.client.model.ModelType;
import com.minelittlepony.api.model.PlayerModelKey;
import com.minelittlepony.client.model.entity.race.ChangelingModel;
import com.minelittlepony.client.model.entity.race.PegasusModel;
import com.minelittlepony.client.model.entity.race.UnicornModel;
import com.minelittlepony.client.model.part.UnicornHorn;
import com.minelittlepony.client.render.entity.state.PlayerPonyRenderState;
import com.minelittlepony.client.render.entity.state.PonyRenderState;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.unicopia.FlightType;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.item.UItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.minelittlepony.unicopia.client.render.RenderGeometry;
import com.minelittlepony.client.render.entity.state.PonyRenderState;
import com.minelittlepony.api.pony.meta.Race;

class BodyPartGear<M extends ClientPonyModel<PonyRenderState>> implements Gear<com.minelittlepony.client.render.entity.state.PonyRenderState> {
    private static final Predicate<PlayerPonyRenderState> MINE_LP_HAS_NO_WINGS = s -> !s.getRace().hasWings() && !s.getRace().hasBugWings();
    private static final Predicate<PlayerPonyRenderState> MINE_LP_HAS_NO_HORN = s -> !s.getRace().hasHorn();

    private static final Predicate<PlayerPonyRenderState> EXCLUDE_PEGASUS_AMULET = s -> !CasterState.of(s).amulet.key.equals(UItems.PEGASUS_AMULET.builtInRegistryHolder().key());
    private static final Predicate<PlayerPonyRenderState> INCLUDE_ALICORN_AMULET = s -> CasterState.of(s).amulet.key.equals(UItems.ALICORN_AMULET.builtInRegistryHolder().key());

    private static final Identifier ICARUS_WINGS = Unicopia.id("textures/models/wings/icarus_pony.png");
    private static final Identifier ICARUS_WINGS_CORRUPTED = Unicopia.id("textures/models/wings/icarus_corrupted_pony.png");

    public static final Predicate<PlayerPonyRenderState> BUG_WINGS_PREDICATE = MINE_LP_HAS_NO_WINGS.and(EXCLUDE_PEGASUS_AMULET).and(s -> CasterState.of(s).species.any(com.minelittlepony.unicopia.Race.CHANGELING::equals));
    public static final Identifier BUG_WINGS = Unicopia.id("textures/models/wings/bug_pony.png");

    public static final Predicate<PlayerPonyRenderState> BAT_WINGS_PREDICATE = MINE_LP_HAS_NO_WINGS.and(EXCLUDE_PEGASUS_AMULET).and(s -> CasterState.of(s).species.any(com.minelittlepony.unicopia.Race.BAT::equals));
    public static final Identifier BAT_WINGS = Unicopia.id("textures/models/wings/bat_pony.png");

    public static final Predicate<PlayerPonyRenderState> UNICORN_HORN_PREDICATE = MINE_LP_HAS_NO_HORN.and(INCLUDE_ALICORN_AMULET.or(s -> CasterState.of(s).species.any(r -> r.canCast())));
    public static final Identifier UNICORN_HORN = Unicopia.id("textures/models/horn/unicorn.png");

    public static final Predicate<PlayerPonyRenderState> PEGA_WINGS_PREDICATE = MINE_LP_HAS_NO_WINGS.and(INCLUDE_ALICORN_AMULET.or(s -> CasterState.of(s).species.any(race -> race != com.minelittlepony.unicopia.Race.BAT && race.flightType() == FlightType.AVIAN)));
    public static final Identifier PEGASUS_WINGS = Unicopia.id("textures/models/wings/pegasus_pony.png");

    public static BodyPartGear<WingsGearModel> pegasusWings() {
        return new BodyPartGear<>(Race.PEGASUS, BodyPart.BODY, ModelType.PEGASUS, PEGA_WINGS_PREDICATE, WingsGearModel::new, e -> {
            if (CasterState.of(e).pegasusAmulet) {
                return CasterState.of(e).inHell ? ICARUS_WINGS_CORRUPTED : ICARUS_WINGS;
            }
            return PEGASUS_WINGS;
        });
    }

    public static BodyPartGear<WingsGearModel> batWings() {
        return new BodyPartGear<>(Race.BATPONY, BodyPart.BODY, ModelType.BAT_PONY, BAT_WINGS_PREDICATE, WingsGearModel::new, e -> BAT_WINGS);
    }

    public static BodyPartGear<BugWingsGearModel> bugWings() {
        return new BodyPartGear<>(Race.CHANGELING, BodyPart.BODY, ModelType.CHANGELING, BUG_WINGS_PREDICATE, BugWingsGearModel::new, e -> BUG_WINGS);
    }

    public static BodyPartGear<HornGearModel> unicornHorn() {
        return new BodyPartGear<>(Race.UNICORN, BodyPart.HEAD, ModelType.UNICORN, UNICORN_HORN_PREDICATE, HornGearModel::new, e -> UNICORN_HORN);
    }

    private final Race race;
    private final M model;
    private final Predicate<PlayerPonyRenderState> renderTargetPredicate;
    private final Function<PlayerPonyRenderState, Identifier> textureSupplier;
    private final BodyPart gearLocation;
    private final LoadingCache<PonyData, PonyData> dataCache = CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.SECONDS).build(CacheLoader.from(this::convertMetadata));

    private @Nullable PlayerPonyRenderState state;

    public BodyPartGear(
            Race race,
            BodyPart gearLocation,
            PlayerModelKey<? super M> modelKey,
            Predicate<PlayerPonyRenderState> renderTargetPredicate,
            MsonModel.Factory<M> modelFactory,
            Function<PlayerPonyRenderState, Identifier> textureSupplier) {
        this.race = race;
        this.gearLocation = gearLocation;
        this.model = modelKey.steveKey().createModel(modelFactory);
        this.renderTargetPredicate = renderTargetPredicate;
        this.textureSupplier = textureSupplier;
    }

    private PonyData convertMetadata(PonyData metadata) {
        return new PonyData(BodyPartGear.this.race,
                metadata.tailLength(), metadata.tailShape(),
                metadata.gender(), metadata.size(),
                metadata.glowColor(), metadata.noSkin(),
                metadata.priority(), metadata.gear());
    }

    @Override
    public BodyPart getGearLocation() {
        return gearLocation;
    }

    @Override
    public boolean canRender(PonyModel<?> model, PonyRenderState state) {
        return state instanceof PlayerPonyRenderState pony
            && !pony.getRace().isHuman()
            && pony.getRace() != race
            && renderTargetPredicate.test(pony);
    }

    @Override
    public Identifier getTexture(PonyRenderState entity, Context<PonyRenderState, ?> context) {
        return textureSupplier.apply((PlayerPonyRenderState)entity);
    }

    public static PonyData copyOf(PonyData metadata, Race newRace) {
        return new PonyData(newRace, metadata.tailLength(), metadata.tailShape(), metadata.gender(),
                metadata.size(), metadata.glowColor(), metadata.noSkin(), metadata.priority(), metadata.gear());
    }

    @Override
    public void render(PoseStack stack, GearRenderState<PonyRenderState> renderState, SubmitNodeCollector frame,
            RenderType renderType, int overlay, int light, int color) {
        PlayerPonyRenderState state = (PlayerPonyRenderState)renderState.entityState;
        var originalRace = state.getRace();
        var originalData = state.getAttributes().metadata;
        try {
            state.getAttributes().metadata = this.dataCache.get(originalData);
            state.race = state.getAttributes().metadata.race();
            model.setupAnim(state);
            RenderGeometry.submit(stack, frame, renderType, (pose, buffer) -> {
                ModelPart body = model.getBodyPart(gearLocation);
                if (gearLocation == BodyPart.HEAD) {
                    renderNamedPart(body, "horn", pose, buffer, light, overlay, color);
                } else {
                    renderNamedPart(body, "left_wing", pose, buffer, light, overlay, color);
                    renderNamedPart(body, "right_wing", pose, buffer, light, overlay, color);
                    renderNamedPart(body, "wings", pose, buffer, light, overlay, color);
                }
            });
        } catch (ExecutionException ignored) {
        } finally {
            state.getAttributes().metadata = originalData;
            state.race = originalRace;
        }
    }

    private static void renderNamedPart(ModelPart parent, String name, PoseStack stack,
            com.mojang.blaze3d.vertex.VertexConsumer buffer, int light, int overlay, int color) {
        if (parent.hasChild(name)) {
            stack.pushPose();
            parent.translateAndRotate(stack);
            parent.getChild(name).render(stack, buffer, light, overlay, color);
            stack.popPose();
        }
    }

    static final class WingsGearModel extends PegasusModel<PonyRenderState> {
        public WingsGearModel(ModelPart tree) {
            super(tree, false);
        }
    }

    static final class BugWingsGearModel extends ChangelingModel<PonyRenderState> {
        public BugWingsGearModel(ModelPart tree) {
            super(tree, false);
        }
    }

    static final class HornGearModel extends UnicornModel<PonyRenderState> {
        public HornGearModel(ModelPart tree) {
            super(tree, false);
        }

        public UnicornHorn<PonyRenderState> getHorn() {
            return horn;
        }
    }
}

