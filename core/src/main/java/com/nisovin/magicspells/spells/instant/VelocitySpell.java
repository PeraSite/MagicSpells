package com.nisovin.magicspells.spells.instant;

import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.spelleffects.EffectPosition;

public class VelocitySpell extends InstantSpell {

	private final Set<UUID> jumping;

	private final double speed;
	private final boolean cancelDamage;
	private final boolean addVelocityInstead;

	public VelocitySpell(MagicConfig config, String spellName) {
		super(config, spellName);

		jumping = new HashSet<>();

		speed = getConfigFloat("speed", 40) / 10F;
		cancelDamage = getConfigBoolean("cancel-damage", true);
		addVelocityInstead = getConfigBoolean("add-velocity-instead", false);
	}

	@Override
	public PostCastAction castSpell(LivingEntity livingEntity, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Vector v = livingEntity.getEyeLocation().getDirection().normalize().multiply(speed * power);
			if (addVelocityInstead) livingEntity.setVelocity(livingEntity.getVelocity().add(v));
			else livingEntity.setVelocity(v);
			jumping.add(livingEntity.getUniqueId());
			playSpellEffects(EffectPosition.CASTER, livingEntity);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
		LivingEntity livingEntity = (LivingEntity) event.getEntity();
		if (!jumping.remove(livingEntity.getUniqueId())) return;
		playSpellEffects(EffectPosition.TARGET, livingEntity.getLocation());
		if (cancelDamage) event.setCancelled(true);
	}

}
