package com.minelittlepony.unicopia.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;

class UnicopiaCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(CommandBuildContext registries, CommandSelection environment) {
        return Commands.literal("unicopia")
                .then(EmoteCommand.create())
                .then(ConfigCommand.create(registries))
                .then(SpeciesCommand.create(environment))
                .then(RacelistCommand.create())
                .then(WorldTribeCommand.create())
                .then(SkyAngleCommand.create())
                .then(GravityCommand.create())
                .then(DisguiseCommand.create(registries))
                .then(CastCommand.create(registries))
                .then(TraitCommand.create())
                .then(ManaCommand.create());
    }
}
