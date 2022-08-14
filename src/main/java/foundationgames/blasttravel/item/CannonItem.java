package foundationgames.blasttravel.item;

import foundationgames.blasttravel.BlastTravel;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;

public class CannonItem extends Item {
	public CannonItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (context.getWorld() instanceof ServerWorld world) {
			BlastTravel.CANNON.spawnFromItemStack(world, context.getStack(), context.getPlayer(), context.getBlockPos().offset(context.getSide()), SpawnReason.SPAWN_EGG, true, false);
			if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
				context.getStack().decrement(1);
			}
		}

		return ActionResult.success(context.getWorld().isClient());
	}
}
