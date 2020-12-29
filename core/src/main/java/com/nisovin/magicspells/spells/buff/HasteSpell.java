package com.nisovin.magicspells.spells.buff;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spelleffects.EffectPosition;

import org.apache.commons.math3.util.FastMath;

public class HasteSpell extends BuffSpell {

	private Map<UUID, HasteData> hasted;

	private int strength;
	private int boostDuration;
	private int accelerationDelay;
	private int accelerationAmount;
	private int accelerationIncrease;
	private int accelerationInterval;

	private boolean hidden;
	private boolean acceleration;

	public HasteSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		strength = getConfigInt("effect-strength", 3);
		boostDuration = getConfigInt("boost-duration", 300);
		accelerationDelay = getConfigInt("acceleration-delay", 0);
		accelerationAmount = getConfigInt("acceleration-amount", 0);
		accelerationIncrease = getConfigInt("acceleration-increase", 0);
		accelerationInterval = getConfigInt("acceleration-interval", 0);

		hidden = getConfigBoolean("hidden", false);
		if (accelerationDelay >= 0 && accelerationAmount > 0 && accelerationIncrease > 0 && accelerationInterval > 0) acceleration = true;

		hasted = new HashMap<>();
	}

	@Override
	public boolean castBuff(LivingEntity entity, float power, String[] args) {
		hasted.put(entity.getUniqueId(), new HasteData(FastMath.round(strength * power)));
		return true;
	}

	@Override
	public boolean isActive(LivingEntity entity) {
		return hasted.containsKey(entity.getUniqueId());
	}

	@Override
	public void turnOffBuff(LivingEntity entity) {
		HasteData data = hasted.get(entity.getUniqueId());
		if (data == null) return;
		MagicSpells.cancelTask(data.task);
		hasted.remove(entity.getUniqueId());
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1, 0));
		entity.removePotionEffect(PotionEffectType.SPEED);
	}

	@Override
	protected void turnOff() {
		for (UUID id : hasted.keySet()) {
			Entity e = Bukkit.getEntity(id);
			if (!(e instanceof LivingEntity)) continue;
			LivingEntity livingEntity = (LivingEntity) e;
			livingEntity.removePotionEffect(PotionEffectType.SPEED);
			HasteData data = hasted.get(id);
			if (data != null) MagicSpells.cancelTask(data.task);
		}

		hasted.clear();
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
		Player pl = event.getPlayer();
		if (!isActive(pl)) return;

		if (isExpired(pl)) {
			turnOff(pl);
			return;
		}

		HasteData data = hasted.get(pl.getUniqueId());
		int amplifier = data.strength;

		if (event.isSprinting()) {
			event.setCancelled(true);
			addUseAndChargeCost(pl);
			playSpellEffects(EffectPosition.CASTER, pl);
			pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, boostDuration, amplifier, false, !hidden));
			if (acceleration) {
				data.task = MagicSpells.scheduleRepeatingTask(() -> {
					if (data.count >= accelerationAmount) {
						MagicSpells.cancelTask(data.task);
						return;
					}
					data.count++;
					pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, boostDuration, amplifier + (data.count * accelerationIncrease), false, !hidden));
				}, accelerationDelay, accelerationInterval);
			}
		} else {
			pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1, 0, false, !hidden));
			pl.removePotionEffect(PotionEffectType.SPEED);
			playSpellEffects(EffectPosition.DISABLED, pl);
			MagicSpells.cancelTask(data.task);
			data.count = 0;
		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		Player pl = event.getPlayer();
		if (!isActive(pl)) return;
		if (!pl.isSprinting()) return;
		pl.removePotionEffect(PotionEffectType.SPEED);

		HasteData data = hasted.get(pl.getUniqueId());
		if (data == null) return;
		MagicSpells.cancelTask(data.task);
	}

	private static class HasteData {

		private int task;
		private int count;
		private int strength;

		private HasteData(int strength) {
			this.strength = strength;
		}

	}

}
