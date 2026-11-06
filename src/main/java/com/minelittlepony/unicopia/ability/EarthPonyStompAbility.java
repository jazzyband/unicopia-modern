package com.minelittlepony.unicopia.ability;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.LandingEventHandler;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.server.world.BlockDestructionManager;
import com.minelittlepony.unicopia.server.world.ModificationType;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.VecHelper;

/**
 * Earth Pony stomping ability
 */
public class EarthPonyStompAbility implements Ability<Hit> {

    private final double rad = 4;

    private final AABB areaOfEffect = new AABB(
            -rad, -rad, -rad,
             rad,  rad,  rad
     );

    @Override
    public int getWarmupTime(Pony player) {
        return 3;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 50;
    }

    @Override
    public Identifier getIcon(Pony player) {
        Identifier id = Abilities.REGISTRY.getKey(this);
        Race race = player.getObservedSpecies();
        return id.withPath(p -> "textures/gui/ability/" + p
            + "_" + (race.isHuman() ? Race.EARTH : race).getId().getPath()
            + ".png");
    }

    @Override
    public double getCostEstimate(Pony player) {
        return rad;
    }

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        if (player.asEntity().getDeltaMovement().y * player.getPhysics().getGravitySignum() < 0
                && !player.asEntity().getAbilities().flying
                && !player.asEntity().isFallFlying()
                && !player.asEntity().isAutoSpinAttack()) {
            thrustDownwards(player);
            return Hit.INSTANCE;
        }

        return Optional.empty();
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Hit> getSerializer() {
        return Hit.CODEC;
    }

    private void thrustDownwards(Pony player) {
        BlockPos ppos = player.getOrigin();
        BlockPos pos = PosHelper.findSolidGroundAt(player.asWorld(), ppos, player.getPhysics().getGravitySignum());

        double downV = Math.sqrt(ppos.distSqr(pos)) * player.getPhysics().getGravitySignum();
        player.asEntity().push(0, -downV, 0);
        player.updateVelocity();
    }

    @Override
    public boolean apply(Pony iplayer, Hit data) {
        final Player player = iplayer.asEntity();
        final double initialY = player.getY() + 5;

        var r = new LandingEventHandler.Callback() {
            @Override
            public double dispatch(double fallDistance) {
                // fail if landing above the starting position
                if (player.getY() > initialY) {
                    return fallDistance;
                }

                player.fallDistance = 0;
                BlockPos center = PosHelper.findSolidGroundAt(player.level(), player.blockPosition(), iplayer.getPhysics().getGravitySignum());

                float heavyness = EnchantmentUtil.getWeight(player);

                iplayer.asWorld().getEntities(player, areaOfEffect.move(iplayer.getOriginVector())).forEach(i -> {
                    double dist = Math.sqrt(center.distSqr(i.blockPosition()));

                    if (dist <= rad + 3) {
                        double inertia = 2 / dist;

                        if (i instanceof LivingEntity l) {
                            inertia *= EnchantmentUtil.getWeight(l);
                        }
                        inertia /= heavyness;

                        double liftAmount = Math.sin(Math.PI * dist / rad) * 12 * iplayer.getPhysics().getGravitySignum();

                        i.push(
                                -(player.getX() - i.getX()) / inertia,
                                -(player.getY() - i.getY() - liftAmount) / inertia + (dist < 1 ? dist : 0),
                                -(player.getZ() - i.getZ()) / inertia);

                        double amount = (1.5F * player.getAttribute(Attributes.ATTACK_DAMAGE).getValue() + heavyness * 0.4) / (float)(dist * 1.3F);

                        if (i instanceof Player) {
                            Race.Composite race = Pony.of((Player)i).getCompositeRace();
                            if (race.canUseEarth()) {
                                amount /= 3;
                            }

                            if (race.canFly()) {
                                amount *= 4;
                            }
                        }

                        if (i instanceof LivingEntity l) {
                            amount /= EnchantmentUtil.getImpactReduction(l);
                        }

                        i.hurtServer((ServerLevel)iplayer.asWorld(), iplayer.damageOf(UDamageTypes.SMASH, iplayer), (float)amount);
                        Living.updateVelocity(i);
                    }
                });

                double radius = rad + heavyness * 0.3;

                spawnEffectAround(iplayer, player, center, radius, rad);

                ParticleUtils.spawnParticle(player.level(), UParticles.GROUND_POUND, player.getX(), player.getY() - 1, player.getZ(), 0, 0, 0);
                BlockState steppingState = player.getBlockStateOn();
                if (steppingState.is(UTags.Blocks.KICKS_UP_DUST)) {
                    ParticleUtils.spawnParticle(player.level(), new BlockParticleOption(UParticles.DUST_CLOUD, steppingState), Vec3.atCenterOf(player.blockPosition().below()), Vec3.ZERO);
                }

                iplayer.subtractEnergyCost(rad);
                iplayer.asEntity().causeFoodExhaustion(3);
                return 0D;
            }

            @Override
            public void onCancelled() {
                iplayer.playSound(USounds.GUI_ABILITY_FAIL, 1F);
            }
        };

        if (iplayer.asEntity().onGround()) {
            iplayer.setAnimation(Animation.STOMP, Animation.Recipient.ANYONE, 10);
            iplayer.asEntity().jumpFromGround();
            iplayer.updateVelocity();
            AwaitTickQueue.scheduleTask(iplayer.asWorld(), w -> r.dispatch(0F), 5);
        } else {
            thrustDownwards(iplayer);
            iplayer.waitForFall(r);
        }

        return true;
    }

    public static void spawnEffectAround(Pony pony, Entity source, BlockPos center, double radius, double range) {
        BlockPos.betweenClosedStream(new BoundingBox(center).inflatedBy(Mth.ceil(radius))).forEach(i -> {
            double dist = Math.sqrt(i.distToLowCornerSqr(source.getX(), source.getY(), source.getZ()));

            if (dist <= radius) {
                spawnEffect(pony, source.level(), i, dist, range);
            }
        });
    }

    public static void spawnEffect(Pony pony, Level w, BlockPos pos, double dist, double rad) {
        if (w.getBlockState(pos.above()).isAir()) {
            BlockState state = w.getBlockState(pos);

            float hardness = state.getDestroySpeed(w, pos);
            float scaledHardness = (1 - hardness / 70);
            float damage = hardness < 0 ? 0 : Mth.clamp((int)((1 - dist / rad) * 9 * scaledHardness), 0, BlockDestructionManager.MAX_DAMAGE - 1);

            stompBlock(pony, w, pos, damage);
        }
    }

    public static void stompBlock(Pony pony, Level w, BlockPos pos, float damage) {
        BlockState state = w.getBlockState(pos);

        if (state.isAir() || damage <= 0) {
            return;
        }

        if (BlockDestructionManager.of(w).damageBlock(pos, damage) >= BlockDestructionManager.MAX_DAMAGE && pony.canModifyAt(pos, ModificationType.PHYSICAL)) {
            w.destroyBlock(pos, true);

            if (w instanceof ServerLevel) {
                if (state.is(BlockTags.BASE_STONE_OVERWORLD) && w.getRandom().nextInt(4) == 0) {
                    ItemStack stack = new ItemStack(UItems.PEBBLES, 1 + w.getRandom().nextInt(2));
                    Block.popResource(w, pos, stack);
                    state.spawnAfterBreak((ServerLevel)w, pos, stack, true);
                }
            }
        } else {
            w.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
        }

        if (state.is(UTags.Blocks.KICKS_UP_DUST)) {
            if (w.getRandom().nextInt(4) == 0 && w.isEmptyBlock(pos.above()) && w.getFluidState(pos.above()).isEmpty()) {
                ParticleUtils.spawnParticle(w, new BlockParticleOption(UParticles.DUST_CLOUD, state), Vec3.atCenterOf(pos.above()), VecHelper.triangular(w.getRandom(), 0, 0.1F));
            }
        }
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(40);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
    }
}
