package foundationgames.blasttravel.mixin;

import foundationgames.blasttravel.entity.CannonEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow @Final private MinecraftClient client;
	@Unique private Entity blasttravel$cachedMount = null;

	@Inject(method = "onEntityPassengersSet", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
			at = @At(value = "INVOKE_ASSIGN", ordinal = 0, shift = At.Shift.AFTER, target = "Lnet/minecraft/client/world/ClientWorld;getEntityById(I)Lnet/minecraft/entity/Entity;"))
	private void blasttravel$cacheMountedEntity(EntityPassengersSetS2CPacket packet, CallbackInfo ci, Entity mounted) {
		blasttravel$cachedMount = mounted;
	}

	@ModifyVariable(method = "onEntityPassengersSet", index = 9,
			at = @At(value = "INVOKE_ASSIGN", ordinal = 0, shift = At.Shift.AFTER, target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"))
	private Text blasttravel$modifyMountMessage(Text old) {
		if (blasttravel$cachedMount instanceof CannonEntity) {
			return Text.translatable("mount.blasttravel.cannon.onboard",
					this.client.options.sneakKey.getKeyName(), this.client.options.jumpKey.getKeyName());
		}
		return old;
	}
}
