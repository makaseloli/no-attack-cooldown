package io.github.makaseloli.noattackcooldown.mixin;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class PlayerMixin {
    @Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
    private void noattackcooldown$getAttackStrengthScale(float adjustTicks, CallbackInfoReturnable<Float> cir) {
        if (noattackcooldown$hasNoAttackCooldown()) {
            cir.setReturnValue(1.0F);
        }
    }

    @Inject(method = "attack", at = @At("TAIL"))
    private void noattackcooldown$alwaysSweep(Entity target, CallbackInfo ci) {
        Player player = (Player)(Object)this;
        if (player.level().isClientSide || !noattackcooldown$hasNoAttackCooldown() || !(target instanceof LivingEntity)) {
            return;
        }

        DamageSource source = player.damageSources().playerAttack(player);
        for (LivingEntity nearby : player.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(1.0, 0.25, 1.0))) {
            if (nearby != player
                && nearby != target
                && !player.isAlliedTo(nearby)
                && !(nearby instanceof ArmorStand armorStand && armorStand.isMarker())
                && player.distanceToSqr(nearby) < 9.0) {
                nearby.knockback(0.4F, Mth.sin(player.getYRot() * (float)(Math.PI / 180.0)), -Mth.cos(player.getYRot() * (float)(Math.PI / 180.0)));
                nearby.hurt(source, 1.0F);
            }
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
        player.sweepAttack();
    }

    private boolean noattackcooldown$hasNoAttackCooldown() {
        return true;
    }
}
