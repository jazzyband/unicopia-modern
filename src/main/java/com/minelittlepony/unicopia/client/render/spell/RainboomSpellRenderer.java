package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.ability.magic.spell.RainboomAbilitySpell;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;

public class RainboomSpellRenderer extends SpellRenderer<RainboomAbilitySpell, SpellRenderState> {
    @Override
    public SpellRenderState createRenderState() {
        return new SpellRenderState();
    }

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector vertices, SpellRenderState spell, CasterState caster,  int light) {

    }
}
