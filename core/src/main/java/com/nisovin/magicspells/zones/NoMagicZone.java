package com.nisovin.magicspells.zones;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.Spell;

public abstract class NoMagicZone implements Comparable<NoMagicZone> {

	private String id;
	
	private int priority;
	
	private String message;
	
	private List<String> allowedSpells;
	
	private List<String> disallowedSpells;
	
	private boolean allowAll;
	
	private boolean disallowAll;
	
	public final void create(String id, ConfigurationSection config) {
		this.id = id;
		this.priority = config.getInt("priority", 0);
		this.message = config.getString("message", "You are in a no-magic zone.");
		this.allowedSpells = config.getStringList("allowed-spells");
		this.disallowedSpells = config.getStringList("disallowed-spells");
		this.allowAll = config.getBoolean("allow-all", false);
		this.disallowAll = config.getBoolean("disallow-all", true);
		if (this.allowedSpells != null && this.allowedSpells.isEmpty()) this.allowedSpells = null;
		if (this.disallowedSpells != null && this.disallowedSpells.isEmpty()) this.disallowedSpells = null;
		if (this.disallowedSpells != null) this.disallowAll = false;
		if (this.allowedSpells != null) this.allowAll = false;
		initialize(config);
	}
	
	public abstract void initialize(ConfigurationSection config);
	
	public final ZoneCheckResult check(Player player, Spell spell) {
		return check(player.getLocation(), spell);
	}
	
	public final ZoneCheckResult check(Location location, Spell spell) {
		if (!inZone(location)) return ZoneCheckResult.IGNORED;
		if (this.disallowedSpells != null && this.disallowedSpells.contains(spell.getInternalName())) return ZoneCheckResult.DENY;
		if (this.allowedSpells != null && this.allowedSpells.contains(spell.getInternalName())) return ZoneCheckResult.ALLOW;
		if (this.disallowAll) return ZoneCheckResult.DENY;
		if (this.allowAll) return ZoneCheckResult.ALLOW;
		return ZoneCheckResult.IGNORED;
	}
	
	public abstract boolean inZone(Location location);
	
	public String getId() {
		return this.id;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	@Override
	public int compareTo(NoMagicZone other) {
		if (this.priority < other.priority) return 1;
		if (this.priority > other.priority) return -1;
		return this.id.compareTo(other.id);
	}
	
	public enum ZoneCheckResult {
		
		ALLOW,
		DENY,
		IGNORED
		
	}
	
}
