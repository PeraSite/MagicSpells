package com.nisovin.magicspells.spells.instant;

import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

import org.bukkit.util.Vector;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.LivingEntity;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.util.Util;
import org.bukkit.event.entity.EntityDamageEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.spelleffects.EffectPosition;

public class LeapSpell extends InstantSpell {

	private final Set<UUID> jumping;

	private final float rotation;
	private final float upwardVelocity;
	private final float forwardVelocity;

	private final boolean clientOnly;
	private final boolean cancelDamage;
	private final boolean addVelocityInstead;

	private final String landSpellName;
	private Subspell landSpell;

	public LeapSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		jumping = new HashSet<>();

		rotation = getConfigFloat("rotation", 0F);
		upwardVelocity = getConfigFloat("upward-velocity", 15F) / 10F;
		forwardVelocity = getConfigFloat("forward-velocity", 40F) / 10F;

		clientOnly = getConfigBoolean("client-only", false);
		cancelDamage = getConfigBoolean("cancel-damage", true);
		addVelocityInstead = getConfigBoolean("add-velocity-instead", false);

		landSpellName = getConfigString("land-spell", "");
	}

	@Override
	public void initialize() {
		super.initialize();

		landSpell = new Subspell(landSpellName);
		if (!landSpell.process()) {
			if (!landSpellName.isEmpty()) MagicSpells.error("LeapSpell '" + internalName + "' has an invalid land-spell defined!");
			landSpell = null;
		}
	}

	public boolean isJumping(LivingEntity livingEntity) {
		return jumping.contains(livingEntity.getUniqueId());
	}

	@Override
	public PostCastAction castSpell(LivingEntity livingEntity, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Vector v = livingEntity.getLocation().getDirection();
			v.setY(0).normalize().multiply(forwardVelocity * power).setY(upwardVelocity * power);
			if (rotation != 0) Util.rotateVector(v, rotation);
			v = Util.makeFinite(v);

			if (clientOnly && livingEntity instanceof Player) MagicSpells.getVolatileCodeHandler().setClientVelocity((Player) livingEntity, v);
			else {
				if (addVelocityInstead) livingEntity.setVelocity(livingEntity.getVelocity().add(v));
				else livingEntity.setVelocity(v);
			}

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
		if (landSpell != null) landSpell.cast(livingEntity, 1F);
		playSpellEffects(EffectPosition.TARGET, livingEntity.getLocation());
		if (cancelDamage) event.setCancelled(true);
	}

}
