package foundationgames.blasttravel.entity.cannon;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.CannonEntity;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ConcretePowderCannonBehavior extends CannonBehavior {
	private static final Identifier TEXTURE = BlastTravel.id("textures/entity/cannon/head/powder.png");
	private static final Map<Item, Vec3f> COLORS = new HashMap<>();

	public ConcretePowderCannonBehavior() {
		super(Items.WHITE_CONCRETE_POWDER,
				s -> s.getItem() instanceof BlockItem item && item.getBlock() instanceof ConcretePowderBlock);
	}

	@Override
	public boolean displayHead(CannonEntity entity) {
		return true;
	}

	private Vec3f color(ItemStack stack) {
		if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof ConcretePowderBlock block) {
			int color = block.getDefaultMapColor().color;

			return COLORS.computeIfAbsent(item, i ->
				new Vec3f((float)((color >> 16) & 0xFF) / 255,
						(float)((color >> 8) & 0xFF) / 255,
						(float)(color & 0xFF) / 255));
		}

		return WHITE;
	}

	@Override
	public void onFired(CannonEntity cannon, ItemStack behaviorStack, Vec3d velocity) {
		if (cannon.world instanceof ServerWorld world) {
			var rot = cannon.getRotationVector();
			var origin = cannon.getPos().add(0, 0.75, 0).add(rot.multiply(1.8));

			world.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, behaviorStack), origin.x, origin.y, origin.z,
					12 + world.random.nextInt(7), 0, 0, 0, 0.17);
		}
	}

	@Override
	public Identifier headTexture(CannonEntity entity) {
		if (!entity.hasPassengers()) {
			return TEXTURE;
		}

		return super.headTexture(entity);
	}

	@Override
	public Vec3f headColor(CannonEntity entity) {
		if (!entity.hasPassengers()) {
			return this.color(entity.getBehaviorStack());
		}

		return super.headColor(entity);
	}

	@Override
	public @Nullable Vec3f fireColor(CannonEntity entity) {
		return this.color(entity.getBehaviorStack());
	}
}
