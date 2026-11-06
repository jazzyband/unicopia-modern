package com.minelittlepony.unicopia.client.render.entity;

import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public class SpellbookModel extends EntityModel<SpellbookEntityRenderer.State> {

    private final BookModel book;

    public SpellbookModel(ModelPart root) {
        super(root);
        book = new BookModel(root);
        root.getChild("seam").x -= 0.01F;
    }

    public static LayerDefinition getTexturedModelData() {
        return BookModel.createBodyLayer();
    }

    public void setAngles(SpellbookEntityRenderer.State state) {
        book.resetPose();
        book.setupAnim(BookModel.State.forAnimation(state.breath, state.leftPageRot, state.rightPageRot, state.openAngle));
    }
}
