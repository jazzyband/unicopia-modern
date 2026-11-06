package com.minelittlepony.unicopia.client.render.entity.state;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.HeldEntityFeatureRenderer;
import com.minelittlepony.unicopia.client.render.PlayerPoser.AnimationInstance;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.AmuletSelectors;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.entity.player.SkinFeatures;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.GlassesItem;
import com.minelittlepony.unicopia.item.GlowableItem;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;

public class CasterState {
    public boolean present;
    public EntityType<?> type;

    public boolean living;

    public boolean ponified;
    public float eatingHeadAngle;

    public List<Component> debugLines;
    public float width;
    public boolean isCamera;
    public boolean isProjectile;
    public boolean isPlacement;

    public boolean showDebugInfo;
    public boolean hasDebugInfo;

    public Vec3 originVector = Vec3.ZERO;

    public final EntityRenderState entityState;

    @Nullable
    public Component masterDisplayName;

    public List<SpellRenderState> spells = List.of();

    public Race.Composite species = Race.UNSET.composite();
    public SkinFeatures skinFeatures = SkinFeatures.DEFAULT;

    public float wingsAngle;
    public boolean dashing;
    public boolean flying;

    public final AbilityState activeAbility = new AbilityState();
    public int activeMagicColor;

    public EquippedStackRenderState amulet = new EquippedStackRenderState();
    public boolean pegasusAmulet;
    public boolean inHell;

    public boolean isLeftPolearm;
    public boolean isRightPolearm;

    public final BangleState mainhandBangle = new BangleState();
    public final BangleState offhandBangle = new BangleState();
    @Deprecated
    public TrinketsDelegate.EquippedStack eyewear = TrinketsDelegate.EquippedStack.EMPTY;

    public boolean hasCoolShades;

    public float leanAmount;
    public float yawOffset;
    public float gemYaw;

    public final PassengerState<?, ?> carriedEntity = new PassengerState<>();

    public AnimationInstance animation = AnimationInstance.NONE;
    public float animationTime;

    @Nullable
    public Entity appearance;

    public CasterState(EntityRenderState entityState) {
        this.entityState = entityState;
    }

    public void clear() {
        present = false;
        living = false;
        debugLines = null;
        originVector = Vec3.ZERO;
        isCamera = false;
        isProjectile = false;
        ponified = false;
        isPlacement = false;
        showDebugInfo = false;
        hasDebugInfo = false;
        pegasusAmulet = false;
        isLeftPolearm = false;
        isRightPolearm = false;
        hasCoolShades = false;
        inHell = false;
        leanAmount = 0;
        yawOffset = 0;
        gemYaw = 0;
        type = null;
        eatingHeadAngle = 0;
        masterDisplayName = null;
        appearance = null;
        wingsAngle = 0;
        dashing = false;
        flying = false;
        carriedEntity.clear();
        spells = spells.isEmpty() ? spells : List.of();
        animation = AnimationInstance.NONE;
        animationTime = 0;
        species = Race.UNSET.composite();
        skinFeatures = SkinFeatures.DEFAULT;
        amulet.update(TrinketsDelegate.EquippedStack.EMPTY);
        mainhandBangle.update(TrinketsDelegate.EquippedStack.EMPTY);
        offhandBangle.update(TrinketsDelegate.EquippedStack.EMPTY);
        eyewear = TrinketsDelegate.EquippedStack.EMPTY;
        activeAbility.update(null, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void update(@Nullable Caster<?> caster, float tickDelta) {
        clear();
        if (caster != null) {
            present = true;
            living = caster instanceof Living;
            hasDebugInfo = caster instanceof Pony || caster instanceof CastSpellEntity;
            type = caster.asEntity().getType();
            width = caster.asEntity().getBbWidth();
            originVector = caster.getOriginVector();

            if (type == EntityTypes.PIG && caster instanceof Creature creature) {
                eatingHeadAngle = creature.getHeadAngle(tickDelta);
            }

            Minecraft client = Minecraft.getInstance();

            isCamera = caster.asEntity() == client.getCameraEntity();
            isProjectile = caster.asEntity() instanceof MagicProjectileEntity;
            isPlacement = caster.asEntity() instanceof CastSpellEntity;
            inHell = caster.asEntity().level().dimension().equals(net.minecraft.world.level.Level.NETHER);

            showDebugInfo = hasDebugInfo
                    && client.getDebugOverlay().showDebugScreen()
                    && !client.showOnlyReducedInfo()
                    && !(caster.asEntity() == client.getCameraEntity() && client.options.getCameraType() == CameraType.FIRST_PERSON);

            spells = caster.getSpellSlot().stream().map(spell -> {
                SpellRenderer<?, ?> renderer = SpellEffectsRenderDispatcher.INSTANCE.getRenderer(spell);
                SpellRenderState spellState = renderer.createRenderState();
                ((SpellRenderer)renderer).updateRenderState(spell, spellState, caster, tickDelta);
                return spellState;
            }).toList();
            activeMagicColor = spells.stream().filter(i -> i.type.type() != SpellType.PLACE_CONTROL_SPELL).map(spell -> spell.type.type().getColor()).findFirst().orElse(0);

            appearance = caster.getSpellSlot().get(SpellPredicate.IS_DISGUISE)
                    .map(Disguise.class::cast)
                    .map(Disguise::getAppearance)
                    .map(EntityAppearance::getEntity)
                    .orElse(null);

            if (caster instanceof Pony pony) {
                species = pony.getCompositeRace();
                skinFeatures = pony.getSkinFeatures();
                Vec3 motion = pony.getMotion().getTrackedVelocity();
                flying = pony.getMotion().isFlying();
                dashing = (flying && (Math.sqrt(motion.x * motion.x + motion.z * motion.z) > 0.4 || pony.getMotion().isDiving())) || pony.getMotion().isRainbooming();
                wingsAngle = pony.getMotion().getWingAngle();
                activeAbility.update(pony.getAbilities().getActiveStat().orElse(null), pony);
                ponified = MineLPDelegate.getInstance().getPlayerPonyRace(pony.asEntity()).isEquine();
                animation = pony.getAnimation();
                animationTime = pony.getAnimationProgress(tickDelta);
            }

            if (caster instanceof Living l) {
                amulet.update(AmuletItem.get(l.asEntity()));
                pegasusAmulet = AmuletSelectors.PEGASUS_AMULET.test(l.asEntity());
                mainhandBangle.update(FriendshipBraceletItem.getWornBangles(l.asEntity(), TrinketsDelegate.MAIN_GLOVE).findFirst().orElse(TrinketsDelegate.EquippedStack.EMPTY));
                offhandBangle.update(FriendshipBraceletItem.getWornBangles(l.asEntity(), TrinketsDelegate.SECONDARY_GLOVE).findFirst().orElse(TrinketsDelegate.EquippedStack.EMPTY));
                isLeftPolearm = l.asEntity().getItemHeldByArm(HumanoidArm.LEFT).is(UTags.Items.POLEARMS);
                isRightPolearm = l.asEntity().getItemHeldByArm(HumanoidArm.RIGHT).is(UTags.Items.POLEARMS);
                eyewear = GlassesItem.getForEntity(l.asEntity());
                hasCoolShades = eyewear.stack().getCustomName() != null && "Cool Shades".equals(eyewear.stack().getCustomName().getString());
                leanAmount = ((LivingEntityDuck)l.asEntity()).getLeaningPitch();
                yawOffset = -(((LivingEntityRenderState)this.entityState).yRot + ((LivingEntityRenderState)this.entityState).bodyRot);
                gemYaw = l.asEntity().isSleeping() ? 0 : 180 - ((LivingEntityRenderState)this.entityState).bodyRot;
            }

            if (client.getDebugOverlay().showDebugScreen()
                    && !client.showOnlyReducedInfo()
                    && !(caster.asEntity() == client.getCameraEntity() && client.options.getCameraType() == CameraType.FIRST_PERSON)) {
                if (caster instanceof Pony || caster instanceof CastSpellEntity) {
                    debugLines = Stream.concat(
                            Stream.of(
                                    caster.asEntity().getDisplayName().copy().append(" (" + BuiltInRegistries.ENTITY_TYPE.getKey(caster.asEntity().getType()) + ")"),
                                    caster.getMaster() != null ? Component.literal("Master: ").append(caster.getMaster().getDisplayName()) : Component.empty()
                            ),
                            caster.getSpellSlot().stream(SpellPredicate.ALL).flatMap(spell ->
                                Stream.of(
                                        Component.literal("UUID: " + spell.getUuid()),
                                        Component.literal("|>Type: ").append(Component.literal(spell.getTypeAndTraits().type().getId().toString()).withStyle(s -> s.withColor(spell.getTypeAndTraits().type().getColor()))),
                                        Component.nullToEmpty("|>Traits: " + spell.getTypeAndTraits().traits()),
                                        Component.literal("|>HasRenderer: ").append(Component.literal((SpellEffectsRenderDispatcher.INSTANCE.getRenderer(spell) != null) + "").withStyle(SpellEffectsRenderDispatcher.INSTANCE.getRenderer(spell) != null ? ChatFormatting.GREEN : ChatFormatting.RED))
                                )
                            )
                    ).toList();
                }
            }

            LivingEntity master = caster.getMaster();
            if (master != null) {
                masterDisplayName = master.getDisplayName();
            }
        }
    }

    public interface Container {
        CasterState getUnicopiaState();
    }

    public static CasterState of(EntityRenderState state) {
        return ((Container)state).getUnicopiaState();
    }

    public static CasterState of(Entity entity, float tickDelta) {
        return of(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity).createRenderState(entity, tickDelta));
    }

    public static class BangleState {
        public boolean empty;
        public int color;
        public boolean glowing;

        public void update(TrinketsDelegate.EquippedStack stack) {
            empty = stack.isEmpty();
            color = empty ? CommonColors.WHITE : DyedItemColor.getOrDefault(stack.stack(), CommonColors.WHITE);
            glowing = !empty && GlowableItem.isGlowing(stack.stack());
        }
    }

    public static class AbilityState {
        public Identifier id;
        public Component name;
        public int color;
        public float fillProgress;

        public void update(@Nullable AbilityDispatcher.Stat stat, Pony pony) {
            Ability<?> ability = stat == null ? null : stat.getActiveAbility().orElse(null);
            color = ability == null ? 0 : ability.getColor(pony);
            id = ability == null ? null : ability.getId();
            name = ability == null ? null : ability.getName(pony);
            fillProgress = stat == null ? 0 : stat.getFillProgress();
        }
    }

    public static class PassengerState<T extends LivingEntity, S extends LivingEntityRenderState> {
        public Vec3 carryPosition = Vec3.ZERO;
        public Vector3fc viewportPosition = new Vector3f();
        public boolean isPony;

        @Nullable
        private EntityRenderer<T, S> renderer;
        @Nullable
        public S state;

        @SuppressWarnings("unchecked")
        public void update(CasterState state, Living<T> entity, Living<T> passenger, float tickDelta) {
            if (passenger != null) {
                isPony = passenger instanceof Pony;
                carryPosition = HeldEntityFeatureRenderer.getCarryPosition(entity, passenger)
                        .xRot(-state.leanAmount * Mth.PI / 4F)
                        .add(new Vec3(0, -0.5F, 0).scale(state.leanAmount));
                viewportPosition = HeldEntityFeatureRenderer.getViewportPosition(entity, passenger, tickDelta);
                this.renderer = (EntityRenderer<T, S>)Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity.asEntity());
                this.state = renderer.createRenderState();
                renderer.extractRenderState(entity.asEntity(), this.state, tickDelta);
                this.state.yRot = 0;
                this.state.bodyRot = 0;
                this.state.walkAnimationPos = 0;
            } else {
                this.renderer = null;
                this.state = null;
            }
        }

        public void clear() {
            this.renderer = null;
            this.state = null;
        }

        public void render(PoseStack matrices, SubmitNodeCollector vertexConsumers, int light) {
            if (renderer != null) {
                var camera = Minecraft.getInstance().gameRenderer.mainCamera();
                var cameraState = new net.minecraft.client.renderer.state.level.CameraRenderState();
                cameraState.pos = camera.position();
                cameraState.orientation = camera.rotation();
                cameraState.initialized = true;
                renderer.submit(state, matrices, vertexConsumers, cameraState);
            }
        }
    }
}
