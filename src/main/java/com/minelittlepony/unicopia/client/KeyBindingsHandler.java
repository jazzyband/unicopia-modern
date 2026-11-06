package com.minelittlepony.unicopia.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.mojang.blaze3d.platform.InputConstants;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.ability.ActivationType;
import com.minelittlepony.unicopia.client.gui.UHud;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class KeyBindingsHandler {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Unicopia.id("category"));

    public static final KeyBindingsHandler INSTANCE = new KeyBindingsHandler();

    static void bootstrap() {}

    private final Map<Binding, AbilitySlot> keys = new HashMap<>();
    private final Map<AbilitySlot, Binding> reverse = new HashMap<>();

    private final Binding pageDown = new Binding(create(InputConstants.KEY_PAGEDOWN, "hud_page_dn"));
    private final Binding pageUp = new Binding(create(InputConstants.KEY_PAGEUP, "hud_page_up"));

    private final KeyMapping singleTapModifier = createSticky(InputConstants.UNKNOWN.getValue(), "ability_modifier_tap");
    private final KeyMapping doubleTapModifier = createSticky(InputConstants.UNKNOWN.getValue(), "ability_modifier_double_tap");
    private final KeyMapping tripleTapModifier = createSticky(InputConstants.UNKNOWN.getValue(), "ability_modifier_triple_tap");

    private final Set<KeyMapping> pressed = new HashSet<>();

    public KeyBindingsHandler() {
        addKeybind(InputConstants.KEY_R, AbilitySlot.PRIMARY);
        addKeybind(InputConstants.KEY_G, AbilitySlot.SECONDARY);
        addKeybind(InputConstants.KEY_V, AbilitySlot.TERTIARY);
    }

    public Binding getBinding(AbilitySlot slot) {
        return reverse.get(slot);
    }

    public boolean isToggleMode() {
        return Unicopia.getConfig().toggleAbilityKeys.get();
    }

    public ActivationType getForcedActivationType() {
        if (singleTapModifier.isDown()) {
            return ActivationType.TAP;
        }

        if (doubleTapModifier.isDown()) {
            return ActivationType.DOUBLE_TAP;
        }

        if (tripleTapModifier.isDown()) {
            return ActivationType.TRIPLE_TAP;
        }

        return ActivationType.NONE;
    }

    public void addKeybind(int code, AbilitySlot slot) {
        Binding binding = new Binding(createSticky(code, slot.name().toLowerCase(Locale.ROOT)));
        reverse.put(slot, binding);
        keys.put(binding, slot);
    }

    KeyMapping create(int code, String name) {
        return new KeyMapping("key.unicopia." + name, code, KEY_CATEGORY);
    }

    KeyMapping createSticky(int code, String name) {
        return new ToggleKeyMapping("key.unicopia." + name, code, KEY_CATEGORY, this::isToggleMode, false);
    }

    public void tick(Minecraft client) {
        if (client.gui.screen() != null
            || client.player == null) {
            return;
        }
        Pony iplayer = Pony.of(client.player);
        AbilityDispatcher abilities = iplayer.getAbilities();
        int maxPage = abilities.getMaxPage();

        int page = Mth.clamp(Unicopia.getConfig().hudPage.get(), 0, maxPage);

        if (page > 0 && pageDown.getState() == PressedState.PRESSED) {
            changePage(client, maxPage, -1);
        } else if (page < maxPage && pageUp.getState() == PressedState.PRESSED) {
            changePage(client, maxPage, 1);
        } else if (!client.player.isSpectator()) {
            for (Binding i : keys.keySet()) {
                AbilitySlot slot = keys.get(i);
                if (slot == AbilitySlot.PRIMARY && client.options.keyShift.isDown() && abilities.isFilled(AbilitySlot.PASSIVE)) {
                    slot = AbilitySlot.PASSIVE;
                }
                if (slot == AbilitySlot.PRIMARY && !abilities.isFilled(slot)) {
                    slot = AbilitySlot.PASSIVE;
                }

                PressedState state = i.getState();

                if (state != PressedState.UNCHANGED) {
                    if (state == PressedState.PRESSED) {
                        abilities.activate(slot, page).map(a -> a.getName(iplayer)).ifPresent(UHud.INSTANCE::setMessage);
                    } else {
                        abilities.clear(slot, ActivationType.NONE, page);
                    }
                } else {
                    ActivationType type = i.getType();
                    if (type.isResult()) {
                        abilities.clear(slot, type, page);
                    }
                }
            }
        }
    }

    private void changePage(Minecraft client, long max, int sigma) {
        int page = Unicopia.getConfig().hudPage.get();
        page += sigma;
        Unicopia.getConfig().hudPage.set(page);
        Unicopia.getConfig().save();
        client.getSoundManager().play(SimpleSoundInstance.forUI(USounds.Vanilla.UI_BUTTON_CLICK, 1.75F + (0.25F * sigma)));
        UHud.INSTANCE.setMessage(Component.translatable("gui.unicopia.page_num", page + 1, max + 1));
    }

    public class Binding {
        private final KeyMapping binding;

        private long nextPhaseTime;

        private final AtomicReference<ActivationType> type = new AtomicReference<>(ActivationType.NONE);

        Binding(KeyMapping binding) {
            this.binding = binding;
        }

        public Component getLabel() {
            return binding.getTranslatedKeyMessage();
        }

        public PressedState getState() {
            PressedState state = getNewState();

            long now = System.currentTimeMillis();

            if (state == PressedState.PRESSED) {
                nextPhaseTime = now + 200;
            }

            if (state == PressedState.RELEASED && now < nextPhaseTime + 10) {
                nextPhaseTime = now + 200;
                type.set(type.get().getNext());
            }

            return state;
        }

        public ActivationType getType() {
            if (binding.isDown()) {
                ActivationType t = getForcedActivationType();
                if (t.isResult()) {
                    KeyMapping.resetToggleKeys();
                    return t;
                }
            }

            if (!isToggleMode() && System.currentTimeMillis() > nextPhaseTime - 70) {
                return type.getAndSet(ActivationType.NONE);
            }

            return ActivationType.NONE;
        }

        private PressedState getNewState() {
            if (binding.isDown()) {
                return pressed.add(binding) ? PressedState.PRESSED : PressedState.UNCHANGED;
            } else if (pressed.remove(binding)) {
                return PressedState.RELEASED;
            }

            return PressedState.UNCHANGED;
        }
    }

    enum PressedState {
        UNCHANGED,
        PRESSED,
        RELEASED
    }
}
