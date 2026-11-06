package com.minelittlepony.unicopia.client.gui;

import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.StonecutterMenu;

public class ShapingBenchScreen extends StonecutterScreen {
    public ShapingBenchScreen(StonecutterMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }
}
