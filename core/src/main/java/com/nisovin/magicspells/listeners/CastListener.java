package com.nisovin.magicspells.listeners;

import java.util.Map;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerAnimationEvent;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.BlockUtils;

public class CastListener implements Listener {

	private MagicSpells plugin;

	private Map<String, Long> noCastUntil;

	public CastListener(MagicSpells plugin) {
		this.plugin = plugin;
		noCastUntil = new HashMap<>();
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		// First check if player is interacting with a special block
		boolean noInteract = false;
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Material m = event.getClickedBlock().getType();
			if (BlockUtils.isWoodDoor(m) ||
					BlockUtils.isWoodTrapdoor(m) ||
					BlockUtils.isWoodButton(m) ||
					BlockUtils.isBed(m) ||
					m == Material.CRAFTING_TABLE ||
					m == Material.CHEST ||
					m == Material.TRAPPED_CHEST ||
					m == Material.ENDER_CHEST ||
					m == Material.FURNACE ||
					m == Material.HOPPER ||
					m == Material.LEVER ||
					m == Material.STONE_BUTTON ||
					m == Material.ENCHANTING_TABLE) {
				noInteract = true;
			} else if (event.hasItem() && event.getItem().getType().isBlock()) {
				noInteract = true;
			}

			// Force exp bar back to show exp when trying to enchant
			if (m == Material.ENCHANTING_TABLE) MagicSpells.getExpBarManager().update(player, player.getLevel(), player.getExp());
		}

		if (noInteract) {
			// Special block -- don't do normal interactions
			noCastUntil.put(event.getPlayer().getName(), System.currentTimeMillis() + 150);
			return;
		}

		if (isEventCastAction(event)) {
			// Cast
			if (!MagicSpells.isCastingOnAnimate()) castSpell(event.getPlayer());
			return;
		}

		if (isEventCycleAction(event) && (MagicSpells.isCyclingSpellsOnOffhandAction() || event.getHand() == EquipmentSlot.HAND)) {
			ItemStack inHand = player.getEquipment().getItemInMainHand();
			
			if (inHand != null && isBow(inHand.getType())) {
				if (!MagicSpells.canBowCycleSpellsSneaking() && player.isSneaking()) return;
			}
			
			if ((inHand != null && !BlockUtils.isAir(inHand.getType())) || MagicSpells.canCastWithFist()) {
				// Cycle spell
				Spell spell;
				if (!player.isSneaking()) spell = MagicSpells.getSpellbook(player).nextSpell(inHand);
				else spell = MagicSpells.getSpellbook(player).prevSpell(inHand);

				if (spell == null) return;
				// Send message
				MagicSpells.sendMessageAndFormat(player, MagicSpells.getSpellChangeMessage(), "%s", spell.getName());
				// Show spell icon
				if (MagicSpells.getSpellIconSlot() >= 0) showIcon(player, MagicSpells.getSpellIconSlot(), spell.getSpellIcon());

				// Use cool new text thingy
				/*boolean yay = false;
				if (yay) {
					final ItemStack fake = inHand.clone();
					if (fake == null) return;
					ItemMeta meta = fake.getItemMeta();
					meta.setDisplayName("Spell: " + spell.getName());
					fake.setItemMeta(meta);
					MagicSpells.scheduleDelayedTask(() -> MagicSpells.getVolatileCodeHandler().sendFakeSlotUpdate(player, player.getInventory().getHeldItemSlot(), fake), 0);
				} */
			}
		}
	}

	@EventHandler
	public void onItemHeldChange(final PlayerItemHeldEvent event) {
		if (MagicSpells.getSpellIconSlot() >= 0 && MagicSpells.getSpellIconSlot() <= 8) {
			Player player = event.getPlayer();
			if (event.getNewSlot() == MagicSpells.getSpellIconSlot()) {
				showIcon(player, MagicSpells.getSpellIconSlot(), null);
				return;
			}
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			Spell spell = spellbook.getActiveSpell(player.getInventory().getItem(event.getNewSlot()));
			if (spell != null) showIcon(player, MagicSpells.getSpellIconSlot(), spell.getSpellIcon());
			else showIcon(player, MagicSpells.getSpellIconSlot(), null);
		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		if (MagicSpells.isCastingOnAnimate()) castSpell(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerShootBow(EntityShootBowEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		castSpell(player);
		event.getProjectile().setMetadata("bow-draw-strength", new FixedMetadataValue(plugin, event.getForce()));
	}

	private void castSpell(Player player) {
		ItemStack inHand = player.getEquipment().getItemInMainHand();
		if (!MagicSpells.canCastWithFist() && (inHand == null || BlockUtils.isAir(inHand.getType()))) return;

		Spellbook spellbook = MagicSpells.getSpellbook(player);
		if (spellbook == null) return;

		Spell spell = spellbook.getActiveSpell(inHand);
		if (spell == null) return;
		if (!spell.canCastWithItem()) return;

		// First check global cooldown
		if (MagicSpells.getGlobalCooldown() > 0 && !spell.isIgnoringGlobalCooldown()) {
			if (noCastUntil.containsKey(player.getName()) && noCastUntil.get(player.getName()) > System.currentTimeMillis()) return;
			noCastUntil.put(player.getName(), System.currentTimeMillis() + MagicSpells.getGlobalCooldown());
		}
		// Cast spell
		spell.cast(player);
	}

	private void showIcon(Player player, int slot, ItemStack icon) {
		if (icon == null) icon = player.getInventory().getItem(MagicSpells.getSpellIconSlot());
		MagicSpells.getVolatileCodeHandler().sendFakeSlotUpdate(player, slot, icon);
	}

	private boolean isBow(Material material) {
		if (material == null) return false;
		return material.name().equalsIgnoreCase("BOW") || material.name().equalsIgnoreCase("CROSSBOW");
	}
	
	private boolean isEventCastAction(PlayerInteractEvent event) {
		if (MagicSpells.areBowCycleButtonsReversed() && event.hasItem() && isBow(event.getItem().getType())) {
			return false;
		}
		
		return event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
	}
	
	private boolean isEventCycleAction(PlayerInteractEvent event) {
		if (MagicSpells.areBowCycleButtonsReversed() && event.hasItem() && isBow(event.getItem().getType())) {
			return event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
		}
		
		return event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
	}

}
