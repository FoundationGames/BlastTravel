package foundationgames.blasttravel.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.cannon.CannonBehavior;
import foundationgames.blasttravel.screen.CannonScreenHandler;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class CannonScreen extends HandledScreen<CannonScreenHandler> {
	private static final Identifier TEXTURE = BlastTravel.id("textures/gui/container/cannon.png");

	private final ItemStack[] allowedBehaviorStacks = CannonBehavior.allBehaviors().stream()
			.filter(b -> b.icon != Items.AIR).map(b -> new ItemStack(b.icon)).toArray(ItemStack[]::new);

	private long time = (long) (Math.random() * 1000);

	public CannonScreen(CannonScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);

		this.backgroundHeight = 140;
		this.playerInventoryTitleY = 47;
		this.titleY = 8;
		this.titleX = 61;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		this.renderBackground(matrices);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, TEXTURE);
		this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

		if (this.getScreenHandler().inventory.getStack(2).isEmpty()) {
			int x = this.x + 98; int y = this.y + 20;
			this.itemRenderer.renderInGui(
					this.allowedBehaviorStacks[MathHelper.floor((float)this.time / 30) % this.allowedBehaviorStacks.length], x, y);

			RenderSystem.depthFunc(516);
			DrawableHelper.fill(matrices, x, y, x + 16, y + 16, 0x8B8B8B8B);
			RenderSystem.depthFunc(515);
		}

		RenderSystem.setShaderTexture(0, TEXTURE);
		for (int i = 0; i < 2; i++) {
			if (this.getScreenHandler().inventory.getStack(i).isEmpty()) {
				this.drawTexture(matrices, this.x + 62 + (18 * i), this.y + 20, 16 * i, 140, 16, 16);
			}
		}
	}

	@Override
	protected void handledScreenTick() {
		this.time++;
	}
}
