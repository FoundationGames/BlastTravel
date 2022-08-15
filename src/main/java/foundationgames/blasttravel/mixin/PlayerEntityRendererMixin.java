package foundationgames.blasttravel.mixin;

import foundationgames.blasttravel.entity.CannonEntity;
import foundationgames.blasttravel.util.PlayerEntityDuck;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
	public PlayerEntityRendererMixin(EntityRendererFactory.Context context, PlayerEntityModel<AbstractClientPlayerEntity> entityModel, float f) {
		super(context, entityModel, f);
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void blasttravel$makePlayersInCannonsInvisible(AbstractClientPlayerEntity player, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
		if (player.getVehicle() instanceof CannonEntity) {
			ci.cancel();
		}
	}

	@Inject(method = "setupTransforms", at = @At("HEAD"), cancellable = true)
	private void blasttravel$modifyPlayerAngles(AbstractClientPlayerEntity player, MatrixStack matrices, float f, float g, float tickDelta, CallbackInfo ci) {
		if (player instanceof PlayerEntityDuck duck && duck.blasttravel$inCannonFlight()) {
			var vel = duck.blasttravel$getVelocityLerped(tickDelta);
			double horizontal = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
			super.setupTransforms(player, matrices, f, 270 + ((float) Math.atan2(vel.z, vel.x) * MathHelper.DEGREES_PER_RADIAN), tickDelta);
			matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion((MathHelper.PI * 1.5f) + (float) Math.atan2(vel.y, horizontal)));

			ci.cancel();
		}
	}
}
