package foundationgames.blasttravel.client;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.client.entity.CannonEntityRenderer;
import foundationgames.blasttravel.client.screen.CannonScreen;
import foundationgames.blasttravel.util.BTNetworking;
import io.github.foundationgames.jsonem.JsonEM;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class BlastTravelClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		EntityRendererRegistry.register(BlastTravel.CANNON, CannonEntityRenderer::new);
		HandledScreens.register(BlastTravel.CANNON_SCREEN_HANDLER, CannonScreen::new);
		BTNetworking.initClient();

		JsonEM.registerModelLayer(CannonEntityRenderer.MODEL);
	}
}
