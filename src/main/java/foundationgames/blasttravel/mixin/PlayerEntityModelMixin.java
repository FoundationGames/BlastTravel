package foundationgames.blasttravel.mixin;

import foundationgames.blasttravel.util.PlayerEntityDuck;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin<T extends LivingEntity> extends BipedEntityModel<T> {
	@Unique private LivingEntity blasttravel$cached;

	public PlayerEntityModelMixin(ModelPart modelPart) {
		super(modelPart);
	}

	@Inject(method = "setAngles", at = @At("HEAD"))
	private void blasttravel$cacheEntity(T entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
		this.blasttravel$cached = entity;
	}

	@Inject(method = "setAngles", at = @At("TAIL"))
	private void blasttravel$setHeadAngle(T entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
		if (entity instanceof PlayerEntityDuck duck && duck.blasttravel$inCannonFlight()) {
			this.head.setAngles(-0.5f * MathHelper.PI, 0, 0);
			this.hat.setAngles(-0.5f * MathHelper.PI, 0, 0);
		}
	}

	@ModifyVariable(method = "setAngles", at = @At("HEAD"), index = 2)
	private float blasttravel$overrideLimbAngles(float old) {
		if (this.blasttravel$cached instanceof PlayerEntityDuck duck && duck.blasttravel$inCannonFlight()) {
			this.blasttravel$cached = null;
			return 0.75f * MathHelper.PI;
		}
		return old;
	}
}
