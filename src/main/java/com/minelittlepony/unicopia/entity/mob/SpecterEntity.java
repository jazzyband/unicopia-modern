package com.minelittlepony.unicopia.entity.mob;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.particle.FootprintParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class SpecterEntity extends Monster {
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16F)
                .add(Attributes.MOVEMENT_SPEED, 0.5F);
    }

    private double stepDistance;
    private double nextStepDistance;
    private boolean wasLeft;

    public long lastHitboxRenderTime = -1;
    public long lastInViewportTime = -1;
    public long hideInViewportTime = -1;

    public SpecterEntity(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        //this.targetSelector.add(2, new TargetGoal<>(this, PlayerEntity.class));
    }

    @Override
    public void tick() {
        Vec3 prevPosition = position();
        super.tick();

        if (getTarget() != null) {
            ParticleUtils.spawnParticles(net.minecraft.core.particles.SpellParticleOption.create(ParticleTypes.EFFECT, 0xFFFFFF, 1), this, 6);

            if (level() instanceof ServerLevel sw && sw.getGameRules().get(GameRules.MOB_GRIEFING)) {
                if (sw.getBlockState(blockPosition()).is(BlockTags.REPLACEABLE_BY_TREES)) {
                    sw.destroyBlock(blockPosition(), true);
                }
            }
        }

        if (!isPassenger() && onGround()) {
            stepDistance += position().subtract(prevPosition).horizontalDistance() * 0.6F;
            if (stepDistance >= nextStepDistance) {
                nextStepDistance = stepDistance + 1;
                wasLeft = !wasLeft;
                float offset = 0.4F;
                float yaw = getYHeadRot();
                Vec3 offsetVec = new Vec3((wasLeft ? offset : -offset), 0, 0).yRot(yaw);
                level().addParticle(new FootprintParticleEffect(yaw), true, true, getX() + offsetVec.x(), getY(), getZ() + offsetVec.z(), 0, 0, 0);
                ParticleUtils.spawnParticles(new BlockParticleOption(ParticleTypes.BLOCK, getBlockStateOn()), level(), position(), 6);
                playSound(getBlockStateOn().getSoundType().getStepSound(), 0.5F, 1);
            }
        }
    }

    @Override
    public void setLastHurtByMob(@Nullable LivingEntity attacker) {
        if (!level().isClientSide() && attacker != null) {
            level().getEntitiesOfClass(SpecterEntity.class, this.getBoundingBox().inflate(5), e -> e != this && e.getTarget() == null).forEach(specter -> {
                specter.notifyPartyAttacker(this, attacker);
            });
        }
        super.setLastHurtByMob(attacker);
    }

    private void notifyPartyAttacker(SpecterEntity sender, @Nullable LivingEntity attacker) {
        super.setLastHurtByMob(attacker);
        getNavigation().stop();
        getNavigation().moveTo(sender, 3);
        playSound(USounds.Vanilla.VEX_HURT, 1, 0.5F);
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.3F;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected void playSwimSound(float volume) {

    }

    @Override
    protected void doWaterSplashEffect() {

    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData data) {
        data = super.finalizeSpawn(world, difficulty, spawnReason, data);
        RandomSource random = world.getRandom();
        float diff = difficulty.getSpecialMultiplier();
        setCanPickUpLoot(random.nextFloat() < 0.55F * diff);
        populateDefaultEquipmentSlots(random, difficulty);
        return data;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance localDifficulty) {
        if (random.nextFloat() < (level().getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F)) {
            if (random.nextFloat() < (level().getDifficulty() == Difficulty.HARD ? 0.5F : 0.1F)) {
                super.populateDefaultEquipmentSlots(random, localDifficulty);
            }
            setItemSlot(EquipmentSlot.MAINHAND, (random.nextInt(3) == 0 ? Items.STONE_SWORD : Items.WOODEN_SWORD).getDefaultInstance());
        }
    }

    static class TargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        public TargetGoal(SpecterEntity specter, Class<T> targetEntityClass) {
            super(specter, targetEntityClass, true);
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean canUse() {
            return mob.getLightLevelDependentMagicValue() < 0.5F && super.canUse();
        }
    }
}
