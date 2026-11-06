package com.minelittlepony.unicopia.client.gui;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.element.*;
import com.minelittlepony.common.client.gui.style.Style;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.entity.player.SkinFeatures;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import com.minelittlepony.unicopia.util.RegistryIndexer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class SettingsScreen extends GameGui {
    private final Config config = Unicopia.getConfig();

    private final ScrollContainer content = new ScrollContainer();

    @Nullable
    private Style mineLpStatus;

    private long drawingHudOutline;

    public SettingsScreen(Screen parent) {
        super(Component.translatable("unicopia.options.title"), parent);

        content.margin.setVertical(30);
        content.getContentPadding().setHorizontal(10);
        content.getContentPadding().top = 10;
        content.getContentPadding().bottom = 20;
    }

    @Override
    public void init() {
        content.init(this::rebuildContent);
    }

    private void rebuildContent() {

        int LEFT = content.width / 2 - 100;

        getChildElements().add(content);

        int row = 0;

        addButton(new Label(width / 2, 5).setCentered()).getStyle().setText(getTitle().getString());
        addButton(new Button(width / 2 - 100, height - 25))
            .onClick(sender -> finish())
            .getStyle()
                .setText("gui.done");

        content.addButton(new Label(LEFT, row)).getStyle().setText("unicopia.options.client");

        content.addButton(new Toggle(LEFT, row += 20, config.ignoreMineLP.get()))
                .onChange(v -> {
                    config.ignoreMineLP.set(v);
                    if (mineLpStatus != null) {
                        mineLpStatus.setText(getMineLPStatus());
                    }
                    return v;
                })
                .getStyle().setText("unicopia.options.ignore_mine_lp");

        row += 10;

        mineLpStatus = content.addButton(new Label(LEFT, row += 10)).getStyle().setText(getMineLPStatus());

        content.addButton(new Toggle(LEFT, row += 20, config.toggleAbilityKeys.get()))
            .onChange(config.toggleAbilityKeys)
            .getStyle().setText("unicopia.options.toggle_ability_keys");

        content.addButton(new EnumSlider<>(LEFT, row += 20, config.hudPosition))
            .onChange(v -> {
                drawingHudOutline = System.currentTimeMillis() + 1000L;
                return config.hudPosition.set(v);
            })
            .setTextFormat(v -> Component.translatable("unicopia.options.hud_position", v.getValue().name()));

        RegistryIndexer<Race> races = RegistryIndexer.of(Race.REGISTRY);

        content.addButton(new Slider(LEFT, row += 25, 0, races.size(), races.indexOf(config.preferredRace.get())))
                .onChange(races.createSetter(config.preferredRace))
                .setTextFormat(v -> Component.translatable("unicopia.options.preferred_race", races.valueOf(v.getValue()).getDisplayName()));

        content.addButton(new Label(LEFT, row += 20)).getStyle().setText("unicopia.options.skin_features");

        content.addButton(new Toggle(LEFT, row += 20, config.skinFeatures.get().showHorn()))
            .onChange(horn -> {
                var features = config.skinFeatures.get();
                config.skinFeatures.set(new SkinFeatures(horn, features.showWings(), features.skinColor()));
                return horn;
            })
            .getStyle().setText("unicopia.options.skin_features.horn");
        content.addButton(new Toggle(LEFT, row += 20, config.skinFeatures.get().showWings()))
            .onChange(wings -> {
                var features = config.skinFeatures.get();
                config.skinFeatures.set(new SkinFeatures(features.showHorn(), wings, features.skinColor()));
                return wings;
            })
            .getStyle().setText("unicopia.options.skin_features.wings");

        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server != null) {
            row += 20;
            content.addButton(new Label(LEFT, row)).getStyle().setText("unicopia.options.world");

            UnicopiaWorldProperties tribes = UnicopiaWorldProperties.forWorld((ServerLevel)server.getPlayerList().getPlayer(Minecraft.getInstance().player.getUUID()).level());

            content.addButton(new Slider(LEFT, row += 20, 0, races.size(), races.indexOf(tribes.getDefaultRace())))
                    .onChange(races.createSetter(tribes::setDefaultRace))
                    .setTextFormat(v -> Component.translatable("unicopia.options.world.default_race", races.valueOf(v.getValue()).getDisplayName()))
                    .setEnabled(minecraft.isLocalServer());
        }
    }

    private Component getMineLPStatus() {
        final boolean hasMineLP = FabricLoader.getInstance().isModLoaded("minelp");

        if (hasMineLP) {
            if (config.ignoreMineLP.get()) {
                return Component.translatable("unicopia.options.ignore_mine_lp.undetected").withStyle(ChatFormatting.DARK_GREEN);
            }

            return Component.translatable("unicopia.options.ignore_mine_lp.detected", MineLPDelegate.getInstance().getPlayerPonyRace().getDisplayName()).withStyle(ChatFormatting.GREEN);
        }

        return Component.translatable("unicopia.options.ignore_mine_lp.missing").withStyle(ChatFormatting.RED);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float tickDelta) {
        super.extractRenderState(context, mouseX, mouseY, tickDelta);
        content.extractRenderState(context, mouseX, mouseY, tickDelta);
        if (drawingHudOutline > System.currentTimeMillis()) {

            int scaledWidth = minecraft.getWindow().getGuiScaledWidth();
            int scaledHeight = minecraft.getWindow().getGuiScaledHeight();

            HudPosition selectedPos = config.hudPosition.get();

            for (var hudPos : HudPosition.values()) {
                HudPosition.Alignment armAlignment = minecraft.options.mainHand().get() == HumanoidArm.LEFT ? HudPosition.Alignment.START : HudPosition.Alignment.END;
                if (hudPos == HudPosition.OFF_HAND) {
                    armAlignment = armAlignment.opposite();
                }

                int left = hudPos.getHorizontal().pick(2, scaledWidth - 50, ((scaledWidth - 50) / 2) + (109 * armAlignment.getSignum()));
                int top = hudPos.getVertical().pick(12, scaledHeight - 50, scaledHeight - 50);
                if (hudPos == HudPosition.BOTTOM_CENTER) {
                    top -= 22;
                }

                context.pose().pushMatrix();
                context.pose().translate(left + UHud.PRIMARY_SLOT_SIZE / 2, top + UHud.PRIMARY_SLOT_SIZE / 2);
                DrawableUtil.drawArc(context, 0, UHud.PRIMARY_SLOT_SIZE / 2, 0, Mth.TWO_PI, hudPos == selectedPos ? 0xEEFF00A5 : 0xEEFFFF25);
                context.pose().popMatrix();
            }
        }
    }

    @Override
    public void removed() {
        config.save();
    }
}
