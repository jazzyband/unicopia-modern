package com.minelittlepony.unicopia.client.render;

import java.util.Optional;
import java.util.Locale;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.command.CommandArgumentEnum;
import com.minelittlepony.unicopia.util.AnimationUtil;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class PlayerPoser {
    public static final PlayerPoser INSTANCE = new PlayerPoser();

    private static final float HEAD_NOD_DURATION = 15F;
    private static final float HEAD_NOD_GAP = HEAD_NOD_DURATION / 3F;

    public void applyBodyTransform(PoseStack matrices, AvatarRenderState state) {
        CasterState caster = CasterState.of(state);
        boolean equine = MineLPDelegate.getInstance().getRace(state).isEquine();
        if (!equine || !caster.animation.canPlay(true)) {
            return;
        }
        float progress = switch (caster.animation.animation()) {
            case HANG, CLIMB -> 1;
            case ARMS_UP, STOMP -> AnimationUtil.seesaw(caster.animationTime);
            default -> 0;
        };
        if (progress != 0) {
            rearUpTransform(matrices, progress);
        }
    }

    public void applyPosing(PoseStack matrices, AvatarRenderState state, HumanoidModel<?> model, Context context) {
        CasterState pony = CasterState.of(state);
        float progress = pony.animationTime;
        AnimationInstance animation = pony.animation;
        Race ponyRace = MineLPDelegate.getInstance().getRace(state);
        HumanoidArm mainArm = state.mainArm;

        boolean liftLeftArm = mainArm == HumanoidArm.LEFT || !ponyRace.isEquine();
        boolean liftRightArm = mainArm == HumanoidArm.RIGHT || !ponyRace.isEquine();

        ModelPart head = model.getHead();

        if (context == Context.THIRD_PERSON && !state.isCrouching) {
            InteractionHand leftHand = mainArm == HumanoidArm.LEFT ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            InteractionHand rightHand = mainArm == HumanoidArm.LEFT ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;


            float pitchChange = -0.5F;
            float yawChange = 0.8F;

            if (pony.isRightPolearm && (!ponyRace.isEquine() || model.rightArm.xRot != 0)) {
                model.rightArm.xRot += pitchChange;
                model.rightArm.yRot += yawChange;
                if (state.swingAnimation > 0 && rightHand == InteractionHand.MAIN_HAND) {
                    model.rightArm.yRot -= 0.5F;
                    model.rightArm.xRot += 1.5F;
                }
            }

            if (pony.isLeftPolearm && (!ponyRace.isEquine() || model.leftArm.xRot != 0)) {
                model.leftArm.xRot += pitchChange;
                model.leftArm.yRot -= yawChange;
                if (state.swingAnimation > 0 && leftHand == InteractionHand.MAIN_HAND) {
                    model.leftArm.yRot -= 0.5F;
                    model.leftArm.xRot += 1.5F;
                }
            }
        }

        if (pony.hasCoolShades) {
            final float bop = AnimationUtil.beat(state.ageInTicks, HEAD_NOD_DURATION, HEAD_NOD_GAP) * 3F;
            head.xRot += bop / 10F;

            float beat30 = bop / 30F;

            if (ponyRace.isEquine()) {
                float beat50 = bop / 50F;
                float beat20 = bop / 20F;

                model.leftArm.zRot -= beat50;
                model.rightArm.zRot += beat50;

                model.leftLeg.zRot -= beat30;
                model.leftLeg.xRot -= beat20;
                model.rightLeg.zRot += beat30;
                model.rightLeg.xRot += beat20;
            } else {
                model.leftArm.zRot -= beat30;
                model.rightArm.zRot += beat30;
            }
        }

        if (animation.canPlay(ponyRace.isEquine())) {
            switch (animation.animation()) {
                case NECK_SNAP: {
                    head.yRot += 3F;
                    head.xRot *= -1;
                    break;
                }
                case WOLOLO: {
                    float roll = Mth.sin(state.ageInTicks / 10F);
                    float yaw = Mth.cos(state.ageInTicks / 10F);

                    if (liftLeftArm) {
                        rotateArm(model.leftArm, 1, yaw, -roll);
                    }

                    if (liftRightArm) {
                        rotateArm(model.rightArm, 1, yaw, roll);
                    }

                    break;
                }
                case ARMS_FORWARD: {
                    float roll = (progress + 1) / 2F;

                    float pitch = 1.5F * roll;
                    float yaw = -0.5F * roll;

                    if (liftLeftArm) {
                        rotateArm(model.leftArm, pitch, -yaw, 0);
                    }

                    if (liftRightArm) {
                        rotateArm(model.rightArm, pitch, yaw, 0);
                    }

                    break;
                }
                case HANG: {
                    float saw = Mth.sin(state.walkAnimationSpeed);

                    float pitch = 0.8F * saw;

                    float basePitch = 3.25F;

                    if (context == Context.THIRD_PERSON) {

                        if (ponyRace.isEquine()) {
                            rearUp(matrices, model, 1);
                            model.head.xRot += 0.8F;
                            basePitch = 2.25F;
                        }

                        rotateArm(model.leftArm, basePitch - pitch, -0.3F, 0);
                        rotateArm(model.rightArm, basePitch + pitch, 0.3F, 0);

                        rotateArm(model.leftLeg, 0, 0.1F, 0);
                        rotateArm(model.rightLeg, 0, -0.1F, 0);
                    } else {
                        pitch *= 0.5F;

                        float x = ponyRace.isEquine() ? 9 : 10;
                        float y = ponyRace.isEquine() ? -3 : -1;
                        float z = ponyRace.isEquine() ? -8 : -6;

                        float cameraPitch = state.xRot * Mth.DEG_TO_RAD;

                        rotateArm(model.leftArm, 0, 0, -0.4F + pitch);
                        rotateArm(model.rightArm, 0, 0, 0.4F + pitch);

                        model.leftArm.x += x;
                        model.leftArm.y += y;
                        model.leftArm.z += z;
                        model.leftArm.zRot -= cameraPitch;

                        model.rightArm.x -= x;
                        model.rightArm.y += y;
                        model.rightArm.z += z;
                        model.rightArm.zRot += cameraPitch;
                    }
                    break;
                }
                case ARMS_UP: {
                    float saw = AnimationUtil.seesaw(progress);

                    float pitch = 3F * saw;
                    float yaw = 0.5F * saw;

                    if (ponyRace.isEquine()) {
                        rearUp(matrices, model, saw);
                        model.head.xRot += saw * 0.5F;
                        pitch = saw * 2F;
                    }

                    if (liftLeftArm) {
                        rotateArm(model.leftArm, pitch, -yaw, yaw);
                    } else if (ponyRace.isEquine()) {
                        model.leftArm.xRot -= saw / 4F;
                        model.leftArm.zRot -= saw / 4F;
                    }

                    if (liftRightArm) {
                        rotateArm(model.rightArm, pitch, yaw, -yaw);
                    } else if (ponyRace.isEquine()) {
                        model.rightArm.xRot += saw / 4F;
                        model.rightArm.zRot += saw / 4F;
                    }

                    break;
                }
                case CLIMB: {
                    float saw = AnimationUtil.seesaw(progress);

                    float pitch = Mth.clamp(3F * saw, 1, 2);
                    float yaw = 0.5F * saw;

                    if (context == Context.THIRD_PERSON) {
                        if (ponyRace.isEquine()) {
                            rearUp(matrices, model, 1);
                            pitch = saw * 2F;
                            model.head.xRot += saw * 0.5F;

                            rotateArm(model.leftArm, pitch, -yaw, yaw / 2F);
                            rotateArm(model.rightLeg, pitch / 2F, yaw, 0);

                            saw = AnimationUtil.seesaw(progress + 0.5F);
                            yaw = 0.5F * -saw;

                            rotateArm(model.rightArm, pitch, yaw, -yaw / 2F);
                            rotateArm(model.leftLeg, pitch / 2F, -yaw, 0);

                        } else {
                            rotateArm(model.leftArm, pitch, -yaw, yaw / 2F);
                            rotateArm(model.rightLeg, pitch / 2F, yaw, 0);

                            saw = AnimationUtil.seesaw((progress + 0.5F) % 1);

                            pitch = Mth.clamp(3F * saw, 1, 2) * (ponyRace.isEquine() ? 2 : 1);
                            yaw = 0.5F * saw;

                            rotateArm(model.rightArm, pitch, yaw, -yaw / 2F);
                            rotateArm(model.leftLeg, pitch / 2F, -yaw, 0);
                        }
                    } else {
                        //saw = MathHelper.sin(progress * 2);
                        float x = ponyRace.isEquine() ? 9 : 0;
                        float y = ponyRace.isEquine() ? -3 : 0;
                        float z = ponyRace.isEquine() ? -8 : -2;

                        float cameraPitch = state.xRot * Mth.DEG_TO_RAD;
                        pitch = Mth.sin((progress * 2) * Mth.PI) * 0.6F;

                        rotateArm(model.leftArm, 0, 0, pitch);
                        rotateArm(model.rightArm, 0, 0, pitch);

                        model.leftArm.x += x;
                        model.leftArm.y += y;
                        model.leftArm.z += z;
                        model.leftArm.zRot -= cameraPitch;

                        model.rightArm.x -= x;
                        model.rightArm.y += y;
                        model.rightArm.z += z;
                        model.rightArm.zRot += cameraPitch;
                    }

                    break;
                }
                case WAVE_ONE:
                case WAVE_TWO: {
                    progress = AnimationUtil.seesaw(progress);

                    if (animation.isOf(Animation.WAVE_TWO) && ponyRace.isEquine()) {
                        rearUp(matrices, model, progress);
                        model.head.xRot += progress * 0.5F;
                    }

                    float wave = 2.5F + progress * Mth.sin(state.ageInTicks / 3F);

                    if (animation.isOf(Animation.WAVE_TWO) || mainArm == HumanoidArm.LEFT) {
                        model.leftArm.zRot = -wave;
                    }
                    if (animation.isOf(Animation.WAVE_TWO) || mainArm == HumanoidArm.RIGHT) {
                        model.rightArm.zRot = wave;
                    }
                    break;
                }
                case KICK: {
                    if (ponyRace.isEquine()) {
                        float roll = (progress + 1) / 2F;

                        model.rightLeg.xRot = roll * 1.5F;
                        model.rightLeg.yRot = -roll / 7F;

                        model.leftLeg.xRot = roll * 1.5F;
                        model.leftLeg.yRot = roll / 7F;

                        model.leftArm.xRot = 0;
                        model.leftArm.zRot = Mth.lerp(progress, model.leftArm.zRot, 0);
                        model.rightArm.xRot = 0;
                        model.rightArm.zRot = Mth.lerp(progress, model.rightArm.zRot, 0);
                        break;
                    }

                    float roll = (progress + 1) / 2F;

                    model.rightArm.xRot += roll / 5F;
                    model.leftArm.zRot -= roll / 5F;
                    model.rightArm.zRot += roll / 5F;

                    if (state.mainArm == HumanoidArm.LEFT) {
                        model.rightLeg.xRot = -roll * 1.5F;
                        model.rightLeg.zRot = roll / 10F;
                    } else {
                        model.leftLeg.xRot = -roll * 1.5F;
                        model.leftLeg.zRot = -roll / 10F;
                    }
                    break;
                }
                case STOMP: {
                    progress = AnimationUtil.seesaw(progress);

                    if (!ponyRace.isEquine()) {
                        if (mainArm == HumanoidArm.LEFT) {
                            model.rightLeg.zRot = -progress / 9F;
                            model.rightLeg.y -= progress * 5;
                        } else {
                            model.leftLeg.zRot = -progress / 9F;
                            model.leftLeg.y -= progress * 5;
                        }
                        break;
                    }

                    rearUp(matrices, model, progress);
                    model.head.xRot += progress * 0.5F;
                    model.leftArm.xRot += progress / 2F;
                    model.rightArm.xRot += progress / 2F;
                    break;
                }
                case WIGGLE_NOSE: {
                    if (!ponyRace.isEquine()) {
                        break;
                    }

                    progress = AnimationUtil.seesaw(progress) * Mth.sin(state.ageInTicks) / 7F;

                    if (head.hasChild("mare")) head.getChild("mare").y = progress;
                    if (head.hasChild("stallion")) head.getChild("stallion").y = progress;
                    break;
                }
                default:
            }
        }

        if (pony.carriedEntity.state != null) {
            if (ponyRace.isEquine() && pony.flying) {
                model.leftLeg.xRot = 1;
                model.rightLeg.xRot = 1;
                model.leftLeg.yRot = 0.3F;
                model.rightLeg.yRot = -0.3F;
            } else {
                model.leftArm.xRot = -1;
                model.rightArm.xRot = -1;
            }
            model.leftArm.yRot = -0.3F;
            model.rightArm.yRot = 0.3F;
            model.leftArm.zRot = 0;
            model.rightArm.zRot = 0;
        }

        if (model instanceof PlayerModel m) {
            copySiblingPose(m.leftArm, m.leftSleeve);
            copySiblingPose(m.rightArm, m.rightSleeve);
            copySiblingPose(m.leftLeg, m.leftPants);
            copySiblingPose(m.rightLeg, m.rightPants);
        }
        copySiblingPose(model.head, model.hat);
    }

    private static void copySiblingPose(ModelPart parent, ModelPart overlay) {
        // Child layers already inherit the parent's transform. Copying it into
        // their local pose applies the movement twice and displaces the layer.
        if (!parent.getAllParts().contains(overlay)) {
            overlay.loadPose(parent.storePose());
        }
    }

    private void rotateArm(ModelPart arm, float pitch, float yaw, float roll) {
        arm.xRot -= pitch;
        arm.zRot = roll;
        arm.yRot = yaw;
    }

    private void rearUp(PoseStack matrices, HumanoidModel<?> model, float progress) {
        rearUpTransform(matrices, progress);

        float roll = progress / 2F;

        model.rightLeg.xRot = roll * 1.5F;
        model.rightLeg.yRot = -roll / 7F;

        model.leftLeg.xRot = roll * 1.5F;
        model.leftLeg.yRot = roll / 7F;
    }

    private static void rearUpTransform(PoseStack matrices, float progress) {
        matrices.translate(0, 0, 0.5);
        matrices.rotate(Axis.XP.rotationDegrees(-45 * progress));
        matrices.translate(0, 0, -0.5);
    }

    public record AnimationInstance(Animation animation, Animation.Recipient recipient) {
        public static final AnimationInstance NONE = new AnimationInstance(Animation.NONE, Animation.Recipient.ANYONE);
        public static final StreamCodec<ByteBuf, AnimationInstance> PACKET_CODEC = StreamCodec.composite(
                PacketCodecUtils.ofEnum(Animation.class), AnimationInstance::animation,
                PacketCodecUtils.ofEnum(Animation.Recipient.class), AnimationInstance::recipient,
                AnimationInstance::new
        );

        public boolean isOf(Animation animation) {
            return animation() == animation;
        }

        public boolean canPlay(boolean isPony) {
            return !isOf(Animation.NONE) && (recipient == Animation.Recipient.ANYONE || isPony == (recipient == Animation.Recipient.PONY));
        }

        public boolean renderBothArms() {
            return isOf(Animation.HANG) || isOf(Animation.CLIMB);
        }
    }

    public enum Animation implements CommandArgumentEnum<Animation> {
        NONE(0),
        WOLOLO(USounds.ENTITY_PLAYER_WOLOLO, 40),
        ARMS_FORWARD(5),
        ARMS_UP(5),
        WAVE_ONE(USounds.ENTITY_PLAYER_WHISTLE, 20),
        WAVE_TWO(USounds.ENTITY_PLAYER_WHISTLE, 20),
        KICK(USounds.ENTITY_PLAYER_KICK, 5),
        CLIMB(20),
        HANG(20),
        STOMP(5),
        WIGGLE_NOSE(6),
        SPREAD_WINGS(6),
        NECK_SNAP(50);

        private final int duration;
        private final Optional<SoundEvent> sound;

        Animation(int duration) {
            this.duration = duration;
            this.sound = Optional.empty();
        }

        Animation(SoundEvent sound, int duration) {
            this.duration = duration;
            this.sound = Optional.of(sound);
        }

        public int getDuration() {
            return duration;
        }

        public Optional<SoundEvent> getSound() {
            return sound;
        }

        public static StringRepresentableArgument<Animation> argument() {
            return new ArgumentType();
        }

        public static final class ArgumentType extends StringRepresentableArgument<Animation> {
            static final Codec<Animation> CODEC = Codec.withAlternative(StringRepresentable.fromEnum(Animation::values),
                    StringRepresentable.fromEnumWithMapping(Animation::values, name -> name.toLowerCase(Locale.ROOT)));

            protected ArgumentType() {
                super(CODEC, Animation::values);
            }
        }

        public enum Recipient implements CommandArgumentEnum<Recipient> {
            HUMAN,
            PONY,
            ANYONE;

            public static StringRepresentableArgument<Recipient> argument() {
                return new com.minelittlepony.unicopia.client.render.PlayerPoser.Animation.Recipient.ArgumentType();
            }

            public static final class ArgumentType extends StringRepresentableArgument<Recipient> {
                static final Codec<Recipient> CODEC = Codec.withAlternative(StringRepresentable.fromEnum(Recipient::values),
                        StringRepresentable.fromEnumWithMapping(Recipient::values, name -> name.toLowerCase(Locale.ROOT)));

                protected ArgumentType() {
                    super(CODEC, Recipient::values);
                }
            }
        }
    }

    public enum Context {
        FIRST_PERSON_LEFT,
        FIRST_PERSON_RIGHT,
        THIRD_PERSON
    }
}
