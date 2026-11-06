package com.minelittlepony.unicopia.network.handler;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.data.Rot;
import com.minelittlepony.unicopia.ability.data.tree.TreeTypes;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.ClientBlockDestructionManager;
import com.minelittlepony.unicopia.client.DiscoveryToast;
import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.minelittlepony.unicopia.client.gui.TribeSelectionScreen;
import com.minelittlepony.unicopia.client.gui.spellbook.ClientChapters;
import com.minelittlepony.unicopia.diet.PonyDiets;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.TotemOfDying;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.network.*;
import com.minelittlepony.unicopia.network.MsgCasterLookRequest.Reply;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ClientNetworkHandlerImpl {
    private final Minecraft client = Minecraft.getInstance();

    public ClientNetworkHandlerImpl() {
        Channel.SERVER_SELECT_TRIBE.receiver().addPersistentListener(this::handleTribeScreen);
        Channel.SERVER_BLOCK_DESTRUCTION.receiver().addPersistentListener(this::handleBlockDestruction);
        Channel.CANCEL_PLAYER_ABILITY.receiver().addPersistentListener(this::handleCancelAbility);
        Channel.UNLOCK_TRAITS.receiver().addPersistentListener(this::handleUnlockTraits);
        Channel.SERVER_RESOURCES.receiver().addPersistentListener(this::handleServerResources);
        Channel.SERVER_SKY_ANGLE.receiver().addPersistentListener(this::handleSkyAngle);
        Channel.SERVER_ZAP_STAGE.receiver().addPersistentListener(this::handleZapStage);
        Channel.SERVER_PLAYER_ANIMATION_CHANGE.receiver().addPersistentListener(this::handlePlayerAnimation);
        Channel.SERVER_REQUEST_PLAYER_LOOK.receiver().addPersistentListener(this::handleCasterLookRequest);
        Channel.SERVER_TRINKET_BROKEN.receiver().addPersistentListener(this::handleTrinketBroken);
        Channel.CONFIGURATION_CHANGE.receiver().addPersistentListener(this::handleConfigurationChange);
        Channel.ENTITY_STATUS.receiver().addPersistentListener(this::handleEntityStatus);
    }

    private void handleTribeScreen(Player sender, MsgTribeSelect packet) {
        client.gui.setScreen(new TribeSelectionScreen(packet.availableRaces(), packet.serverMessage()));
    }

    private void handleBlockDestruction(Player sender, MsgBlockDestruction packet) {
        ClientBlockDestructionManager destr = ((ClientBlockDestructionManager.Source)client.levelRenderer).getDestructionManager();

        packet.destructions().forEach((i, d) -> {
            destr.setBlockDestruction(i, d);
        });
    }

    private void handleEntityStatus(Player sender, MsgEntityStatus status) {
        Living.getOrEmpty(sender.level().getEntity(status.entityId())).ifPresent(living -> {
            if (status.status() == MsgEntityStatus.USE_TOTEM_OF_DYING) {
                client.particleEngine.createTrackingEmitter(living.asEntity(), ParticleTypes.TOTEM_OF_UNDYING, 30);
                living.asWorld().playLocalSound(living.asEntity().getX(), living.asEntity().getY(), living.asEntity().getZ(), SoundEvents.TOTEM_USE, living.asEntity().getSoundSource(), 1, 1, false);
                if (living.asEntity() == client.player) {
                    ItemStack totem = TotemOfDying.getTotem(client.player, false);
                    client.player.displayItemActivation(totem.isEmpty() ? UItems.TOTEM_OF_DYING.getDefaultInstance() : totem);
                }
            }
        });
    }

    private void handleCancelAbility(Player sender, MsgCancelPlayerAbility packet) {
        client.player.playSound(USounds.GUI_ABILITY_FAIL, 1, 1);
        Pony.of(client.player).getAbilities().getStats().forEach(s -> s.setCooldown(0));
    }

    private void handleUnlockTraits(Player sender, MsgUnlockTraits packet) {
        for (Trait trait : packet.traits()) {
            DiscoveryToast.show(client.gui.toastManager(), trait.getSprite());
        }
    }

    private void handleSkyAngle(Player sender, MsgSkyAngle packet) {
        UnicopiaClient.getInstance().tangentalSkyAngle.update(packet.tangentalSkyAngle(), 200);
    }

    private void handleZapStage(Player sender, MsgZapAppleStage packet) {
        UnicopiaClient.getInstance().setZapAppleStage(packet.stage());
    }

    private void handleServerResources(Player sender, MsgServerResources packet) {
        SpellTraits.load(packet.traits());
        ClientChapters.load(packet.chapters());
        TreeTypes.load(packet.treeTypes());
        PonyDiets.load(packet.diets());
        InteractionManager.getInstance().setCloudShapingRecipes(packet.cloudCuttingRecipes());
        Pony.of(sender).setSkinFeatures(Unicopia.getConfig().skinFeatures.get());
    }

    private void handlePlayerAnimation(Player sender, MsgPlayerAnimationChange packet) {
        Pony player = Pony.of(Minecraft.getInstance().level.getPlayerByUUID(packet.playerId()));
        if (player == null) {
            return;
        }

        player.setAnimation(packet.animation(), packet.duration());
    }

    private void handleCasterLookRequest(Player sender, MsgCasterLookRequest packet) {
        Pony player = Pony.of(Minecraft.getInstance().player);
        if (player == null) {
            return;
        }

        Channel.CLIENT_CASTER_LOOK.sendToServer(new Reply(packet.spellId(), Rot.of(player)));
    }

    private void handleTrinketBroken(Player player, MsgTrinketBroken packet) {
        if (player.level().getEntity(packet.entityId()) instanceof LivingEntity sender) {
            ItemStack stack = packet.stack();
            if (!stack.isEmpty()) {
                if (!sender.isSilent()) {
                    sender.level().playLocalSound(
                            sender.getX(), sender.getY(), sender.getZ(),
                            SoundEvents.ITEM_BREAK.value(), sender.getSoundSource(),
                            0.8F,
                            0.8F + sender.level().getRandom().nextFloat() * 0.4F,
                            false
                    );
                }

                int count = 5;

                for (int i = 0; i < count; ++i) {
                    Vec3 vec3d = new Vec3((sender.level().getRandom().nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
                    vec3d = vec3d.xRot(-sender.getXRot() * (float) (Math.PI / 180.0));
                    vec3d = vec3d.yRot(-sender.getYRot() * (float) (Math.PI / 180.0));
                    double d = (-sender.level().getRandom().nextFloat()) * 0.6 - 0.3;
                    Vec3 vec3d2 = new Vec3((sender.level().getRandom().nextFloat() - 0.5) * 0.3, d, 0.6);
                    vec3d2 = vec3d2.xRot(-sender.getXRot() * (float) (Math.PI / 180.0));
                    vec3d2 = vec3d2.yRot(-sender.getYRot() * (float) (Math.PI / 180.0));
                    vec3d2 = vec3d2.add(sender.getX(), sender.getEyeY(), sender.getZ());
                    sender.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, net.minecraft.world.item.ItemStackTemplate.fromStack(stack)), vec3d2.x, vec3d2.y, vec3d2.z, vec3d.x, vec3d.y + 0.05, vec3d.z);
                }
            }
        }
    }

    private void handleConfigurationChange(Player sender, MsgConfigurationChange packet) {
        InteractionManager.getInstance().setSyncedConfig(packet.config());
    }
}
