package foundationgames.blasttravel.mixin;

import foundationgames.blasttravel.entity.CannonEntity;
import foundationgames.blasttravel.util.PlayerEntityDuck;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin implements PlayerEntityDuck {
	@Shadow public Input input;

	@Inject(method = "tickRiding", at = @At("TAIL"))
	private void blasttravel$handleCannonInput(CallbackInfo ci) {
		if (((ClientPlayerEntity)(Object)this).getVehicle() instanceof CannonEntity cannon) {
			cannon.handleInput(this.input.jumping);
		}
	}

	@Inject(method = "tickNewAi", at = @At("TAIL"))
	private void blasttravel$preserveHSpeedInFlight(CallbackInfo ci) {
		if (this.blasttravel$inCannonFlight()) {
			var self = (ClientPlayerEntity)(Object)this;
			self.sidewaysSpeed = 0;
			self.forwardSpeed = 0;
		}
	}
}
