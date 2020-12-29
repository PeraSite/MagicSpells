package com.nisovin.magicspells.spells.targeted;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;

public class CloseInventorySpell extends TargetedSpell implements TargetedEntitySpell {

	private final int delay;

	public CloseInventorySpell(MagicConfig config, String spellName) {
		super(config, spellName);
		delay = getConfigInt("delay", 0);
	}

	@Override
	public PostCastAction castSpell(LivingEntity livingEntity, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			TargetInfo<Player> targetInfo = getTargetedPlayer(livingEntity, power);
			if (targetInfo == null) return noTarget(livingEntity);
			Player target = targetInfo.getTarget();
			if (target == null) return noTarget(livingEntity);
			close(target);
			playSpellEffects(livingEntity, target);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
		return close(target);
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		return close(target);
	}

	private boolean close(LivingEntity target) {
		if (!(target instanceof Player)) return false;
		close((Player) target);
		return true;
	}

	private void close(Player target) {
		if (delay > 0) MagicSpells.scheduleDelayedTask(target::closeInventory, delay);
		else target.closeInventory();
	}

}
