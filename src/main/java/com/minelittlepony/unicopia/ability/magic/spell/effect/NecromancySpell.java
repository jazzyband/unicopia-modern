package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractAreaEffectSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.projectile.MagicBeamEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.Weighted;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.HolderLookup;

/**
 * An area-effect spell that summons the undead.
 */
public class NecromancySpell extends AbstractAreaEffectSpell implements ProjectileDelegate.EntityHitListener, ProjectileDelegate.BlockHitListener {
    private final Supplier<Optional<EntityType<? extends LivingEntity>>> spawnPool = new Weighted.Builder<EntityType<? extends LivingEntity>>()
            .put(7, EntityTypes.ZOMBIE)
            .put(4, EntityTypes.HUSK)
            .put(3, EntityTypes.DROWNED)
            .put(2, EntityTypes.ZOMBIFIED_PIGLIN)
            .put(1, EntityTypes.ZOMBIE_VILLAGER)
            .build();

    static final Map<Predicate<Entity>, EntityType<? extends Mob>> NECROMANTIC_CONVERSIONS = Map.of(
            match(EntityTypes.PIGLIN), EntityTypes.ZOMBIFIED_PIGLIN,
            match(EntityTypes.HOGLIN), EntityTypes.ZOGLIN,
            match(EntityTypes.HORSE), EntityTypes.ZOMBIE_HORSE,
            match(EntityTypes.DONKEY), EntityTypes.SKELETON_HORSE,
            match(EntityTypes.ZOMBIE), EntityTypes.HUSK,
            match(EntityTypes.SKELETON), EntityTypes.WITHER_SKELETON,
            match(EntityTypes.WANDERING_TRADER), EntityTypes.WITCH,
            match(EntityTypes.WITHER), EntityTypes.WARDEN,
            match(EntityTypes.WARDEN), EntityTypes.RABBIT,
            (e -> e instanceof Villager), EntityTypes.ZOMBIE_VILLAGER
    );
    static final Item[][] GEAR = {
            { Items.LEATHER_HELMET, Items.TURTLE_HELMET, Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET },
            { Items.LEATHER_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE },
            { Items.LEATHER_LEGGINGS, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS },
            { Items.LEATHER_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS },
            { Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL,
              Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD,
              Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE,
              Items.DIAMOND_SHOVEL, Items.DIAMOND_SWORD
            }
    };

    static Predicate<Entity> match(EntityType<?> type) {
        return e -> e.getType() == type;
    }

    static final SpellAttribute<Integer> WAVE_SIZE = SpellAttribute.create(SpellAttributeType.WAVE_SIZE, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.CHAOS, chaos -> 10 + (int)Mth.clamp(chaos, 0, 10));
    static final TooltipFactory TOOLTIP = TooltipFactory.of(RANGE, WAVE_SIZE);

    private final List<EntityReference<LivingEntity>> summonedEntities = new ArrayList<>();

    private int spawnCountdown;

    protected NecromancySpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        float radius = source.getLevel().getScaled(4) * 4 + RANGE.get(getTraits());

        if (radius <= 0) {
            return false;
        }

        boolean rainy = source.asWorld().isRainingAt(source.getOrigin());

        source.spawnParticles(new Sphere(true, radius * 2), rainy ? 98 : 125, pos -> {
            BlockPos bpos = BlockPos.containing(pos);

            if (source.asWorld().isEmptyBlock(bpos) && !source.asWorld().isEmptyBlock(bpos.below())) {
                source.addParticle(source.asWorld().isRainingAt(bpos) ? ParticleTypes.SMOKE : ParticleTypes.FLAME, pos, Vec3.ZERO);
            }
        });

        if (source.isClient() || source.asWorld().getDifficulty() == Difficulty.PEACEFUL) {
            return true;
        }

        summonedEntities.removeIf(ref -> ref.getOrEmpty(source.asWorld()).filter(e -> {
            if (e.isRemoved()) {
                return false;
            }
            if (e.position().distanceTo(source.getOriginVector()) > radius * 2) {
                e.level().broadcastEntityEvent(e, (byte)60);
                Vec3 pos = source.getOriginVector();
                e.setPosRaw(pos.x, pos.y, pos.z);
            }
            return true;
        }).isEmpty());

        if (--spawnCountdown > 0 && !summonedEntities.isEmpty()) {
            return true;
        }
        // TODO: refactory speed attribute
        // TODO: weather resistant attribute
        spawnCountdown = 1200 + source.asWorld().getRandom().nextInt(rainy ? 2000 : 1000);

        float additional = source.asServerWorld().getCurrentDifficultyAt(source.getOrigin()).getEffectiveDifficulty() + WAVE_SIZE.get(getTraits());

        if (summonedEntities.size() > additional) {
            return true;
        }

        Shape affectRegion = new Sphere(false, radius);

        for (int i = 0; i < 10; i++) {
            Vec3 pos = affectRegion.computePoint(source.asWorld().getRandom()).add(source.getOriginVector());

            BlockPos loc = BlockPos.containing(pos);

            if (source.asWorld().isEmptyBlock(loc.above()) && !source.asWorld().isEmptyBlock(loc)) {
                spawnPool.get().ifPresent(type -> {
                    spawnMonster(source, pos, type);
                });
            }
        }

        return true;
    }

    @Override
    protected void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        if (caster.isClient()) {
            return;
        }
        LivingEntity master = caster.getMaster();
        summonedEntities.forEach(ref -> {
            ref.ifPresent(caster.asWorld(), e -> {
                if (caster.asWorld() instanceof ServerLevel sw) {
                    EnchantmentHelper.doPostAttackEffects(sw, e, e.damageSources().indirectMagic(e, master));
                }
                e.level().broadcastEntityEvent(e, EntityEvent.POOF);
                e.discard();
            });
        });
    }

    protected void spawnMonster(Caster<?> source, Vec3 pos, EntityType<? extends LivingEntity> type) {
        LivingEntity minion = type.create(source.asWorld(), EntitySpawnReason.EVENT);

        if (!source.subtractEnergyCost(3)) {
            setDead();
        }

        minion.absSnapTo(pos.x, pos.y, pos.z, 0, 0);
        minion.setDeltaMovement(0, 0.3, 0);

        source.asWorld().levelEvent(LevelEvent.PARTICLES_DRAGON_FIREBALL_SPLASH, minion.blockPosition(), 0);
        source.playSound(USounds.SPELL_NECROMANCY_ACTIVATE, 1, 0.3F);
        source.spawnParticles(ParticleTypes.LARGE_SMOKE, 10);

        int level = source.getLevel().get();
        float levelSqr = Mth.clamp(level * level, 0, source.getLevel().getMax());
        float powerScale = levelSqr / source.getLevel().getMax();

        for (int i = source.asWorld().getRandom().nextInt(GEAR.length); i < GEAR.length; i++) {
            ItemStack pick = GEAR[i][(int)(powerScale * GEAR[i].length) % GEAR[i].length].getDefaultInstance();

            minion.setItemSlot(minion.getEquipmentSlotForItem(pick), pick);

            if (source.asWorld().getRandom().nextFloat() > powerScale) {
                break;
            }
        }

        Equine.of(minion).filter(eq -> eq instanceof Creature).ifPresent(eq -> {
            ((Creature)eq).setMaster(source);
            if (source.asWorld().getRandom().nextFloat() < source.getCorruption().getScaled(1)) {
                ((Creature)eq).setDiscorded(true);
            }
        });

        source.asWorld().addFreshEntity(minion);
        summonedEntities.add(new EntityReference<>(minion));
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        compound.putInt("spawnCountdown", spawnCountdown);
        if (summonedEntities.size() > 0) {
            ListTag list = new ListTag();
            summonedEntities.forEach(ref -> list.add(ref.toNBT(lookup)));
            compound.put("summonedEntities", list);
        }
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        spawnCountdown = compound.getIntOr("spawnCountdown", 0);
        summonedEntities.clear();
        compound.getList("summonedEntities").ifPresent(l -> l.forEach(tag -> {
            summonedEntities.add(new EntityReference<>((CompoundTag)tag, lookup));
        }));
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockHitResult hit) {
        if (!(projectile instanceof MagicBeamEntity source)) {
            return;
        }

        Shape affectRegion = new Sphere(false, 3);

        for (int i = 0; i < 10; i++) {
            Vec3 pos = affectRegion.computePoint(source.asWorld().getRandom()).add(source.getOriginVector());

            BlockPos loc = BlockPos.containing(pos);

            if (source.asWorld().isEmptyBlock(loc.above()) && !source.asWorld().isEmptyBlock(loc)) {
                spawnPool.get().ifPresent(type -> {
                    spawnMonster(source, pos, type);
                });
            }
        }

        summonedEntities.clear();
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        NECROMANTIC_CONVERSIONS.entrySet().stream().filter(entry -> entry.getKey().test(hit.getEntity())).findFirst().ifPresent(entry -> {
            Mob target = ((Mob)hit.getEntity());
            target.convertTo(entry.getValue(), ConversionParams.single(target, true, true), newEntity -> {
                newEntity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
            });
        });
    }
}
