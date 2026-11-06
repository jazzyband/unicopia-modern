package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.Projectile;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class WeatherJarItem extends BlockItem implements Projectile, ProjectileDelegate.HitListener {
    private final Type type;

    public WeatherJarItem(Block block, Item.Properties settings, Type type) {
        super(block, settings.overrideDescription(block.getDescriptionId()));
        this.type = type;
        Projectile.makeDispensable(this);
    }

    @Override
    public SoundEvent getThrowSound(ItemStack stack) {
        return USounds.ENTITY_JAR_THROW;
    }

    @Override
    public float getProjectileDamage(ItemStack stack) {
        return 0.5F;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            return super.use(world, player, hand);
        }
        return triggerThrow(world, player, hand);
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile) {
        releaseContents(projectile.level(), projectile.blockPosition());
    }

    public void releaseContents(Level world, BlockPos pos) {
        if (!world.isClientSide()) {
            ServerLevel sw = (ServerLevel)world;

            if (type == Type.RAIN || type == Type.THUNDER) {
                // clear weather time = number of ticks for which the weather is clear
                // rain time = ticks until rain gets toggled (reset the tick after toggling)
                // thunder time = ticks until thundering gets toggled (reset the tick after toggling)

                // clear weather time
                //   Number of ticks weather must stay clear.
                //   Raining and thundering, and raining/thundering times are kept to false and 0
                // when clear weather time is <= 0
                //   - wait for thunder time to reach zero then toggle thundering
                //   - wait for rain time to reach zero then toggle raining
                // when thunder time is <= 0
                //   - randomly pick a new value for thunder time
                // when rain time is <= 0
                //   - randomly pick a new value for rain time

                var weather = sw.getWeatherData();
                weather.setClearWeatherTime(0);
                weather.setRainTime(0);
                weather.setThunderTime(0);
                weather.setRaining(type == Type.RAIN || type == Type.THUNDER);
                weather.setThundering(type == Type.THUNDER);

                if (type == Type.THUNDER) {
                    for (int i = world.getRandom().nextInt(7); i > 0; i--) {
                        AwaitTickQueue.scheduleTask(world, w -> {
                            LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(world, EntitySpawnReason.EVENT);
                            bolt.setVisualOnly(true);
                            bolt.snapTo(Vec3.atBottomCenterOf(world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, world.getBlockRandomPos(
                                    SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(pos.getX())),
                                    0,
                                    SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(pos.getZ())),
                                    15
                            )).above(32)));
                            world.addFreshEntity(bolt);
                        }, 15 + world.getRandom().nextInt(12));
                    }
                }
            }

            if (type == Type.LIGHTNING) {
                LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(world, EntitySpawnReason.EVENT);
                lightning.snapTo(pos.getX(), pos.getY(), pos.getZ());

                world.addFreshEntity(lightning);
            }
        }

        Vec3 centerPos = Vec3.atCenterOf(pos);

        if (type == Type.LIGHTNING) {
            ParticleUtils.spawnParticle(world, LightningBoltParticleEffect.DEFAULT, centerPos, Vec3.ZERO);
        }

        if (type == Type.RAIN || type == Type.THUNDER) {
            world.levelEvent(LevelEvent.PARTICLES_SPELL_POTION_SPLASH, pos, type == Type.THUNDER ? 0x888888 : 0xF8F8F8);

            for (int i = world.getRandom().nextInt(3) + 1; i >= 0; i--) {
                ParticleUtils.spawnParticle(world, UParticles.CLOUDS_ESCAPING,
                        centerPos.x(), centerPos.y(), centerPos.z(),
                        world.getRandom().nextFloat() - 0.5,
                        0,
                        world.getRandom().nextFloat() - 0.5
                );
            }
        }

        world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(Blocks.GLASS.defaultBlockState()));
    }

    public enum Type {
        RAIN,
        THUNDER,
        LIGHTNING
    }
}
