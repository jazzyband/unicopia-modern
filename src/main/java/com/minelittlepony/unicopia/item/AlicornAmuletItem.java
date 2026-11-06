package com.minelittlepony.unicopia.item;

import java.util.*;
import java.util.function.Consumer;

import com.google.common.base.Predicates;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.entity.mob.SombraEntity;
import com.minelittlepony.unicopia.entity.mob.SpellbookEntity;
import com.minelittlepony.unicopia.entity.player.*;
import com.minelittlepony.unicopia.item.component.Charges;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import com.minelittlepony.unicopia.util.VecHelper;

import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class AlicornAmuletItem extends AmuletItem implements ItemTracker.Trackable, ItemImpl.ClingyItem, TickableItem, DamageChecker {
    private static final Identifier EFFECT_ID = Unicopia.id("alicorn_amulet_modifiers");
    private static final Object2FloatMap<Holder<Attribute>> EFFECT_SCALES = Object2FloatMaps.unmodifiable(new Object2FloatOpenHashMap<>(Map.of(
            Attributes.ATTACK_DAMAGE, 0.2F,
            Attributes.ATTACK_KNOCKBACK, 0.05F,
            Attributes.ATTACK_SPEED, 0.2F,
            Attributes.ARMOR_TOUGHNESS, 0.001F,
            Attributes.ARMOR, 0.01F
    )));
    private static final Float2ObjectFunction<AttributeModifier> EFFECT_FACTORY = v -> {
        return new AttributeModifier(EFFECT_ID, v, AttributeModifier.Operation.ADD_VALUE);
    };

    public AlicornAmuletItem(Item.Properties settings) {
        super(settings, 0);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        ItemStackDuck.of(stack).getTransientComponents().getCarrier().flatMap(Pony::of).ifPresent(pony -> {
            if (pony.getArmourStacks().anyMatch(i -> i == stack)) {
                long ticks = pony.getArmour().getTicks(this);
                if (ticks > 0) {
                    textConsumer.accept(Component.literal(ItemTracker.formatTicks(ticks, context.tickRate()).formatted(ChatFormatting.GRAY)));
                }
            }
        });
    }

    @Override
    public ParticleOptions getParticleEffect(IItemEntity entity) {
        return ((ItemEntity)entity).level().getRandom().nextBoolean() ? ParticleTypes.LARGE_SMOKE : ParticleTypes.FLAME;
    }

    @Override
    public boolean isClingy(ItemStack stack) {
        return true;
    }

    @Override
    public boolean takesDamageFrom(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @Override
    public float getFollowDistance(IItemEntity entity) {
        return Math.max(20, ItemImpl.ClingyItem.super.getFollowDistance(entity));
    }

    @Override
    public float getFollowSpeed(IItemEntity entity) {
        return Math.max(0.12F, ItemImpl.ClingyItem.super.getFollowSpeed(entity));
    }

    @Override
    public void interactWithPlayer(IItemEntity item, Player player) {
        ItemEntity entity = (ItemEntity)item;

        if (!player.level().isClientSide() && !entity.isRemoved() && !player.isCreative()) {
            if (player.position().distanceTo(entity.position()) < 0.5) {
               if (entity.level().getRandom().nextInt(150) == 0) {
                   entity.setPickUpDelay(0);
                   entity.playerTouch(player);

                   if (player.getMainHandItem().is(this) && use(player.level(), player, InteractionHand.MAIN_HAND) == InteractionResult.SUCCESS) {
                       entity.setPickUpDelay(1000);
                       entity.setRemoved(RemovalReason.DISCARDED);
                   }
               }
            }
        }
    }

    @Override
    public void onEquipped(Living<?> wearer) {
        wearer.playSound(USounds.ITEM_ALICORN_AMULET_CURSE, 0.5F, 1);
    }

    @Override
    public void onUnequipped(Living<?> wearer, long timeWorn) {

        LivingEntity entity = wearer.asEntity();

        if (entity instanceof Player player && player.isCreative()) {
            return;
        }

        float attachedTime = timeWorn / ItemTracker.HOURS;

        float difficulty = wearer.isClient() ? 0 : wearer.asServerWorld().getCurrentDifficultyAt(wearer.getOrigin()).getSpecialMultiplier();
        float amount = Math.min(entity.getMaxHealth() - 1, (attachedTime / 4) * (1 + difficulty));

        if (timeWorn > ItemTracker.DAYS) {
            amount++;
        }

        if (entity instanceof Player player) {
            player.getFoodData().setFoodLevel(1);
        }
        if (!wearer.isClient()) {
            entity.hurtServer(wearer.asServerWorld(), wearer.damageOf(UDamageTypes.ALICORN_AMULET), amount);
            entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 1));
            if (timeWorn > ItemTracker.HOURS) {
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 3));
            }
        }

        if (attachedTime > ItemTracker.HOURS / 2) {
            entity.knockback(1, 1, 1, wearer.damageOf(UDamageTypes.ALICORN_AMULET), 0);
            wearer.updateVelocity();
        }

        updateAttributes(wearer, 0);
    }

    public static void updateAttributes(Living<?> wearer, float effectScale) {
        EFFECT_SCALES.object2FloatEntrySet().forEach(entry -> {
            wearer.updateAttributeModifier(EFFECT_ID, entry.getKey(), entry.getFloatValue() * effectScale, EFFECT_FACTORY, false);
        });
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot slot) {

        if (world.isClientSide()) {
            return;
        }

        // if we're in the main hand, try to equip ourselves
        if (entity instanceof Player player && slot == EquipmentSlot.MAINHAND && !isApplicable(player, this) && world.getRandom().nextInt(320) == 0) {
            use(world, player, InteractionHand.MAIN_HAND);
            return;
        }

        final Living<?> living = Living.living(entity);

        if (living == null || !living.getArmour().contains(this)) {
            return;
        }

        if (entity instanceof Player) {
            if (entity.isOnFire() && world.getBlockState(entity.blockPosition().above()).is(UBlocks.SPECTRAL_FIRE)) {
                if (UnicopiaWorldProperties.forWorld(world).isActiveAltar(entity)) {
                    if (living.asEntity().getHealth() < 2) {
                        entity.setRemainingFireTicks(0);
                        world.removeBlock(entity.blockPosition().above(), false);
                        stack.shrink(1);
                        world.explode(null, entity.getX(), entity.getY(), entity.getZ(), 0, ExplosionInteraction.NONE);
                        world.playSound(null, entity.blockPosition(), USounds.ENTITY_SOMBRA_LAUGH, SoundSource.AMBIENT, 10, 1);
                        world.getEntitiesOfClass(SpellbookEntity.class, entity.getBoundingBox().inflate(6), Predicates.alwaysTrue()).forEach(e -> e.kill(world));

                        SombraEntity.startEncounter(world, entity.blockPosition());
                    }
                    return;
                }
            }
        }

        final long attachedTicks = living.getArmour().getTicks(this);
        final long daysAttached = attachedTicks / ItemTracker.DAYS;
        final boolean fullSecond = attachedTicks % ItemTracker.SECONDS == 0;

        if (entity instanceof Player player) {
            // healing effect
            if (daysAttached <= 4) {
                if (player.getHealth() < player.getMaxHealth()) {
                    player.heal(0.5F);
                } else if (player.canEat(false)) {
                    player.getFoodData().eat(1, 0);
                }
            }

            Pony pony = (Pony)living;

            // butterfingers effects
            if (daysAttached >= 2) {
                if (pony.asWorld().getRandom().nextInt(200) == 0 && !pony.asEntity().hasEffect(UEffects.BUTTER_FINGERS)) {
                    pony.asEntity().addEffect(new MobEffectInstance(UEffects.BUTTER_FINGERS, 2100, 1));
                }

                pony.findAllEntitiesInRange(10, e -> e instanceof LivingEntity l && !l.hasEffect(UEffects.CORRUPT_INFLUENCE)).forEach(e -> {
                    ((LivingEntity)e).addEffect(new MobEffectInstance(UEffects.CORRUPT_INFLUENCE, 100, 1));
                });
            }

            // bind to the player after 3 days
            if (daysAttached >= 3 && !pony.asEntity().isCreative()) {
                TrinketsDelegate.EquippedStack amulet = getForEntity(pony.asEntity(), this);

                if (!amulet.stack().isEmpty() && !EnchantmentHelper.has(amulet.stack(), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
                    pony.playSound(USounds.ITEM_ALICORN_AMULET_HALLUCINATION, 3, 1);
                    amulet.stack().enchant(pony.entryFor(Enchantments.BINDING_CURSE), 1);
                    amulet.markChanged();
                }
            }

            MagicReserves reserves = pony.getMagicalReserves();

            // constantly increase exertion
            if (reserves.getExertion().get() < reserves.getExertion().getMax()) {
                reserves.getExertion().add(0.02F);
            }

            // gradual corruption accumulation
            if (fullSecond && world.getRandom().nextInt(12) == 0 && !pony.asEntity().isCreative()) {
                reserves.getEnergy().add(10);
                pony.getCorruption().add((int)Mth.clamp(attachedTicks / ItemTracker.HOURS, 1, pony.getCorruption().getMax()));
            }

            // ambient effects
            if (attachedTicks % ItemTracker.HOURS < 90 && world.getRandom().nextInt(900) == 0) {
                pony.playSound(USounds.ITEM_ALICORN_AMULET_HALLUCINATION, 3, 1);
            } else if (attachedTicks < 2 || (attachedTicks % (10 * ItemTracker.SECONDS) < 9 && world.getRandom().nextInt(90) == 0)) {
                if (attachedTicks % 5 == 0) {
                    InteractionManager.getInstance().playLoopingSound(player, InteractionManager.SOUND_HEART_BEAT, 0);
                }

                reserves.getExertion().addPercent(10);
                reserves.getEnergy().add(10);
                living.asEntity().removeEffect(MobEffects.WEAKNESS);
                living.asEntity().removeEffect(MobEffects.NAUSEA);
            }

            // damage effect after 4 days
            if (daysAttached >= 4) {
                if (attachedTicks % 100 == 0) {
                    player.getFoodData().addExhaustion(90F);
                    float healthDrop = Mth.clamp(player.getMaxHealth() - player.getHealth(), 2, 5);
                    player.hurtServer(world, pony.damageOf(UDamageTypes.ALICORN_AMULET), healthDrop);
                }

                return;
            }
        }

        // every 1 second, update modifiers
        if (fullSecond) {
            updateAttributes(living, (float)attachedTicks / ItemTracker.SECONDS);
        }
    }

    @Override
    public InteractionResult onGroundTick(IItemEntity item) {
        ItemEntity entity = (ItemEntity)item;

        if (entity.level().getRandom().nextInt(500) == 0) {
            entity.level().playSound(null, entity.blockPosition(), USounds.ITEM_ALICORN_AMULET_AMBIENT, SoundSource.HOSTILE, 0.5F, 1);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void inFrameTick(ItemFrame entity) {
        RandomSource rng = entity.level().getRandom();

        if (rng.nextInt(1500) == 0) {
            entity.level().playSound(null, entity.blockPosition(), USounds.ITEM_ALICORN_AMULET_AMBIENT, SoundSource.HOSTILE, 0.5F, 1);
            for (int i = 0; i < 5; i++) {
                entity.level().addParticle(rng.nextBoolean() ? ParticleTypes.LARGE_SMOKE : ParticleTypes.FLAME,
                        rng.triangle(entity.getX(), 0.5),
                        rng.triangle(entity.getY(), 0.5),
                        rng.triangle(entity.getZ(), 0.5),
                        0, 0, 0
                );
            }
        }

        if ((entity.tickCount / 1000) % 10 == 0 && entity.tickCount % 50 == 0) {
            for (Entity target : VecHelper.findInRange(entity, entity.level(), entity.position(), 10, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
                if (target instanceof LivingEntity l) {
                    for (EquipmentSlot slot : EquipmentSlot.VALUES) {
                        ItemStack equipment = l.getItemBySlot(slot);
                        if (equipment.is(UItems.GROGARS_BELL)) {
                            if (Charges.discharge(equipment, 3)) {
                                ParticleUtils.spawnParticle(entity.level(),
                                        new FollowingParticleEffect(UParticles.HEALTH_DRAIN, entity, 0.4F)
                                        .withChild(ParticleTypes.COMPOSTER), target.getEyePosition(), Vec3.ZERO);
                            }
                        }
                    }
                }
            }
        }
    }
}
