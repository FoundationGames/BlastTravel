package foundationgames.blasttravel.util;

import net.minecraft.util.math.Vec3d;

public interface PlayerEntityDuck {
	void blasttravel$setCannonFlight(boolean inFlight);

	boolean blasttravel$inCannonFlight();

	Vec3d blasttravel$getVelocityLerped(float delta);
}
