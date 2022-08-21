package foundationgames.blasttravel.entity.cannon;

import foundationgames.blasttravel.entity.CannonEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class EntityCannonBehavior extends CannonBehavior {
	private final EntityFactory entityFactory;

	public EntityCannonBehavior(Item item, Identifier texture, EntityFactory entityFactory) {
		this(item, stack -> stack.isOf(item), texture, entityFactory);
	}

	public EntityCannonBehavior(Item icon, Predicate<ItemStack> filter, Identifier texture, EntityFactory entityFactory) {
		super(icon, filter, texture);
		this.entityFactory = entityFactory;
	}

	@Override
	public boolean occupiesCannon(ItemStack behaviorStack) {
		return true;
	}

	@Override
	public void onFired(CannonEntity cannon, ItemStack behaviorStack, Vec3d velocity) {
		var pos = cannon.getPos().add(0, 0.75, 0).add(cannon.getRotationVector().multiply(1.8));
		var entity = entityFactory.create(cannon.getWorld(), pos, behaviorStack);
		entity.setVelocity(velocity);
		behaviorStack.decrement(1);

		cannon.getWorld().spawnEntity(entity);
	}

	public static Entity tntFactory(World world, Vec3d pos, ItemStack from) {
		return new TntEntity(world, pos.x, pos.y - 0.5, pos.z, null);
	}

	public static Entity fallingBlockFactory(World world, Vec3d pos, ItemStack from) {
		return new FallingBlockEntity(world, pos.x, pos.y - 0.5, pos.z,
				from.getItem() instanceof BlockItem block ? block.getBlock().getDefaultState() : Blocks.AIR.getDefaultState());
	}

	@FunctionalInterface
	public interface EntityFactory {
		Entity create(World world, Vec3d pos, ItemStack spawnedFrom);
	}
}
