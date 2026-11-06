package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.component.Charges;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BellItem extends Item {
    public BellItem(Properties settings) {
        super(settings.component(UDataComponentTypes.CHARGES, Charges.of(1000, 1000)));
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 3000;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        player.startUsingItem(hand);
        Pony pony = Pony.of(player);
        pony.getCorruption().add(1 + player.getRandom().nextIntBetweenInclusive(1, 10));
        pony.playSound(USounds.ITEM_GROGAR_BELL_USE, 0.4F, 0.2F);
        Living<?> targetLiving = target instanceof Mob || target instanceof Player ? Living.getOrEmpty(target)
                .filter(living -> !(living instanceof Creature c && c.isDiscorded()))
                .orElse(null) : null;
        pony.setTarget(targetLiving);
        return targetLiving == null ? InteractionResult.FAIL : InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack offhandStack = AmuletItem.get(player).stack();

        if (!Charges.of(offhandStack).canHoldCharge()) {
            offhandStack = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        }

        Pony pony = Pony.of(player);

        Charges charges = Charges.of(stack);

        if (charges.energy() > 0) {
            pony.playSound(USounds.ITEM_GROGAR_BELL_CHARGE, 0.6F, 1);
            pony.getCorruption().add(player.getRandom().nextIntBetweenInclusive(1, 10));

            Charges offhandCharges = Charges.of(offhandStack);

            if (offhandCharges.canHoldCharge()) {
                int maxChargeBy = charges.maximum() - offhandCharges.energy();
                int energyTransferred = Math.min(charges.energy(), maxChargeBy);
                Charges.recharge(offhandStack, energyTransferred);
                Charges.discharge(stack, energyTransferred);
            } else {
                pony.getMagicalReserves().getMana().add(charges.energy());
                stack.set(UDataComponentTypes.CHARGES, charges.withEnergy(0));
            }

            pony.spawnParticles(Vec3.atCenterOf(pony.getPhysics().getHeadPosition()), new Sphere(false, 0.5F), 7, p -> {
                pony.addParticle(new MagicParticleEffect(0xAAFFFF), p, Vec3.ZERO);
            });

            return InteractionResult.CONSUME;
        }

        pony.playSound(USounds.ITEM_GROGAR_BELL_USE, 0.01F, 0.9F);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level world, LivingEntity userEntity, ItemStack stack, int remainingUseTicks) {
        if (userEntity.tickCount % 5 != 0) {
            return;
        }

        Living.getOrEmpty(userEntity).ifPresent(user -> {
            user.getTarget().ifPresent(living -> {
                float maxUseTime = getUseDuration(stack, userEntity);
                float progress = (maxUseTime - remainingUseTicks) / maxUseTime;

                if (tickDraining(user, living, stack, progress)) {
                    onStoppedDraining(user, living, true);
                }
            });
        });
    }

    private boolean onStoppedDraining(Living<?> user, Living<?> target, boolean completed) {
        user.setTarget(null);
        user.playSound(USounds.ITEM_GROGAR_BELL_STOP_USING, 0.2F, 0.3F);
        if (target instanceof Creature creature && (completed || target.asEntity().getHealth() < (target.asEntity().getMaxHealth() * 0.5F) + 1)) {
            ItemStack handStack = creature.asEntity().getItemInHand(InteractionHand.MAIN_HAND);
            if (!handStack.isEmpty()) {
                creature.asEntity().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                creature.asEntity().spawnAtLocation(creature.asServerWorld(), handStack);
            }
            creature.setDiscorded(true);
            return true;
        }
        return false;
    }

    private boolean tickDraining(Living<?> user, Living<?> living, ItemStack stack, float progress) {
        if (living.getOrigin().distSqr(user.getOrigin()) > 25 || living.asEntity().isRemoved()) {
            return true;
        }

        float amountDrawn;
        ParticleOptions particleType = ParticleTypes.COMPOSTER;

        if (living instanceof Pony pony) {
            amountDrawn = pony.getMagicalReserves().getMana().get() * 0.2F;
            pony.getMagicalReserves().getMana().multiply(0.8F);
            if (pony.getSpecies() == Race.CHANGELING) {
                particleType = ParticleTypes.HEART;
            }
        } else {
            float damageAmount = Math.min(Math.max(1, living.asEntity().getMaxHealth() / 25F), living.asEntity().getHealth() - 1);
            living.asEntity().hurtServer(user.asServerWorld(), user.damageOf(UDamageTypes.EXHAUSTION, user), damageAmount);
            living.asEntity().setLastHurtByMob(user.asEntity());
            if (living.asEntity() instanceof Mob mob) {
                mob.setTarget(null);
            }
            amountDrawn = Math.max(living.asEntity().getBbWidth(), living.asEntity().getBbHeight()) + living.asEntity().getHealth();

            if (living.asEntity() instanceof Creeper creeper) {
                creeper.setSwellDir(-1);

                if (creeper.getEntityData().get(Creeper.DATA_IS_POWERED)) {
                    creeper.getEntityData().set(Creeper.DATA_IS_POWERED, false);
                    amountDrawn += 60;
                }
                creeper.getEntityData().set(Creeper.DATA_IS_IGNITED, false);
            }

            if (living.asEntity() instanceof AbstractIllager) {
                particleType = ParticleTypes.ANGRY_VILLAGER;
            }

            if (living.asEntity().getHealth() <= 1) {
                return true;
            }
        }
        if (Charges.discharge(stack, -(int)amountDrawn)) {
            user.playSound(USounds.ITEM_GROGAR_BELL_DRAIN, 0.2F, progress);

            for (int i = 0; i < 4; i++) {
                living.addParticle(
                        new FollowingParticleEffect(UParticles.HEALTH_DRAIN, user.asEntity(), 0.4F)
                            .withChild(particleType),
                        living.getOriginVector().add(0, living.getPhysics().getGravitySignum() * living.asEntity().getBbHeight() / 2, 0)
                            .add(VecHelper.triangular(user.asWorld().getRandom(), 0, 0.2)),
                        Vec3.ZERO
                );
            }
        }

        return false;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        Living.getOrEmpty(user).ifPresent(living -> {
            living.getTarget().ifPresent(target -> {
                onStoppedDraining(living, target, true);
            });
        });
        return super.finishUsingItem(stack, world, user);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        return Living.getOrEmpty(user)
            .filter(living -> living.getTarget()
                .filter(target -> onStoppedDraining(living, target, false))
                .isPresent())
            .isPresent();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || Charges.of(stack).energy() > 0;
    }
}
