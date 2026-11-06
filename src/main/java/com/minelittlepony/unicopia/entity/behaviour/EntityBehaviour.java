package com.minelittlepony.unicopia.entity.behaviour;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.compat.pehkui.PehkUtil;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.LimbAnimationUtil;
import com.minelittlepony.unicopia.util.RegistryUtils;

public class EntityBehaviour<T extends Entity> {

    private static final EntityBehaviour<Entity> DEFAULT = new EntityBehaviour<>();
    private static final Registry<EntityBehaviour<?>> REGISTRY = RegistryUtils.createSimple(Unicopia.id("entity_behaviour"));

    /**
     * Equivalent of the entity#tick method. Called every tick to update th logic for a disguise.
     * <br>
     * We use this to add entity-specific behaviours.
     */
    public void update(Living<?> source, T entity, Disguise spell) {
        if (source instanceof Pony) {
            update((Pony)source, entity, spell);
        }
    }

    protected void update(Pony pony, T entity, Disguise spell) {

    }

    public void onImpact(Caster<?> source, T entity, double distance, float damageMultiplier, DamageSource cause) {

    }

    public T onCreate(T entity, EntityAppearance context, boolean wasNew) {
        entity.clearFire();
        PehkUtil.clearScale(entity);
        return entity;
    }

    public void onDestroy(T entity) {
        entity.setPermanentlyInvulnerable(false);
        entity.setNoGravity(false);
        entity.remove(RemovalReason.KILLED);
    }

    public boolean isEqual(T a, Entity b) {
        return a.getType() == b.getType();
    }

    public Optional<Float> getCameraDistance(Entity entity, Pony player) {
        if (entity == null) {
            return Optional.empty();
        }

        double normalHeight = player.asEntity().getDimensions(net.minecraft.world.entity.Pose.STANDING).height();
        double entityHeight = entity.getDimensions(entity.getPose()).height();

        return Optional.of((float)(entityHeight / normalHeight));
    }

    public Optional<EntityDimensions> getDimensions(T entity, Optional<EntityDimensions> current) {
        if (entity == null) {
            return Optional.empty();
        }

        EntityDimensions dims = entity.getDimensions(entity.getPose());

        float h = Math.max(0.001F, dims.height());
        float w = Math.max(0.001F, dims.width());

        if (current.isPresent() && h == current.get().height() && w == current.get().width()) {
            return current;
        }

        return Optional.of(EntityDimensions.scalable(w, h));
    }

    public void copyBaseAttributes(LivingEntity from, Entity to) {
        copyBaseAttributes(from, to, Vec3.ZERO);
    }

    public void copyBaseAttributes(LivingEntity from, Entity to, Vec3 positionOffset) {
        // Set first because position calculations rely on it
        to.tickCount = from.tickCount;
        ((EntityDuck)to).setRemovalReason(from.getRemovalReason());
        to.setOnGround(from.onGround());

        if (!from.level().isClientSide()) {
            // player collision is not known on the server
            boolean clip = to.noPhysics;
            to.noPhysics = false;
            Vec3 vel = from.getViewVector(1);

            if (!(to instanceof HangingEntity)) {
                to.move(MoverType.SELF, vel);
            } else {
                to.setDeltaMovement(Vec3.ZERO);
            }
            to.noPhysics = clip;
        } else {
            to.verticalCollision = from.verticalCollision;
            to.horizontalCollision = from.horizontalCollision;
        }

        if (EntityAppearance.isAxisAligned(to)) {
            double x = positionOffset.x + Math.floor(from.getX()) + 0.5;
            double y = positionOffset.y + Math.floor(from.getY());
            double z = positionOffset.z + Math.floor(from.getZ()) + 0.5;

            to.xo = x;
            to.yo = y;
            to.zo = z;

            to.xOld = x;
            to.yOld = y;
            to.zOld = z;

            to.absSnapTo(x, y, z);

            if (to instanceof FallingBlockEntity fbe) {
                fbe.setStartPos(from.blockPosition());
            }
        } else {
            to.copyPosition(from);

            to.xo = positionOffset.x + from.xo;
            to.yo = positionOffset.y + from.yo;
            to.zo = positionOffset.z + from.zo;

            to.xOld = positionOffset.x + from.xOld;
            to.yOld = positionOffset.y + from.yOld;
            to.zOld = positionOffset.z + from.zOld;
        }

        to.setDeltaMovement(from.getDeltaMovement());

        to.setXRot(from.getXRot());
        to.xRotO = from.xRotO;
        to.setYRot(from.getYRot());
        to.yRotO = from.yRotO;
        to.flyDist = from.flyDist;
        to.fallDistance = 0;
        to.setOnGround(from.onGround());
        to.setPermanentlyInvulnerable(from.isInvulnerable() || (from instanceof Player player && player.getAbilities().instabuild));

        to.moveDist = from.moveDist;

        if (to instanceof LivingEntity l) {
            l.yHeadRot = from.yHeadRot;
            l.yHeadRotO = from.yHeadRotO;
            l.yBodyRot = from.yBodyRot;
            l.yBodyRotO = from.yBodyRotO;

            LimbAnimationUtil.copy(from.walkAnimation, l.walkAnimation);

            var sourceSwing = (com.minelittlepony.unicopia.mixin.SwingStateAccessor)(Object)((com.minelittlepony.unicopia.mixin.LivingEntitySwingAccessor)from).unicopia$getSwingState();
            var targetSwing = (com.minelittlepony.unicopia.mixin.SwingStateAccessor)(Object)((com.minelittlepony.unicopia.mixin.LivingEntitySwingAccessor)l).unicopia$getSwingState();
            targetSwing.unicopia$setCurrentSwing(sourceSwing.unicopia$getCurrentSwing());
            targetSwing.unicopia$setTicks(sourceSwing.unicopia$getTicks());
            targetSwing.unicopia$setOldAnimation(sourceSwing.unicopia$getOldAnimation());
            targetSwing.unicopia$setAnimation(sourceSwing.unicopia$getAnimation());

            l.hurtTime = from.hurtTime;
            l.deathTime = from.deathTime;
            l.removeStingerTime = from.removeStingerTime;
            l.removeArrowTime = from.removeArrowTime;

            // disguise uses our health
            l.setHealth((from.getHealth() / from.getMaxHealth()) * l.getMaxHealth());

            // we use the disguise's air so changelings disguised as dolphin/axolotl/etc drown on land
            from.setAirSupply((l.getAirSupply() / l.getMaxAirSupply()) * from.getMaxAirSupply());

            copyInventory(from, l);
        }

        if (to instanceof TamableAnimal tameable) {
            tameable.setOrderedToSit(from.isShiftKeyDown());
        }

        if (to instanceof AbstractSkeleton skeleton) {
            skeleton.setAggressive(from.getUseItemRemainingTicks() > 0);
        }

        if (to instanceof LivingEntityDuck duck) {
            duck.updateItemUsage(from.getUsedItemHand(), from.getUseItem(), from.getUseItemRemainingTicks());
        }

        if (from.tickCount < 100 || from instanceof Player player && (player.isCreative() || player.isSpectator())) {
            to.clearFire();
        }

        if (to.isOnFire()) {
            from.igniteForSeconds(1);
        } else {
            from.clearFire();
        }

        to.setShiftKeyDown(from.isShiftKeyDown());
        if (to instanceof Player) {
            to.setPose(from.getPose());
        }
    }

    protected void copyInventory(LivingEntity from, LivingEntity l) {
        for (EquipmentSlot i : EquipmentSlot.values()) {
            if (!skipSlot(i)) {
                ItemStack neu = from.getItemBySlot(i);
                ItemStack old = l.getItemBySlot(i);
                if (old != neu) {
                    l.setItemSlot(i, neu);
                }
            }
        }
    }

    protected boolean skipSlot(EquipmentSlot slot) {
        return false;
    }

    protected boolean isSneakingOnGround(Caster<?> source) {
        Entity e = source.asEntity();
        return e.isShiftKeyDown() && (e.onGround() && !(e instanceof Player player && player.getAbilities().flying));
    }

    public static <T extends Entity> void register(Supplier<EntityBehaviour<T>> behaviour, EntityType<?>... types) {
        for (EntityType<?> type : types) {
            Registry.register(REGISTRY, EntityType.getKey(type), behaviour.get());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> EntityBehaviour<T> forEntity(@Nullable T entity) {
        if (entity == null) {
            return (EntityBehaviour<T>)DEFAULT;
        }
        return (EntityBehaviour<T>)REGISTRY.getOptional(EntityType.getKey(entity.getType())).orElse(DEFAULT);
    }

    static {
        register(PlayerBehaviour::new, EntityTypes.PLAYER);
        register(FallingBlockBehaviour::new, EntityTypes.FALLING_BLOCK);
        register(MobBehaviour::new, EntityTypes.RAVAGER);
        register(IronGolemBehaviour::new, EntityTypes.IRON_GOLEM);
        register(HoppingBehaviour::new, EntityTypes.RABBIT, EntityTypes.SLIME, EntityTypes.MAGMA_CUBE);
        register(TraderBehaviour::new, EntityTypes.VILLAGER, EntityTypes.WANDERING_TRADER);
        register(SteedBehaviour::new, EntityTypes.HORSE, EntityTypes.DONKEY, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE);
        register(SheepBehaviour::new, EntityTypes.SHEEP);
        register(BeeBehaviour::new, EntityTypes.BEE);
        register(GhastBehaviour::new, EntityTypes.GHAST);
        register(AxolotlBehaviour::new, EntityTypes.AXOLOTL);
        register(EndermanBehaviour::new, EntityTypes.ENDERMAN);
        EntityBehaviour.<Llama>register(() -> new RangedAttackBehaviour<>(USounds.Vanilla.LLAMA_SPIT, LlamaSpit::new), EntityTypes.LLAMA, EntityTypes.TRADER_LLAMA);
        EntityBehaviour.<SnowGolem>register(() -> new RangedAttackBehaviour<>(USounds.Vanilla.SNOW_GOLEM_SHOOT, (world, owner) -> new Snowball(world, owner, Items.SNOWBALL.getDefaultInstance())), EntityTypes.SNOW_GOLEM);
        register(SpellcastingIllagerBehaviour::new, EntityTypes.ILLUSIONER, EntityTypes.EVOKER);
        register(ShulkerBehaviour::new, EntityTypes.SHULKER);
        register(CreeperBehaviour::new, EntityTypes.CREEPER);
        register(SilverfishBehaviour::new, EntityTypes.SILVERFISH);
        register(ChickenBehaviour::new, EntityTypes.CHICKEN);
        register(BlazeBehaviour::new, EntityTypes.BLAZE);
        register(BreezeBehaviour::new, EntityTypes.BREEZE);
        register(MinecartBehaviour::new, EntityTypes.CHEST_MINECART, EntityTypes.COMMAND_BLOCK_MINECART, EntityTypes.FURNACE_MINECART, EntityTypes.HOPPER_MINECART, EntityTypes.MINECART, EntityTypes.SPAWNER_MINECART, EntityTypes.TNT_MINECART);
        register(CamelBehaviour::new, EntityTypes.CAMEL);
    }

    public static void bootstrap() {}
}
