package foundationgames.blasttravel.mixin;

import foundationgames.blasttravel.entity.CannonPlayerDamageSource;
import foundationgames.blasttravel.util.PlayerEntityDuck;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityDuck {
	private static final EntityDimensions CANNON_FLIGHT_DIMENSIONS = EntityDimensions.changing(0.6f, 0.6f);
	private Vec3d blasttravel$vel = Vec3d.ZERO;
	private Vec3d blasttravel$trackingVel = Vec3d.ZERO;
	private Vec3d blasttravel$prevVel = Vec3d.ZERO;

	private boolean blasttravel$inCannonFlight = false;
	private boolean blasttravel$cancelFallDamage = false;

	@Inject(method = "tick", at = @At("HEAD"))
	private void blasttravel$beginTick(CallbackInfo ci) {
		this.blasttravel$prevVel = ((PlayerEntity)(Object)this).isMainPlayer() ? blasttravel$vel : blasttravel$trackingVel;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void blasttravel$endTick(CallbackInfo ci) {
		var self = (PlayerEntity)(Object)this;
		this.blasttravel$vel = self.getPos().subtract(self.prevX, self.prevY, self.prevZ);

		if (this.blasttravel$inCannonFlight()) {
			 if (!self.world.isClient()) {
				var vel = self.getVelocity();
				var frontBox = self.getBoundingBox().stretch(0.2, 0.2, 0.2);
				for (var entity : self.world.getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), frontBox, entity -> entity != self)) {
					if (!entity.isInvulnerable()) {
						entity.damage(new CannonPlayerDamageSource(self), (float)(vel.length() * 4));
					}
				}
			}

			if ((self.isOnGround() || self.isFallFlying() || self.getAbilities().flying || self.isSubmergedInWater())) {
				this.blasttravel$setCannonFlight(false);
				if (self.isOnGround()) {
					self.world.playSound(null, self.getX(), self.getY(), self.getZ(),
							SoundEvents.ENTITY_GENERIC_SMALL_FALL, SoundCategory.PLAYERS, 1, 0.78f);
				}
			}
		}

		if (!((PlayerEntity)(Object)this).isMainPlayer()) {
			this.blasttravel$trackingVel = this.blasttravel$trackingVel.add(
					this.blasttravel$vel.subtract(this.blasttravel$trackingVel).multiply(1f / self.getType().getTrackTickInterval()));
		}
	}

	@Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
	private void blasttravel$cancelFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
		if (this.blasttravel$cancelFallDamage) {
			this.blasttravel$cancelFallDamage = false;
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
	private void blasttravel$setFlyingPose(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
		if (this.blasttravel$inCannonFlight()) {
			cir.setReturnValue(CANNON_FLIGHT_DIMENSIONS);
		}
	}

	@Inject(method = "getActiveEyeHeight", at = @At("HEAD"), cancellable = true)
	private void blasttravel$setFlyingEyeHeight(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
		if (this.blasttravel$inCannonFlight()) {
			cir.setReturnValue(dimensions.height * 0.5f);
		}
	}

	@Override
	public void blasttravel$setCannonFlight(boolean inFlight) {
		if (inFlight && !this.blasttravel$inCannonFlight) {
			this.blasttravel$vel =
					this.blasttravel$trackingVel =
							this.blasttravel$prevVel = ((PlayerEntity)(Object)this).getVelocity();
			this.blasttravel$cancelFallDamage = true;
		}

		this.blasttravel$inCannonFlight = inFlight;
		((LivingEntityAccess)this).blasttravel$setNoDrag(inFlight);

		((PlayerEntity)(Object)this).calculateDimensions();
	}

	@Override
	public boolean blasttravel$inCannonFlight() {
		return this.blasttravel$inCannonFlight;
	}

	@Override
	public Vec3d blasttravel$getVelocityLerped(float delta) {
		var vel = ((PlayerEntity)(Object)this).isMainPlayer() ? blasttravel$vel : blasttravel$trackingVel;
		return new Vec3d(
				MathHelper.lerp(delta, blasttravel$prevVel.x, vel.x),
				MathHelper.lerp(delta, blasttravel$prevVel.y, vel.y),
				MathHelper.lerp(delta, blasttravel$prevVel.z, vel.z));
	}
}
