package com.minelittlepony.unicopia.client.render;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public class BatWingsFeatureRenderer<S extends HumanoidRenderState, E extends LivingEntity> extends WingsFeatureRenderer<S> {

    private static final Identifier TEXTURE = Unicopia.id("textures/models/wings/bat.png");

    public BatWingsFeatureRenderer(RenderLayerParent<S, ? extends HumanoidModel<S>> context) {
        super(context);
    }

    @Override
    protected void createWing(String name, PartDefinition parent, CubeDeformation dilation, int k) {
        PartDefinition base = parent.addOrReplaceChild(name,
                CubeListBuilder.create().addBox(0, 0, 0, 2, 10, 2, dilation),
                PartPose.offset(k * 2, 2, 2 + k * 0.5F));

        for (int i = 0; i < FEATHER_COUNT; i++) {
            int texX = (i % 2) * 8;
            int baseLength = 23;
            int featherLength = i < 7 ? baseLength - (i % 4 * 2) : baseLength - (i * 2);
            PartDefinition wing = base.addOrReplaceChild("feather_" + i,
                    CubeListBuilder.create()
                        .texOffs(8 + texX, 0)
                        .addBox(-k * (i % 2) / 90F, 0, 0, 0.02F, featherLength * 0.8F, 4, dilation),
                    PartPose.offset(-i * k / 9F, 7, 0)
            );

            wing.addOrReplaceChild("secondary", CubeListBuilder.create()
                    .texOffs(8 + texX, 0)
                    .addBox(-k * (i % 2) / 90F, 0, 0, 0.02F, featherLength, 4, dilation),
                PartPose.rotation(0.2F, 0, 0)
            );
            if (i < 5) {
                wing.addOrReplaceChild("tertiary", CubeListBuilder.create()
                        .texOffs(8 + texX, 0)
                        .addBox(-k * (i % 2) / 90F, 0, 0, 0.02F, featherLength - 1, 4, dilation),
                    PartPose.rotation(-0.2F, 0, 0)
                );
            }
        }
    }

    @Override
    protected boolean canRender(S state) {
        return state instanceof net.minecraft.client.renderer.entity.state.AvatarRenderState
                && CasterState.of(state).species.physical() == Race.BAT
                && CasterState.of(state).skinFeatures.showWings()
                && !CasterState.of(state).pegasusAmulet;
    }

    @Override
    protected Identifier getTexture(S state) {
        return TEXTURE;
    }

    @Override
    @Nullable
    protected Identifier getOverlayTexture(S state) {
        return null;
    }
}
