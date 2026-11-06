package com.minelittlepony.unicopia.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.component.Charges;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.item.group.MultiItem;

public class EnchantedStaffItem extends StaffItem implements MultiItem, Charges.ChargeChangeCallback {
    private static final Map<EntityType<?>, SpellType<?>> ENTITY_TYPE_TO_SPELL = new HashMap<>();

    public static <T extends Spell> SpellType<T> register(EntityType<?> entityType, SpellType<T> spellType) {
        ENTITY_TYPE_TO_SPELL.put(entityType, spellType);
        return spellType;
    }

    public static SpellType<?> getSpellType(Entity entity, boolean remove) {
        if (entity instanceof CastSpellEntity cast) {
            return cast.getSpellSlot().get(SpellType.PLACE_CONTROL_SPELL.negate())
                    .map(Spell::getTypeAndTraits)
                    .map(CustomisedSpellType::type)
                    .orElse(SpellType.empty());
        }
        if (entity instanceof Player player) {
            if (remove) {
                return Pony.of(player).getCharms().equipSpell(InteractionHand.MAIN_HAND, SpellType.EMPTY_KEY.withTraits()).type();
            }
            return Pony.of(player).getCharms().getEquippedSpell(InteractionHand.MAIN_HAND).type();
        }
        return ENTITY_TYPE_TO_SPELL.getOrDefault(entity.getType(), SpellType.empty());
    }

    static {
        register(EntityTypes.DROWNED, SpellType.BUBBLE);
        register(EntityTypes.DOLPHIN, SpellType.BUBBLE);
        register(EntityTypes.BLAZE, SpellType.FIRE_BOLT);
        register(EntityTypes.CHICKEN, SpellType.FEATHER_FALL);
        register(EntityTypes.CREEPER, SpellType.CATAPULT);
        register(EntityTypes.HUSK, SpellType.HYDROPHOBIC);
        register(EntityTypes.SNOW_GOLEM, SpellType.FROST);
        register(EntityTypes.POLAR_BEAR, SpellType.FROST);
        register(EntityTypes.FIREBALL, SpellType.FLAME);
        register(EntityTypes.SMALL_FIREBALL, SpellType.FLAME);
        register(EntityTypes.ENDER_DRAGON, SpellType.DISPLACEMENT);
        register(EntityTypes.GUARDIAN, SpellType.AWKWARD);
        register(EntityTypes.ELDER_GUARDIAN, SpellType.AWKWARD);
        register(EntityTypes.DRAGON_FIREBALL, SpellType.INFERNAL);
        register(EntityTypes.CAVE_SPIDER, SpellType.REVEALING);
        register(EntityTypes.ZOMBIE, SpellType.NECROMANCY);
        register(EntityTypes.VEX, SpellType.NECROMANCY);
        register(EntityTypes.SKELETON, SpellType.CATAPULT);
        register(EntityTypes.WITHER_SKELETON, SpellType.CATAPULT);
        register(EntityTypes.SKELETON_HORSE, SpellType.CATAPULT);
        register(UEntities.TWITTERMITE, SpellType.LIGHT);
    }

    public EnchantedStaffItem(Properties settings) {
        super(settings.component(UDataComponentTypes.CHARGES, new Charges(3, 3, 3, true)).durability(500));
    }

    @Override
    public List<ItemStack> getDefaultStacks() {
        return Stream.concat(
                Stream.of(getDefaultInstance()),
                ENTITY_TYPE_TO_SPELL.values().stream().distinct().map(type -> EnchantableItem.enchant(getDefaultInstance(), type))
            ).toList();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        if (EnchantableItem.isEnchanted(stack)) {
            SpellType<?> key = EnchantableItem.getSpellKey(stack);
            textConsumer.accept(Component.translatable(key.getTranslationKey()).withStyle(key.getAffinity().getColor()));
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (EnchantableItem.isEnchanted(stack)) {
            return InteractionResult.PASS;
        }

        super.interactLivingEntity(stack, player, target, hand);

        boolean remove = EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target);
        SpellType<?> type = getSpellType(target, remove);
        if (!type.isEmpty()) {
            if (remove) {
                target.setHealth(1);
                target.setTicksFrozen(9000);
            }
            stack = EnchantableItem.enchant(stack, type);
            Charges.recharge(stack);
            player.setItemInHand(hand, stack);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level world, LivingEntity entity, int timeLeft) {
        int i = getUseDuration(stack, entity) - timeLeft;

        if (i > 5 && EnchantableItem.isEnchanted(stack)) {
            CustomisedSpellType<?> spellType = EnchantableItem.getSpellEffect(stack);
            if (Charges.discharge(stack, 1)) {
                Pony.of(entity).ifPresent(pony -> {
                    spellType.apply(pony, CastingMethod.STAFF);
                    pony.setAnimation(Animation.ARMS_UP, Animation.Recipient.ANYONE, 10);
                    stack.hurtAndBreak(1, pony.asEntity(), EquipmentSlot.MAINHAND);
                    pony.subtractEnergyCost(4);
                });
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean castContainedEffect(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.isShiftKeyDown()) {
            CustomisedSpellType<?> spellType = EnchantableItem.getSpellEffect(stack);
            if (Charges.discharge(stack, 1)) {
                stack.hurtAndBreak(50, attacker, EquipmentSlot.MAINHAND);
                Caster.of(attacker).ifPresent(c -> c.subtractEnergyCost(4));
                Caster.of(target).ifPresent(c -> spellType.apply(c, CastingMethod.STAFF));
            }
        }

        return false;
    }

    @Override
    public void onUseTick(Level world, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (entity instanceof LivingEntity) {
            LivingEntity living = entity;

            if (living.getUseItem().getItem() == this) {
                Vec3 eyes = entity.getEyePosition(1);

                float i = getUseDuration(stack, entity) - ticksRemaining;

                world.addParticle(i > 150 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CLOUD, eyes.x, eyes.y, eyes.z,
                        (world.getRandom().nextGaussian() - 0.5) / 10,
                        (world.getRandom().nextGaussian() - 0.5) / 10,
                        (world.getRandom().nextGaussian() - 0.5) / 10
                );
                world.playSound(null, entity.blockPosition(), USounds.ITEM_MAGIC_STAFF_CHARGE, SoundSource.PLAYERS, 1, i / 20);

                if (i > 200 && world instanceof ServerLevel sw) {
                    living.stopUsingItem();
                    living.hurtServer(sw, entity.damageSources().magic(), 1);
                    if (EnchantableItem.isEnchanted(stack)) {
                        CustomisedSpellType<?> spellType = EnchantableItem.getSpellEffect(stack);
                        if (Charges.discharge(stack, 1)) {
                            Caster.of(entity).ifPresent(c -> spellType.apply(c, CastingMethod.STAFF));
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (EnchantableItem.isEnchanted(stack) && Charges.of(stack).energy() > 0) {
            return Component.translatable(getDescriptionId() + ".enchanted", super.getName(stack), EnchantableItem.getSpellKey(stack).getName());
        }
        return super.getName(stack);
    }

    @Override
    public void onDischarge(ItemStack stack) {
        if (Charges.of(stack).energy() == 0) {
            EnchantableItem.unenchant(stack);
        }
    }
}
