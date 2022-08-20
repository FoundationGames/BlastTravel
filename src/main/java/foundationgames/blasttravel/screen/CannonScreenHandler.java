package foundationgames.blasttravel.screen;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.cannon.CannonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.function.Predicate;

public class CannonScreenHandler extends ScreenHandler {
	public final Inventory inventory;

	public CannonScreenHandler(int syncId, PlayerInventory playerInv) {
		this(syncId, playerInv, new SimpleInventory(3));
	}

	public CannonScreenHandler(int syncId, PlayerInventory playerInv, Inventory inv) {
		super(BlastTravel.CANNON_SCREEN_HANDLER, syncId);

		checkSize(inv, 3);
		this.inventory = inv;
		inv.onOpen(playerInv.player);

		this.addSlot(new FilterSlot(inv, 0, 62, 20, stack -> stack.isOf(Items.GUNPOWDER)));
		this.addSlot(new FilterSlot(inv, 1, 80, 20, stack -> stack.isOf(Items.CHAIN)));
		this.addSlot(new FilterSlot(inv, 2, 98, 20, CannonBehavior::isValidBehaviorStack));

		int row;
		int col;
		for(row = 0; row < 3; ++row) {
			for(col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 58 + row * 18));
			}
		}
		for(row = 0; row < 9; ++row) {
			this.addSlot(new Slot(playerInv, row, 8 + row * 18, 116));
		}
	}

	@Override
	public ItemStack transferSlot(PlayerEntity player, int fromSlotId) {
		var newStack = ItemStack.EMPTY;
		var fromSlot = this.slots.get(fromSlotId);

		if (fromSlot.hasStack()) {
			var fromStack = fromSlot.getStack();
			newStack = fromStack.copy();
			if (fromSlotId >= 0 && fromSlotId < 3) {
				if (!this.insertItem(fromStack, 3, 39, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.insertItem(fromStack, 0, 3, false)) {
				return ItemStack.EMPTY;
			}

			if (fromStack.isEmpty()) {
				fromSlot.setStack(ItemStack.EMPTY);
			} else {
				fromSlot.markDirty();
			}
		}

		return newStack;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	public static class FilterSlot extends Slot {
		private final Predicate<ItemStack> filter;

		public FilterSlot(Inventory inventory, int id, int x, int y, Predicate<ItemStack> filter) {
			super(inventory, id, x, y);
			this.filter = filter;
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return super.canInsert(stack) && filter.test(stack);
		}
	}
}
