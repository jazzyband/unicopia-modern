package com.minelittlepony.unicopia.server.world;

import com.minelittlepony.unicopia.block.cloud.CloudLike;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CloudStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;

public interface WeatherAccess extends LevelReader {
    int CLOUD_GENERATION_HEIGHT = 230;
    int CLOUDS_BUFFER_RANGE = 5;
    int FANCY_CLOUDS_BUFFER_RANGE = 10;
    int CHUNK_SECTION_HEIGHT = SectionPos.SECTION_SIZE;

    void setWeatherOverride(Float rain, Float thunder);

    default boolean isInRangeOfStorm(BlockPos pos) {
        return WeatherConditions.get(this).isInRangeOfStorm(pos);
    }

    @Environment(EnvType.CLIENT)
    default boolean isBelowClientCloudLayer(BlockPos pos) {

        int range = Minecraft.getInstance().options.getCloudStatus() == CloudStatus.FANCY ? FANCY_CLOUDS_BUFFER_RANGE : CLOUDS_BUFFER_RANGE;

        if (pos.getY() < CLOUD_GENERATION_HEIGHT - range) {
            return true;
        }

        ChunkAccess chunk = getChunk(pos);
        int topSection = chunk.getHighestFilledSectionIndex();

        if (topSection > ChunkAccess.NO_FILLED_SECTION) {
            int sectionBottomY = SectionPos.sectionToBlockCoord(topSection);
            if (sectionBottomY >= pos.getY() - CHUNK_SECTION_HEIGHT) {
                BlockPos.MutableBlockPos mutable = pos.mutable();
                BlockPos.MutableBlockPos probeMutable = pos.mutable();
                int maxDistance = CHUNK_SECTION_HEIGHT;

                while (!isOutsideBuildHeight(mutable)) {
                    if (--maxDistance <= 0) break;
                    if (!isEmptyBlock(probeMutable.setY(mutable.getY() + range))) {

                        mutable.set(pos);
                        maxDistance = CHUNK_SECTION_HEIGHT;

                        while (!isOutsideBuildHeight(mutable)) {
                            if (--maxDistance <= 0) break;
                            if (getBlockState(probeMutable.setY(mutable.getY())).getBlock() instanceof CloudLike) {
                                return false;
                            }
                            mutable.move(Direction.DOWN);
                        }

                        return true;
                    }
                    mutable.move(Direction.UP);
                }
            }
        }
        return false;
    }

    default boolean isBelowCloudLayer(BlockPos pos) {
        if (pos.getY() < CLOUD_GENERATION_HEIGHT) {
            return true;
        }

        ChunkAccess chunk = getChunk(pos);
        int topSection = chunk.getHighestFilledSectionIndex();

        if (topSection > ChunkAccess.NO_FILLED_SECTION) {
            int sectionBottomY = SectionPos.sectionToBlockCoord(topSection);
            if (sectionBottomY >= pos.getY() - CHUNK_SECTION_HEIGHT) {
                BlockPos.MutableBlockPos mutable = pos.mutable();
                int maxDistance = CHUNK_SECTION_HEIGHT * 2;

                while (!isOutsideBuildHeight(mutable)) {
                    if (--maxDistance <= 0) break;
                    if (!isEmptyBlock(mutable)) {
                        return true;
                    }
                    mutable.move(Direction.UP);
                }
            }
        }
        return false;
    }
}
