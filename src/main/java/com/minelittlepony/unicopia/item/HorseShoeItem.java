package com.minelittlepony.unicopia.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.projectile.PhysicsBodyProjectileEntity;

public class HorseShoeItem extends HeavyProjectileItem {

    private final float projectileInnacuracy;
    private final float baseProjectileSpeed;

    public HorseShoeItem(Item.Properties settings, float projectileDamage, float projectileInnacuracy, float baseProjectileSpeed) {
        super(settings.attributes(ItemAttributeModifiers.builder().add(
            UEntityAttributes.EXTENDED_ATTACK_DISTANCE,
            new AttributeModifier(UItemModifierIds.ATTACK_RANGE_MODIFIER_ID, -3F, AttributeModifier.Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND
    ).build()), projectileDamage);
        this.projectileInnacuracy = projectileInnacuracy;
        this.baseProjectileSpeed = baseProjectileSpeed;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        float degradation = (stack.getDamageValue() / (float)stack.getMaxDamage());
        float inaccuracy = projectileInnacuracy + degradation * 30;

        var race = InteractionManager.getInstance().getClientPony().map(Pony::getCompositeRace).orElse(null);
        float speed = baseProjectileSpeed;
        if (race != null) {
            if (race.any(Race::canUseEarth)) {
                speed += 0.5F;
            }
            if (!race.includes(Race.ALICORN) && race.physical().canFly()) {
                speed /= 1.5F;
            }
        }
        speed /= 1.5F;
        speed *= 1 - (0.6F * degradation);

        textConsumer.accept(Component.empty());
        textConsumer.accept(Component.translatable("item.unicopia.horse_shoe.accuracy", 100 * (30 - inaccuracy) / 30).withStyle(ChatFormatting.GRAY));
        textConsumer.accept(Component.translatable("item.unicopia.horse_shoe.speed", Math.max(0.2F, speed)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public PhysicsBodyProjectileEntity createProjectile(ItemStack stack, Level world, @Nullable Player player) {
        PhysicsBodyProjectileEntity projectile = super.createProjectile(stack, world, player);
        projectile.setDamageType(UDamageTypes.HORSESHOE);

        float degradation = (stack.getDamageValue() / (float)stack.getMaxDamage());

        if (player != null) {
            Pony pony = Pony.of(player);
            var race = pony.getCompositeRace();
            float speed = baseProjectileSpeed + 0.1F;
            if (race.any(Race::canUseEarth)) {
                speed += 0.5F;
            }
            if (!race.includes(Race.ALICORN) && race.physical().canFly()) {
                speed /= 1.5F;
            }
            speed /= 1.5F;
            speed *= 1 - (0.6F * degradation);
            float inaccuracy = projectileInnacuracy + degradation * 30;
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, Math.max(0.2F, speed), inaccuracy);
        }
        return projectile;
    }

    @Override
    public void shoot(Projectile entity, double x, double y, double z, float power, float uncertainty) {
        float degradation = 0;
        if (entity instanceof PhysicsBodyProjectileEntity p) {
            ItemStack stack = p.getItem();
            degradation = (stack.getDamageValue() / (float)stack.getMaxDamage());
        }

        float speed = Math.max(0.2F, ((baseProjectileSpeed + 0.1F) / 1.5F) * (1 - (0.6F * degradation)));
        float inaccuracy = projectileInnacuracy + degradation * 30;
        entity.shoot(x, y, z, speed, inaccuracy);
    }


    @Override
    public SoundEvent getThrowSound(ItemStack stack) {
        return USounds.Vanilla.TRIDENT_THROW.value();
    }
}
