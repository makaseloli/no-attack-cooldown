package io.github.makaseloli.noattackcooldown.mixin;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class PlayerMixin {
    @Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
    private void noattackcooldown$getAttackStrengthScale(float adjustTicks, CallbackInfoReturnable<Float> cir) {
        if (noattackcooldown$isHoldingSword()) {
            cir.setReturnValue(1.0F);
        }
    }

    @Inject(method = "isSweepAttack", at = @At("HEAD"), cancellable = true)
    private void noattackcooldown$isSweepAttack(boolean fullStrengthAttack, boolean criticalAttack, boolean knockbackAttack, CallbackInfoReturnable<Boolean> cir) {
        if (noattackcooldown$isHoldingSword()) {
            cir.setReturnValue(true);
        }
    }

    private boolean noattackcooldown$isHoldingSword() {
        return ((Player)(Object)this).getMainHandItem().is(ItemTags.SWORDS);
    }
}
