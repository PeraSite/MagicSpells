package com.nisovin.magicspells.spells.buff;

import java.util.*;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.LivingEntity;
import com.nisovin.magicspells.MagicSpells;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.DamageSpell;
import com.nisovin.magicspells.events.SpellApplyDamageEvent;

public class ResistSpell extends BuffSpell {

	private Map<UUID, Float> buffed;

	private float multiplier;

	private Set<String> spellDamageTypes;
	private Set<DamageCause> normalDamageTypes;

	public ResistSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		multiplier = getConfigFloat("multiplier", 0.5F);

		normalDamageTypes = new HashSet<>();
		List<String> causes = getConfigStringList("normal-damage-types", null);
		if (causes != null) {
			for (String cause : causes) {
				try {
					DamageCause damageCause = DamageCause.valueOf(cause.replace(" ","_").replace("-","_").toUpperCase());
					normalDamageTypes.add(damageCause);
				}
				catch (IllegalArgumentException e) {
					MagicSpells.error("ResistSpell '" + internalName + "' has an invalid damage cause defined '" + cause + "'!");
				}
			}
		}

		spellDamageTypes = new HashSet<>();
		causes = getConfigStringList("spell-damage-types", null);
		if (causes != null) spellDamageTypes.addAll(causes);

		buffed = new HashMap<>();
	}

	@Override
	public boolean castBuff(LivingEntity entity, float power, String[] args) {
		buffed.put(entity.getUniqueId(), power);
		return true;
	}

	@Override
	public boolean isActive(LivingEntity entity) {
		return buffed.containsKey(entity.getUniqueId());
	}

	@Override
	protected void turnOffBuff(LivingEntity entity) {
		buffed.remove(entity.getUniqueId());
	}

	@Override
	protected void turnOff() {
		buffed.clear();
	}
	
	@EventHandler
	public void onSpellDamage(SpellApplyDamageEvent event) {
		if (spellDamageTypes.isEmpty()) return;
		if (!(event.getSpell() instanceof DamageSpell)) return;
		if (!isActive(event.getTarget())) return;

		DamageSpell spell = (DamageSpell) event.getSpell();
		String spellDamageType = spell.getSpellDamageType();
		if (spellDamageType == null) return;
		if (!spellDamageTypes.contains(spellDamageType)) return;

		LivingEntity entity = event.getTarget();

		float power = multiplier;
		if (multiplier < 1) power *= 1 / buffed.get(entity.getUniqueId());
		else if (multiplier > 1) power *= buffed.get(entity.getUniqueId());

		addUseAndChargeCost(entity);
		event.applyDamageModifier(power);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (normalDamageTypes.isEmpty()) return;
		if (!normalDamageTypes.contains(event.getCause())) return;

		Entity entity = event.getEntity();
		if (!(entity instanceof LivingEntity)) return;
		if (!isActive((LivingEntity) entity)) return;

		float mult = multiplier;
		if (multiplier < 1) mult *= 1 / buffed.get(entity.getUniqueId());
		else if (multiplier > 1) mult *= buffed.get(entity.getUniqueId());

		addUseAndChargeCost((LivingEntity) entity);
		event.setDamage(event.getDamage() * mult);
	}

}
