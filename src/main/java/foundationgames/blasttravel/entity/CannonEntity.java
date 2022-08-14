package foundationgames.blasttravel.entity;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.screen.CannonScreenHandler;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CannonEntity extends Entity {
	private static final List<Wrapping> ID_TO_WRAPPING = new ArrayList<>();
	private static final Object2IntMap<Item> ITEM_TO_WRAPPING = new Object2IntOpenHashMap<>();

	public static final Text UI_TITLE = Text.translatable("container.blasttravel.cannon");

	public static final Wrapping NONE = new Wrapping(Items.AIR, BlastTravel.id("textures/entity/cannon/regular.png")).register();
	public static final Wrapping MOSS = new Wrapping(Items.MOSS_BLOCK, BlastTravel.id("textures/entity/cannon/mossy.png")).register();
	public static final Wrapping AMETHYST = new Wrapping(Items.AMETHYST_BLOCK, BlastTravel.id("textures/entity/cannon/amethyst.png")).register();

	public static final TrackedData<Integer> WRAPPING = DataTracker.registerData(CannonEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Boolean> CHAINED = DataTracker.registerData(CannonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	private boolean chained;

	private final SimpleInventory inventory = new SimpleInventory(3) {
		@Override
		public void setStack(int slot, ItemStack stack) {
			super.setStack(slot, stack);
			CannonEntity.this.updateStateFromInventory();
		}
	};

	public CannonEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	public CannonEntity(World world) {
		this(BlastTravel.CANNON, world);
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.chained && this.getPrimaryPassenger() instanceof PlayerEntity player) {
			this.setYaw(player.getHeadYaw());
			this.setPitch(player.getPitch());
		}

		if (this.dataTracker.get(CHAINED) != this.chained) {
			if (!world.isClient()) {
				this.dataTracker.set(CHAINED, this.chained);
			} else {
				this.chained = this.dataTracker.get(CHAINED);
			}
		}
	}

	@Override
	public void onPassengerLookAround(Entity passenger) {
		if (this.world.isClient() && passenger instanceof PlayerEntity player && player.isMainPlayer()) {
			if (chained) {
				player.setYaw(this.getYaw());
				player.setPitch(this.getPitch());
			} else {
				player.setPitch(Math.min(18, player.getPitch()));
			}
		}
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (player == this.getPrimaryPassenger()) {
			return super.interact(player, hand);
		}

		if (player.isSneaking()) {
			if (!world.isClient()) {
				player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInv, user) ->
						new CannonScreenHandler(syncId, playerInv, this.inventory), UI_TITLE));
				return ActionResult.PASS;
			}
			return ActionResult.SUCCESS;
		}

		if (!this.hasPassengers()) {
			if (!world.isClient()) {
				player.setYaw(this.getYaw());
				player.setPitch(this.getPitch());
				player.startRiding(this);

				return ActionResult.PASS;
			}
			return ActionResult.SUCCESS;
		}

		return super.interact(player, hand);
	}

	@Override
	public boolean handleAttack(Entity attacker) {
		if (attacker instanceof PlayerEntity player) {
			if (player.canModifyBlocks() &&
					(player.isCreative() || player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof PickaxeItem)) {
				if (!this.world.isClient()) {
					ItemScatterer.spawn(this.world, this.getBlockPos(), this.inventory);
					if (!player.isCreative()) {
						ItemScatterer.spawn(this.world, this.getX(), this.getY(), this.getZ(), new ItemStack(BlastTravel.CANNON_ITEM));
					}

					this.remove(RemovalReason.KILLED);
				}
				return true;
			}
		}

		return super.handleAttack(attacker);
	}

	protected int getWrappingId() {
		return this.dataTracker.get(WRAPPING);
	}

	protected void setWrappingId(int id) {
		this.dataTracker.set(WRAPPING, id);
	}

	public boolean hasFuse() {
		return this.hasPassengers();
	}

	public boolean hasChains() {
		return this.chained;
	}

	public static boolean isValidWrappingStack(ItemStack stack) {
		return ITEM_TO_WRAPPING.containsKey(stack.getItem());
	}

	@Environment(EnvType.CLIENT)
	public @Nullable AbstractClientPlayerEntity getClientPlayer() {
		return this.getPrimaryPassenger() instanceof AbstractClientPlayerEntity player ? player : null;
	}

	public Wrapping getWrapping() {
		return ID_TO_WRAPPING.get(getWrappingId());
	}

	protected void updateStateFromInventory() {
		if (!this.world.isClient()) {
			for (int slot = 0; slot < this.inventory.size(); slot++) {
				var item = this.inventory.getStack(slot).getItem();
				if (slot == 1) {
					this.chained = item == Items.CHAIN;
				} else if (slot == 2) {
					this.setWrappingId(ITEM_TO_WRAPPING.getOrDefault(item, 0));
				}
			}
		}
	}

	@Nullable
	@Override
	public Entity getPrimaryPassenger() {
		return this.getFirstPassenger();
	}

	@Override
	public boolean collides() {
		return !this.isRemoved();
	}

	@Override
	public boolean collidesWith(Entity other) {
		return (other.isCollidable() || other.isPushable()) && !this.isConnectedThroughVehicle(other);
	}

	@Override
	public boolean isCollidable() {
		return true;
	}

	@Override
	public double getMountedHeightOffset() {
		if (this.hasPassengers()) {
			var passenger = this.getPrimaryPassenger();
			return (-passenger.getHeightOffset() - passenger.getEyeHeight(passenger.getPose())) + 0.75f;
		}
		return super.getMountedHeightOffset();
	}

	@Override
	protected void initDataTracker() {
		this.dataTracker.startTracking(WRAPPING, 0);
		this.dataTracker.startTracking(CHAINED, false);
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		Inventories.readNbt(nbt.getCompound("Items"), this.inventory.stacks);
		this.updateStateFromInventory();
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		var inv = new NbtCompound();
		Inventories.writeNbt(inv, this.inventory.stacks);
		nbt.put("Items", inv);
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

	public record Wrapping(Item filter, Identifier texture) {
		public Wrapping register() {
			ITEM_TO_WRAPPING.put(this.filter, ID_TO_WRAPPING.size());
			ID_TO_WRAPPING.add(this);

			return this;
		}
	}
}