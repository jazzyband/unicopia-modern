package com.minelittlepony.unicopia.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.command.ManaCommand.ManaType;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;

public class Commands {
    public static void bootstrap() {
        ArgumentTypeRegistry.registerArgumentType(Unicopia.id("animation"), Animation.ArgumentType.class, SingletonArgumentInfo.contextFree(Animation::argument));
        ArgumentTypeRegistry.registerArgumentType(Unicopia.id("animation_recipient"), Animation.Recipient.ArgumentType.class, SingletonArgumentInfo.contextFree(Animation.Recipient::argument));
        ArgumentTypeRegistry.registerArgumentType(Unicopia.id("mana_type"), ManaType.ArgumentType.class, SingletonArgumentInfo.contextFree(ManaType::argument));
        ArgumentTypeRegistry.registerArgumentType(Unicopia.id("trait_type"), Trait.ArgumentType.class, SingletonArgumentInfo.contextFree(Trait::argument));
        ArgumentTypeRegistry.registerArgumentType(Unicopia.id("spell_traits"), TraitsArgumentType.class, SingletonArgumentInfo.contextFree(TraitsArgumentType::traits));

        CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) -> {
            dispatcher.register(EmoteCommand.create());
            dispatcher.register(SpeciesCommand.create(environment));
            dispatcher.register(UnicopiaCommand.create(registries, environment));
        });
    }
}
