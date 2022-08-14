package foundationgames.blasttravel.util;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.mixin.InGameHudAccess;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public enum BTUtil {;
	public static final Identifier CANNON_OVERLAY_TEX = BlastTravel.id("textures/misc/cannon_overlay.png");

	public static @Nullable Identifier TEXTURE_OVERRIDE = null;

	public static void renderHudOverlay(InGameHud hud, float scale, Identifier texture) {
		TEXTURE_OVERRIDE = texture;
		((InGameHudAccess)hud).blasttravel$renderSpyglassOverlay(scale);
		TEXTURE_OVERRIDE = null;
	}
}
