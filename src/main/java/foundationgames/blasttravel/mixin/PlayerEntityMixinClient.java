package foundationgames.blasttravel.mixin;

import foundationgames.blasttravel.util.PlayerEntityDuck;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixinClient implements PlayerEntityDuck {
	@Inject(method = "tick", at = @At("TAIL"))
	private void blasttravel$endTick(CallbackInfo ci) {
		var self = (PlayerEntity)(Object)this;
		if (this.blasttravel$inCannonFlight() && self.world.isClient()) {
			MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
					self.prevX, self.prevY, self.prevZ, 0, 0, 0);
		}
	}
}
