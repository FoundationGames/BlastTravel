package foundationgames.blasttravel.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.text.Text;

public class CannonPlayerDamageSource extends ProjectileDamageSource {
	public CannonPlayerDamageSource(Entity attacker) {
		super("cannon", attacker, attacker);
	}

	@Override
	public Text getDeathMessage(LivingEntity entity) {
		return Text.translatable("death.attack.blasttravel.cannon", entity.getDisplayName(), this.getSource().getDisplayName());
	}
}
