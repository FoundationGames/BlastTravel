package foundationgames.blasttravel.entity;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.cannon.CannonBehavior;
import foundationgames.blasttravel.entity.cannon.ConcretePowderCannonBehavior;
import foundationgames.blasttravel.entity.cannon.EntityCannonBehavior;
import foundationgames.blasttravel.screen.CannonScreenHandler;
import foundationgames.blasttravel.util.BTNetworking;
import foundationgames.blasttravel.util.PlayerEntityDuck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CannonEntity extends Entity {
	public static final Text UI_TITLE = Text.translatable("container.blasttravel.cannon");
	public static final Text NO_GUNPOWDER_DIALOG = Text.translatable("dialog.blasttravel.no_gunpowder").formatted(Formatting.RED);
	public static final Text FULL_CANNON_DIALOG = Text.translatable("dialog.blasttravel.full_cannon").formatted(Formatting.RED);

	public static final CannonBehavior NONE = new CannonBehavior(Items.AIR, stack -> false).register();
	public static final CannonBehavior GOLDEN = new CannonBehavior(Items.GOLD_BLOCK, BlastTravel.id("textures/entity/cannon/golden.png")).register();
	public static final CannonBehavior MOSSY = new CannonBehavior(Items.MOSS_BLOCK, BlastTravel.id("textures/entity/cannon/mossy.png")).register();
	public static final CannonBehavior LAZULI = new CannonBehavior(Items.LAPIS_BLOCK, BlastTravel.id("textures/entity/cannon/lazuli.png")).register();
	public static final CannonBehavior AMETHYST = new CannonBehavior(Items.AMETHYST_BLOCK, BlastTravel.id("textures/entity/cannon/amethyst.png")).register();
	public static final CannonBehavior TNT = new EntityCannonBehavior(Items.TNT, BlastTravel.id("textures/entity/cannon/tnt.png"), EntityCannonBehavior::tntFactory).register();
	public static final CannonBehavior ANVIL = new EntityCannonBehavior(Items.ANVIL, s -> s.isIn(ItemTags.ANVIL), BlastTravel.id("textures/entity/cannon/anvil.png"), EntityCannonBehavior::fallingBlockFactory).register();
	public static final CannonBehavior POWDER = new ConcretePowderCannonBehavior().register();

	public static final TrackedData<Integer> BEHAVIOR = DataTracker.registerData(CannonEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Boolean> CHAINED = DataTracker.registerData(CannonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static final TrackedData<ItemStack> BEHAVIOR_STACK = DataTracker.registerData(CannonEntity.class, BlastTravel.ITEM_STACK_HANDLER);

	public static final int MAX_ANIMATION = 12;

	private boolean chained;
	private boolean firing;
	private boolean powered;
	private boolean alwaysModifiable;

	private int animation = 0;

	private double targetX;
	private double targetY;
	private double targetZ;
	private int targetTicks;

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
		if (this.firing && !this.hasPassengers()) {
			this.firing = false;
		}

		super.tick();

		if (!this.chained && this.getFirstPassenger() instanceof PlayerEntity player) {
			this.setYaw(player.getHeadYaw());
			this.setPitch(player.getPitch());
		}

		if (this.dataTracker.get(CHAINED) != this.chained) {
			if (!world.isClient()) {
				this.dataTracker.set(CHAINED, this.chained);
			} else {
				setChained(this.dataTracker.get(CHAINED));
			}
		}

		if (this.animation > 0) {
			this.animation--;
		}

		if (this.world.isClient()) {
			if (this.hasFuse()) {
				var pos = this.getPos().add(0, 0.75, 0).add(this.getRotationVector(this.getPitch() - 90, this.getYaw()).multiply(0.75));
				MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.SMOKE,
						pos.x, pos.y, pos.z, 0, 0, 0);
			}

			this.positionTrackTick();
		} else {
			boolean hasPower = this.world.getReceivedRedstonePower(this.getBlockPos()) > 0 ||
					this.world.getReceivedRedstonePower(this.getBlockPos().down()) > 0;
			if (hasPower != this.powered) {
				if (hasPower) {
					this.fireServer();
				}
				this.powered = hasPower;
			}

			this.movementTick();
			this.move(MovementType.SELF, this.getVelocity());
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

	public boolean canPlayerModify(PlayerEntity player) {
		return this.alwaysModifiable || player.canModifyBlocks();
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (player == this.getFirstPassenger()) {
			return super.interact(player, hand);
		}

		if (player.isSneaking()) {
			if (!world.isClient()) {
				if (this.canPlayerModify(player)) {
					player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInv, user) ->
							new CannonScreenHandler(syncId, playerInv, this.inventory), UI_TITLE));
				} else {
					world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 0.5f, 1.5f);
				}
				return ActionResult.PASS;
			}
			return ActionResult.SUCCESS;
		}

		if (!this.hasPassengers() && !this.getBehavior().occupiesCannon(this.inventory.getStack(2))) {
			if (!world.isClient()) {
				player.setYaw(this.getYaw());
				player.setPitch(this.getPitch());
				player.startRiding(this);

				return ActionResult.PASS;
			}
			return ActionResult.SUCCESS;
		} else {
			player.sendMessage(FULL_CANNON_DIALOG, true);
		}

		return super.interact(player, hand);
	}

	@Override
	public boolean handleAttack(Entity attacker) {
		if (attacker instanceof PlayerEntity player && player != this.getFirstPassenger()) {
			if (player.canModifyBlocks() &&
					(player.isCreative() || player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof PickaxeItem)) {
				if (!this.world.isClient()) {
					ItemScatterer.spawn(this.world, this.getBlockPos(), this.inventory);
					if (!player.isCreative()) {
						ItemScatterer.spawn(this.world, this.getX(), this.getY(), this.getZ(), new ItemStack(BlastTravel.CANNON_ITEM));
					}

					this.remove(RemovalReason.KILLED);
				}
				this.world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS,
						1, 0.8f);
				this.world.addBlockBreakParticles(this.getBlockPos(), Blocks.ANVIL.getDefaultState());

				return true;
			}
		}

		this.world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS,
				1, 0.5f);
		return true;
	}

	@Override
	public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
		if (!this.world.isClient()) {
			this.setPosition(x, y, z);

			if (!this.hasPassengers()) {
				this.setRotation(yaw, pitch);
			}
		} else {
			this.targetX = x;
			this.targetY = y;
			this.targetZ = z;
			this.targetTicks = this.getType().getTrackTickInterval();
		}
	}

	private void positionTrackTick() {
		if (this.targetTicks > 0) {
			this.setPosition(
					this.getX() + (this.targetX - this.getX()) / (double)this.targetTicks,
					this.getY() + (this.targetY - this.getY()) / (double)this.targetTicks,
					this.getZ() + (this.targetZ - this.getZ()) / (double)this.targetTicks
			);

			this.targetTicks--;
		}
	}

	private void movementTick() {
		var vel = this.getVelocity();

		this.setVelocity(new Vec3d(vel.x * 0.9, this.isOnGround() ? 0 : Math.max(vel.y - 0.07, -0.7), vel.z * 0.9));
		this.velocityModified = true;
	}

	public ItemStack getBehaviorStack() {
		if (!this.world.isClient()) {
			return this.inventory.getStack(2);
		}

		return this.dataTracker.get(BEHAVIOR_STACK);
	}

	public void handleInput(boolean firing) {
		if (this.world.isClient()) {
			if (firing && !this.firing) {
				BTNetworking.c2sRequestFire(this);
			}
			this.firing = firing;
		}
	}

	public void fireServer() {
		if (this.world instanceof ServerWorld world) {
			var gunpowder = this.inventory.getStack(0);
			if (gunpowder.isOf(Items.GUNPOWDER) && gunpowder.getCount() > 0) {
				PlayerEntity firedPlayer = null;
				var behaviorStack = this.getBehaviorStack();
				var vel = getVelocity().add(getRotationVector().multiply(Math.sqrt(gunpowder.getCount()) * 0.6));

				this.getBehavior().onFired(this, behaviorStack, vel);
				if (this.getFirstPassenger() instanceof PlayerEntity player) {
					player.stopRiding();
					player.setVelocity(vel);
					((PlayerEntityDuck)player).blasttravel$setCannonFlight(true);
					firedPlayer = player;
				}

				this.world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1, 1);
				for (var to : world.getPlayers()) {
					BTNetworking.s2cFireCannon(to, this, firedPlayer, vel);
				}

				this.updateStateFromInventory();
			} else {
				if (this.getFirstPassenger() instanceof PlayerEntity player) {
					player.stopRiding();
					player.sendMessage(NO_GUNPOWDER_DIALOG, true);
				}

				this.world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1, 0.8f);
			}
		}
	}

	public void fireClient() {
		if (!world.isClient()) {
			return;
		}

		this.animate();

		final int ringParticles = 18;
		for (int i = 0; i < ringParticles; i++) {
			double angle = (2d / ringParticles) * Math.PI * i;
			var arc = new Vec3d(Math.sin(angle), Math.cos(angle), 0)
					.rotateX(-this.getPitch() * MathHelper.RADIANS_PER_DEGREE)
					.rotateY(-this.getYaw() * MathHelper.RADIANS_PER_DEGREE);

			var pos = this.getPos()
					.add(0, 0.75, 0)
					.add(this.getRotationVector(this.getPitch() - 4, this.getYaw())
							.multiply(1.69f)).add(arc.multiply(0.15f));
			var vel = arc.multiply(0.14);

			MinecraftClient.getInstance().particleManager.addParticle(BlastTravel.CANNON_BLAST,
					pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
		}
	}

	public void animate() {
		this.animation = MAX_ANIMATION;
	}

	protected int getBehaviorId() {
		return this.dataTracker.get(BEHAVIOR);
	}

	protected void setBehaviorId(int id) {
		this.dataTracker.set(BEHAVIOR, id);
	}

	public boolean hasFuse() {
		return this.hasPassengers() || this.getBehavior().occupiesCannon(this.inventory.getStack(2));
	}

	public boolean hasChains() {
		return this.chained;
	}

	public float getAnimation(float tickDelta) {
		float anim = Math.max(0, this.animation - tickDelta);
		return anim / MAX_ANIMATION;
	}

	public int getAnimationTick() {
		return this.animation;
	}

	private void setChained(boolean chained) {
		if (chained != this.chained) {
			this.world.playSound(null, this.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_CHAIN,
					SoundCategory.BLOCKS, 1, 1.2f);
		}

		this.chained = chained;
	}

	@Environment(EnvType.CLIENT)
	public @Nullable AbstractClientPlayerEntity getClientPlayer() {
		return this.getFirstPassenger() instanceof AbstractClientPlayerEntity player ? player : null;
	}

	public CannonBehavior getBehavior() {
		return CannonBehavior.byId(getBehaviorId());
	}

	protected void updateStateFromInventory() {
		if (!this.world.isClient()) {
			for (int slot = 0; slot < this.inventory.size(); slot++) {
				var stack = this.inventory.getStack(slot);
				if (slot == 1) {
					setChained(stack.isOf(Items.CHAIN));
				} else if (slot == 2) {
					this.setBehaviorId(CannonBehavior.idForStack(stack));
					this.dataTracker.set(BEHAVIOR_STACK, stack);
				}
			}
		}
	}

	@Nullable
	@Override
	public ItemStack getPickBlockStack() {
		return new ItemStack(BlastTravel.CANNON_ITEM);
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
			var passenger = this.getFirstPassenger();
			return (-passenger.getHeightOffset() - passenger.getEyeHeight(passenger.getPose())) + 0.75f;
		}
		return super.getMountedHeightOffset();
	}

	@Override
	protected void initDataTracker() {
		this.dataTracker.startTracking(BEHAVIOR, 0);
		this.dataTracker.startTracking(CHAINED, false);
		this.dataTracker.startTracking(BEHAVIOR_STACK, ItemStack.EMPTY);
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		Inventories.readNbt(nbt.getCompound("Items"), this.inventory.stacks);
		this.powered = nbt.getBoolean("powered");
		this.alwaysModifiable = nbt.getBoolean("alwaysModifiable");

		this.updateStateFromInventory();
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		var inv = new NbtCompound();
		Inventories.writeNbt(inv, this.inventory.stacks);
		nbt.put("Items", inv);
		nbt.putBoolean("powered", this.powered);
		nbt.putBoolean("alwaysModifiable", this.alwaysModifiable);
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}
}
