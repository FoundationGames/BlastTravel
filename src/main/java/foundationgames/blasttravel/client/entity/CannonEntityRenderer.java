package foundationgames.blasttravel.client.entity;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.CannonEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public class CannonEntityRenderer extends EntityRenderer<CannonEntity> {
	public static final EntityModelLayer MODEL = new EntityModelLayer(BlastTravel.id("cannon"), "main");

	private final ModelPart root;
	private final ModelPart leftWheel;
	private final ModelPart rightWheel;
	private final ModelPart cannon;
	private final ModelPart chains;
	private final ModelPart fuse;
	private final ModelPart playerHead;

	public CannonEntityRenderer(EntityRendererFactory.Context context) {
		super(context);

		this.root = context.getPart(MODEL).getChild("main");

		this.leftWheel = this.root.getChild("left_wheel");
		this.rightWheel = this.root.getChild("right_wheel");
		this.cannon = this.root.getChild("cannon");
		this.playerHead = this.root.getChild("player_head");

		this.chains = this.cannon.getChild("chains");
		this.fuse = this.cannon.getChild("fuse");

		this.resetModel();
	}

	private void resetModel() {
		this.leftWheel.pitch = this.rightWheel.pitch = 0;
		this.cannon.pitch = 0;

		this.cannon.visible = true;
		this.playerHead.visible = false;
		this.fuse.visible = true;
		this.chains.visible = false;
	}

	@Override
	public Identifier getTexture(CannonEntity entity) {
		return entity.getWrapping().texture();
	}

	@Override
	public void render(CannonEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
		matrices.push();

		yaw = (180 + yaw) * MathHelper.RADIANS_PER_DEGREE;

		// 1.2 is the ratio of the circumference of the wheelbase to the circumference of the wheel (probably)
		float wheelAngle = yaw * 1.2f;

		this.leftWheel.pitch = wheelAngle;
		this.rightWheel.pitch = -wheelAngle;
		this.cannon.pitch = (entity.getPitch(tickDelta) + 90) * MathHelper.RADIANS_PER_DEGREE;

		boolean renderCannon = (entity.getPrimaryPassenger() != MinecraftClient.getInstance().player) ||
				MinecraftClient.getInstance().gameRenderer.getCamera().isThirdPerson();

		this.cannon.visible = renderCannon;
		this.chains.visible = entity.hasChains();
		this.fuse.visible = entity.hasFuse();

		matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180));
		matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(yaw));

		this.root.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutout(this.getTexture(entity))), light, OverlayTexture.DEFAULT_UV);

		var player = entity.getClientPlayer();
		if (renderCannon && player != null) {
			this.playerHead.visible = true;
			this.playerHead.pitch = this.cannon.pitch;

			this.playerHead.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(player.getSkinTexture())), light, OverlayTexture.DEFAULT_UV);
		}

		this.resetModel();
		matrices.pop();
	}
}
