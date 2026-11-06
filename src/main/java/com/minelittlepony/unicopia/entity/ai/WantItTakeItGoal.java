package com.minelittlepony.unicopia.entity.ai;

import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.VecHelper;

public class WantItTakeItGoal extends BreakHeartGoal {

    private final Predicate<LivingEntity> entityPredicate = EquinePredicates.LIVING_HAS_WANT_IT_NEED_IT.and(LivingEntity::canBeSeenAsEnemy).and(EntitySelector.NO_CREATIVE_OR_SPECTATOR);
    private final TargetingConditions predicate = TargetingConditions.forNonCombat()
            .range(64)
            .selector((e, w) -> entityPredicate.test(e));

    protected int cooldown;

    private final Creature creature;

    public WantItTakeItGoal(Creature creature, DynamicTargetGoal targetter) {
        super((Mob)creature.asEntity(), targetter);
        this.creature = creature;
    }

    @Override
    protected boolean canTarget(Entity e) {
        return e != null && !e.isRemoved() && (
                  (e instanceof LivingEntity l && predicate.test((ServerLevel)mob.level(), mob, l)
               || (e instanceof ItemEntity i && EnchantmentUtil.getWantItNeedItLevel(i) > 0)
            )
        );
    }

    @Override
    protected void attackTarget(Entity target, double reach, double distance) {
        ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, mob.position().add(VecHelper.sphere(mob.level().getRandom()).get()), 0.2F), mob, 1);

        double speed = 0.8D;

        if (distance > reach && distance < 16) {
            speed = 1.33;
        } else if (distance < 225) {
            speed = 0.6;
        }
        if (Math.abs(reach - distance) > 1) {
            speed *= 2;
        }

        reach = Math.max(1.5, reach);

        mob.getNavigation().moveTo(target, speed);

        cooldown = Math.max(cooldown - 1, 0);
        creature.setSmitten(true);

        if (distance <= reach) {
            if (target instanceof LivingEntity living) {
                if (cooldown <= 0) {
                    cooldown = 20;
                    mob.doHurtTarget(getServerLevel(mob.level()), target);
                    mob.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT);

                    if (mob.level().getRandom().nextInt(20) == 0) {
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            ItemStack stack = living.getItemBySlot(slot);
                            if (EnchantmentUtil.getWantItNeedItLevel(stack) > 0) {
                                AwaitTickQueue.scheduleTask(mob.level(), w -> {
                                    living.setItemSlot(slot, ItemStack.EMPTY);
                                    mob.equipItemIfPossible(getServerLevel(mob.level()), stack);
                                });
                                break;
                            }
                        }
                    }
                }
            } else if (target instanceof ItemEntity) {
                AwaitTickQueue.scheduleTask(mob.level(), w -> {
                    ItemEntity item = (ItemEntity)target;
                    ItemStack stack = item.getItem();

                    if (!item.isRemoved()) {
                        ItemStack collected = mob.equipItemIfPossible(getServerLevel(mob.level()), stack.copy());
                        if (!collected.isEmpty()) {
                            mob.onItemPickup(item);
                            mob.take(item, stack.getCount());
                            stack.shrink(collected.getCount());
                            if (stack.isEmpty()) {
                                item.discard();
                            }
                        }
                    }
                });
            }
        }
    }
}
