package com.minelittlepony.unicopia.item;

import java.util.List;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.component.Appearance;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.item.group.MultiItem;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.TraceHelper;
import com.minelittlepony.unicopia.util.RegistryUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.Items;

public class ZapAppleItem extends Item implements MultiItem, Appearance.AppearanceChangeCallback {
    public ZapAppleItem(Properties settings) {
        super(settings.rarity(Rarity.RARE).component(UDataComponentTypes.APPEARANCE, Appearance.DEFAULT_FULLY_DISGUISED));
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return TraceHelper.findEntity(player, 5, 1, e -> canFeedTo(stack, e))
                .map(entity -> onFedTo(stack, player, entity))
                .orElseGet(() -> super.use(world, player, hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level w, LivingEntity player) {
        stack = super.finishUsingItem(stack, w, player);

        if (w instanceof ServerLevel sw) {
            player.hurtServer(sw, Living.living(player).damageOf(UDamageTypes.ZAP_APPLE), 120);

            LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(sw, EntitySpawnReason.EVENT);
            lightning.snapTo(player.getX(), player.getY(), player.getZ());

            player.thunderHit((ServerLevel)w, lightning);

            if (player instanceof Player) {
                UCriteria.EAT_TRICK_APPLE.trigger(player);
            }
        }

        player.gameEvent(GameEvent.LIGHTNING_STRIKE);
        ParticleUtils.spawnParticle(w, LightningBoltParticleEffect.DEFAULT, player.position(), Vec3.ZERO);

        return stack;
    }

    public boolean canFeedTo(ItemStack stack, Entity e) {
        return e instanceof Villager
                || e instanceof Creeper
                || e instanceof Pig;
    }

    public InteractionResult onFedTo(ItemStack stack, Player player, Entity e) {
        LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(e.level(), EntitySpawnReason.EVENT);
        lightning.snapTo(e.getX(), e.getY(), e.getZ());
        lightning.setVisualOnly(true);
        if (player instanceof ServerPlayer) {
            lightning.setCause((ServerPlayer)player);
        }

        if (!e.level().isClientSide()) {
            e.thunderHit((ServerLevel)e.level(), lightning);
            UCriteria.FEED_TRICK_APPLE.trigger(player);
        }
        player.level().addFreshEntity(lightning);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getDefaultStacks() {
        return InteractionManager.getInstance().getClientPony().map(Pony::asWorld)
                .stream()
                .flatMap(world -> RegistryUtils.valuesForTag(world, UConventionalTags.Items.APPLES))
                .filter(a -> a != this).map(item -> {
            return Appearance.set(getDefaultInstance(), item.getDefaultInstance());
        }).toList();
    }

    @Override
    public Component getName(ItemStack stack) {
        Appearance appearance = Appearance.get(stack);
        return appearance.item().isEmpty() ? super.getName(stack) : appearance.item().getHoverName();
    }

    @Override
    public void onAppearanceSet(ItemStack stack, Appearance appearance) {
        stack.set(DataComponents.RARITY, appearance.item().isEmpty() ? stack.getPrototype().get(DataComponents.RARITY) : Rarity.EPIC);
    }
}
