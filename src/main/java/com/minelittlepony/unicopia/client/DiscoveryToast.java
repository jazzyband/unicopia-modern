package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.item.UItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;
import org.spongepowered.include.com.google.common.base.Objects;

public class DiscoveryToast implements Toast {
    private static final long MAX_AGE = 5000L;
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("toast/advancement");
    private static final Component TITLE = Component.translatable("unicopia.toast.discoveries.title");
    private static final Component DESCRIPTION = Component.translatable("unicopia.toast.discoveries.description");

    private final List<Identifier> discoveries = new ArrayList<>();
    private long startTime;
    private long age;

    private boolean justUpdated;


    @Override
    public Visibility getWantedVisibility() {
        return discoveries.isEmpty() || age >= MAX_AGE ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (justUpdated) {
            this.startTime = time;
            justUpdated = false;
        }
        age = startTime - this.startTime;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, Font textRenderer, long startTime) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, width(), height());
        context.text(textRenderer, TITLE, 30, 7, -11534256, false);
        context.text(textRenderer, DESCRIPTION, 30, 18, -16777216, false);

        Identifier icon = discoveries.get((int)(startTime / Math.max(1L, MAX_AGE / discoveries.size()) % discoveries.size()));

        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.scale(0.6F, 0.6F);
        context.item(UItems.SPELLBOOK.getDefaultInstance(), 3, 3);
        matrices.popMatrix();

        context.blit(icon, 8, 8, 16, 16, 0, 0, 1, 1);
    }

    public DiscoveryToast addDiscoveries(Identifier icon) {
        if (!discoveries.contains(icon)) {
            discoveries.add(icon);
        }
        justUpdated = true;

        return this;
    }

    class Discovery {
        final Identifier icon;
        final Component description;

        Discovery(Identifier icon, Component description) {
            this.icon = icon;
            this.description = description;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Discovery
                    && Objects.equal(icon, ((Discovery) o).icon)
                    && Objects.equal(description, ((Discovery) o).description);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(icon, description);
        }
    }

    public static void show(ToastManager manager, Identifier icon) {
        DiscoveryToast existing = manager.getToast(DiscoveryToast.class, NO_TOKEN);
        if (existing == null) {
            manager.addToast(new DiscoveryToast().addDiscoveries(icon));
        } else {
            existing.addDiscoveries(icon);
        }
    }
}

