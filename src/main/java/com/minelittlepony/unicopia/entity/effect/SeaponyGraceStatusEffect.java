package com.minelittlepony.unicopia.entity.effect;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;

public class SeaponyGraceStatusEffect {

    public static void update(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        if (EquinePredicates.PLAYER_SEAPONY.test(entity)) {
            if (!entity.hasEffect(UEffects.SEAPONYS_GRACE) && !entity.hasEffect(UEffects.SEAPONYS_IRE)) {
                entity.addEffect(new MobEffectInstance(UEffects.SEAPONYS_GRACE, MobEffectInstance.INFINITE_DURATION, 0, false, true));
            }
        } else if (entity.hasEffect(UEffects.SEAPONYS_GRACE)) {
            entity.removeEffect(UEffects.SEAPONYS_GRACE);
        }
    }

    public static boolean hasIre(LivingEntity entity, Mob enemy) {
        return enemy.getLastHurtByMob() == entity || hasIre(entity);
    }

    public static boolean hasGrace(LivingEntity entity) {
        boolean isSeapony = EquinePredicates.PLAYER_SEAPONY.test(entity);
        return isSeapony && entity.hasEffect(UEffects.SEAPONYS_GRACE) && !entity.hasEffect(UEffects.SEAPONYS_IRE);
    }

    public static boolean hasIre(LivingEntity entity) {
        return !EquinePredicates.PLAYER_SEAPONY.test(entity) || entity.hasEffect(UEffects.SEAPONYS_IRE);
    }

    public static void processBlockChange(Level world, Player player, BlockPos pos, BlockState stateBroken, @Nullable BlockEntity blockEntity) {

        if (!(world instanceof ServerLevel sw)) {
            return;
        }

        if (!hasGrace(player)) {
            return;
        }

        if (!stateBroken.is(UTags.Blocks.ANGERS_GUARDIANS)) {
            return;
        }
        StructureStart start = sw.structureManager().getStructureWithPieceAt(pos, key -> key.is(BuiltinStructures.OCEAN_MONUMENT));
        if (start.getStructure() == null) {
            return;
        }

        List<Guardian> guardians = sw.getEntitiesOfClass(Guardian.class, player.getBoundingBox().inflate(10), EntitySelector.LIVING_ENTITY_STILL_ALIVE);

        if (guardians.size() > 0) {
            guardians.forEach(guardian -> {
                guardian.setTarget(player);
                guardian.playAmbientSound();
            });

            player.removeEffect(UEffects.SEAPONYS_GRACE);
            player.addEffect(new MobEffectInstance(UEffects.SEAPONYS_IRE, 90000, 0, false, true));
            world.playSound(null, pos, USounds.Vanilla.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 1, 1);
        }
    }
}
