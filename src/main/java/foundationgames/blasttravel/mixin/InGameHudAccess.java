package foundationgames.blasttravel.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(InGameHud.class)
public interface InGameHudAccess {
	@Invoker("renderSpyglassOverlay")
	void blasttravel$renderSpyglassOverlay(float scale);
}
