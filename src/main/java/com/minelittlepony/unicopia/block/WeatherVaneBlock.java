package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.particle.TargetBoundParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.server.world.WeatherConditions;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.HolderLookup;

public class WeatherVaneBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(7.5F, 0, 7.5F, 8.5F, 14, 8.5F),
            Block.box(7, 0, 7, 9, 1, 9)
    );

    protected WeatherVaneBlock(Properties settings) {
        super(settings);
    }


    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WeatherVane(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, UBlockEntities.WEATHER_VANE, world.isClientSide() ? WeatherVane::clientTick : WeatherVane::serverTick);
    }

    public static class WeatherVane extends BlockEntity {
        private float angle;

        private float clientAngle;
        private float prevAngle;
        private float lastAngle;

        private Vec3 airflow = Vec3.ZERO;

        public WeatherVane(BlockPos pos, BlockState state) {
            super(UBlockEntities.WEATHER_VANE, pos, state);
        }

        public float getAngle(float tickDelta) {
            return Mth.lerp(tickDelta, prevAngle, clientAngle);
        }

        @Override
        protected void loadAdditional(net.minecraft.world.level.storage.ValueInput nbt) {
            angle = nbt.getFloatOr("angle", 0);
            airflow = new Vec3(nbt.getDoubleOr("windX", 0), 0, nbt.getDoubleOr("windZ", 0));
        }

        @Override
        protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput nbt) {
            nbt.putFloat("angle", angle);
            nbt.putDouble("windX", airflow.x);
            nbt.putDouble("windZ", airflow.z);
        }

        @Override
        public Packet<ClientGamePacketListener> getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this);
        }

        @Override
        public CompoundTag getUpdateTag(Provider lookup) {
            return saveWithoutMetadata(lookup);
        }

        public static void serverTick(Level world, BlockPos pos, BlockState state, WeatherVane entity) {
            Vec3 airflow = WeatherConditions.get(world).getWindDirection();
            float angle = (WeatherConditions.get(world).getWindYaw() % Mth.PI);

            entity.lastAngle = entity.prevAngle;
            entity.prevAngle = entity.angle;
            if (angle != entity.angle) {
                entity.angle = angle;

                entity.airflow = airflow;
                entity.setChanged();
                if (world instanceof ServerLevel sw) {
                    sw.getChunkSource().blockChanged(pos);
                }

                if (entity.lastAngle == entity.prevAngle) {
                    world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), USounds.BLOCK_WEATHER_VANE_ROTATE, SoundSource.BLOCKS, 1, 0.5F + (float)world.getRandom().nextGaussian());
                }
            }

        }

        public static void clientTick(Level world, BlockPos pos, BlockState state, WeatherVane entity) {
            entity.prevAngle = entity.clientAngle;

            float angle = entity.angle + (float)Math.sin(world.getGameTime() / 70F) * (world.isThundering() ? 30 : 1);

            float step = (Math.abs(entity.clientAngle) - Math.abs(angle)) / 7F;

            if (entity.clientAngle < angle) {
                entity.clientAngle += step;
            } else if (entity.clientAngle > angle) {
                entity.clientAngle -= step;
            }

            if (world.getRandom().nextInt(3) == 0) {
                float radius = 10;
                for (int i = 0; i < 5; i++) {
                    world.addAlwaysVisibleParticle(new TargetBoundParticleEffect(UParticles.WIND, null),
                            world.getRandom().triangle(pos.getX(), radius),
                            world.getRandom().triangle(pos.getY(), radius),
                            world.getRandom().triangle(pos.getZ(), radius),
                            entity.airflow.x / 10F, 0, entity.airflow.z / 10F
                    );
                }
            }
        }
    }
}
