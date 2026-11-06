package com.minelittlepony.unicopia.block;

import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class StrippablePillarBlock extends RotatedPillarBlock implements StrippingLootable {
    private Optional<ResourceKey<LootTable>> strippingLootTableKey;

    public StrippablePillarBlock(Properties settings) {
        super(settings);
        strippingLootTableKey = getLootTable().map(key ->ResourceKey.create(Registries.LOOT_TABLE, key.identifier().withSuffix("_stripping")));
    }

    @Override
    public final Optional<ResourceKey<LootTable>> getStrippingLootTableKey() {
        return strippingLootTableKey;
    }

    @Override
    public boolean onStripped(UseOnContext context) {
        context.getItemInHand().hurtAndBreak(2, context.getPlayer(), context.getHand().asEquipmentSlot());
        if (context.getLevel() instanceof ServerLevel sw) {
            getStrippingLootTableKey().map(sw.getServer().reloadableRegistries()::getLootTable).ifPresent(table -> {
                table.getRandomItems(new LootParams.Builder(sw)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(context.getClickedPos()))
                            .withParameter(LootContextParams.TOOL, context.getItemInHand())
                            .withParameter(LootContextParams.BLOCK_STATE, context.getLevel().getBlockState(context.getClickedPos()))
                            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, context.getLevel().getBlockEntity(context.getClickedPos()))
                        .create(LootContextParamSets.BLOCK))
                    .forEach(stack -> Block.popResource(sw, context.getClickedPos(), stack));
            });
        }

        return true;
    }

}
