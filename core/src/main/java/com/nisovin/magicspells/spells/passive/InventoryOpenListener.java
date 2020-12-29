package com.nisovin.magicspells.spells.passive;

import java.util.Set;
import java.util.HashSet;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;

import com.nisovin.magicspells.util.OverridePriority;
import com.nisovin.magicspells.spells.passive.util.PassiveListener;

// Optional trigger variable that may contain a comma separated list of inventory names to trigger on
public class InventoryOpenListener extends PassiveListener {

	private final Set<String> inventoryNames = new HashSet<>();

	@Override
	public void initialize(String var) {
		if (var == null || var.isEmpty()) return;

		String[] split = var.split(",");
		for (String s : split) {
			inventoryNames.add(s.trim());
		}
	}

	@OverridePriority
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		HumanEntity player = event.getPlayer();
		String inventoryName = event.getView().getTitle();
		if (!inventoryNames.isEmpty() && !inventoryNames.contains(inventoryName)) return;

		if (!hasSpell(player)) return;
		passiveSpell.activate(player);
	}

}
