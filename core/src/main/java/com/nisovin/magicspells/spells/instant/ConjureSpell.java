package com.nisovin.magicspells.spells.instant;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.util.Vector;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.TimeUtil;
import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.InventoryUtil;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.spells.command.TomeSpell;
import com.nisovin.magicspells.util.magicitems.MagicItem;
import com.nisovin.magicspells.util.magicitems.MagicItems;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.command.ScrollSpell;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.magicitems.MagicItemData;

public class ConjureSpell extends InstantSpell implements TargetedEntitySpell, TargetedLocationSpell {

	private static ExpirationHandler expirationHandler = null;

	private Random rand = ThreadLocalRandom.current();

	private int delay;
	private int pickupDelay;
	private int requiredSlot;
	private int preferredSlot;

	private double expiration;

	private float randomVelocity;

	private boolean offhand;
	private boolean autoEquip;
	private boolean stackExisting;
	private boolean itemHasGravity;
	private boolean addToInventory;
	private boolean addToEnderChest;
	private boolean ignoreMaxStackSize;
	private boolean powerAffectsChance;
	private boolean dropIfInventoryFull;
	private boolean powerAffectsQuantity;
	private boolean forceUpdateInventory;
	private boolean calculateDropsIndividually;

	private List<String> itemList;

	private ItemStack[] itemTypes;

	private double[] itemChances;

	private int[] itemMinQuantities;
	private int[] itemMaxQuantities;

	public ConjureSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		delay = getConfigInt("delay", -1);
		pickupDelay = getConfigInt("pickup-delay", 0);
		requiredSlot = getConfigInt("required-slot", -1);
		preferredSlot = getConfigInt("preferred-slot", -1);

		expiration = getConfigDouble("expiration", 0L);

		randomVelocity = getConfigFloat("random-velocity", 0F);

		offhand = getConfigBoolean("offhand", false);
		autoEquip = getConfigBoolean("auto-equip", false);
		stackExisting = getConfigBoolean("stack-existing", true);
		itemHasGravity = getConfigBoolean("gravity", true);
		addToInventory = getConfigBoolean("add-to-inventory", false);
		addToEnderChest = getConfigBoolean("add-to-ender-chest", false);
		ignoreMaxStackSize = getConfigBoolean("ignore-max-stack-size", false);
		powerAffectsChance = getConfigBoolean("power-affects-chance", true);
		dropIfInventoryFull = getConfigBoolean("drop-if-inventory-full", true);
		powerAffectsQuantity = getConfigBoolean("power-affects-quantity", false);
		forceUpdateInventory = getConfigBoolean("force-update-inventory", true);
		calculateDropsIndividually = getConfigBoolean("calculate-drops-individually", true);

		itemList = getConfigStringList("items", null);

		pickupDelay = Math.max(pickupDelay, 0);
	}
	
	@Override
	public void initialize() {
		super.initialize();

		if (expiration > 0 && expirationHandler == null) expirationHandler = new ExpirationHandler();
		
		if (itemList != null && !itemList.isEmpty()) {
			itemTypes = new ItemStack[itemList.size()];
			itemMinQuantities = new int[itemList.size()];
			itemMaxQuantities = new int[itemList.size()];
			itemChances = new double[itemList.size()];
			
			for (int i = 0; i < itemList.size(); i++) {
				try {
					String str = itemList.get(i);

					int brackets = 0;
					int closedBrackets = 0;
					for (int j = 0; j < str.length(); j++) {
						char ch = str.charAt(j);
						if (ch == '{') brackets++;
						if (ch == '}') closedBrackets++;
					}

					// checks if all brackets are properly closed
					if (brackets != closedBrackets) {
						MagicSpells.error("ConjureSpell '" + internalName + "' has an invalid item defined (e1): " + str);
						continue;
					}

					brackets = 0;
					closedBrackets = 0;

					String[] data = str.split(" ");
					String[] conjureData = null;

					StringBuilder itemData = new StringBuilder();

					for (int j = 0; j < data.length; j++) {
						for (char ch : data[j].toCharArray()) {
							if (ch == '{') brackets++;
							if (ch == '}') closedBrackets++;
						}

						itemData.append(data[j]).append(" ");
						// magicItemData is ready, add the conjureData
						if (brackets == closedBrackets) {
							int dataLeft = data.length - j - 1;
							conjureData = new String[dataLeft];

							// fill the conjureData array with stuff like amount and chance
							for (int d = 0; d < dataLeft; d++) {
								conjureData[d] = data[j + d + 1];
							}
							break;
						}
					}

					String strItemData = itemData.toString().trim();

					if (strItemData.startsWith("TOME:")) {
						String[] tomeData = strItemData.split(":");
						TomeSpell tomeSpell = (TomeSpell) MagicSpells.getSpellByInternalName(tomeData[1]);
						Spell spell = MagicSpells.getSpellByInternalName(tomeData[2]);
						int uses = tomeData.length > 3 ? Integer.parseInt(tomeData[3].trim()) : -1;
						itemTypes[i] = tomeSpell.createTome(spell, uses, null);
					} else if (strItemData.startsWith("SCROLL:")) {
						String[] scrollData = strItemData.split(":");
						ScrollSpell scrollSpell = (ScrollSpell) MagicSpells.getSpellByInternalName(scrollData[1]);
						Spell spell = MagicSpells.getSpellByInternalName(scrollData[2]);
						int uses = scrollData.length > 3 ? Integer.parseInt(scrollData[3].trim()) : -1;
						itemTypes[i] = scrollSpell.createScroll(spell, uses, null);
					} else {
						MagicItem magicItem = MagicItems.getMagicItemFromString(strItemData);
						if (magicItem == null) continue;
						itemTypes[i] = magicItem.getItemStack();
					}

					int minAmount = 1;
					int maxAmount = 1;

					double chance = 100;

					// add default values if there arent any specified
					if (conjureData == null) {
						itemMinQuantities[i] = minAmount;
						itemMaxQuantities[i] = maxAmount;
						itemChances[i] = chance;
						continue;
					}

					// parse minAmount, maxAmount
					if (conjureData.length >= 1) {
						String[] amount = conjureData[0].split("-");
						if (amount.length == 1) {
							minAmount = Integer.parseInt(amount[0].trim());
							maxAmount = minAmount;
						} else if (amount.length >= 2) {
							minAmount = Integer.parseInt(amount[0].trim());
							maxAmount = Integer.parseInt(amount[1].trim()) + 1;
						}
					}

					// parse chance
					if (conjureData.length >= 2) {
						chance = Double.parseDouble(conjureData[1].replace("%", "").trim());
					}

					itemMinQuantities[i] = minAmount;
					itemMaxQuantities[i] = maxAmount;
					itemChances[i] = chance;

				} catch (Exception e) {
					MagicSpells.error("ConjureSpell '" + internalName + "' has specified invalid item (e2): " + itemList.get(i));
					itemTypes[i] = null;
				}
			}
		}
		itemList = null;
	}

	@Override
	public PostCastAction castSpell(LivingEntity livingEntity, SpellCastState state, float power, String[] args) {
		if (itemTypes == null) return PostCastAction.ALREADY_HANDLED;
		if (state == SpellCastState.NORMAL && livingEntity instanceof Player) {
			if (delay >= 0) MagicSpells.scheduleDelayedTask(() -> conjureItems((Player) livingEntity, power), delay);
			else conjureItems((Player) livingEntity, power);
		}
		return PostCastAction.HANDLE_NORMALLY;
		
	}
	
	private void conjureItems(Player player, float power) {
		List<ItemStack> items = new ArrayList<>();
		if (calculateDropsIndividually) individual(items, power);
		else together(items, power);

		Location loc = player.getEyeLocation().add(player.getLocation().getDirection());
		boolean updateInv = false;
		for (ItemStack item : items) {
			MagicItemData itemData = MagicItems.getMagicItemDataFromItemStack(item);
			if (itemData == null) continue;

			boolean added = false;
			PlayerInventory inv = player.getInventory();
			if (autoEquip && item.getAmount() == 1) {
				if (item.getType().name().endsWith("HELMET") && InventoryUtil.isNothing(inv.getHelmet())) {
					inv.setHelmet(item);
					added = true;
				} else if (item.getType().name().endsWith("CHESTPLATE") && InventoryUtil.isNothing(inv.getChestplate())) {
					inv.setChestplate(item);
					added = true;
				} else if (item.getType().name().endsWith("LEGGINGS") && InventoryUtil.isNothing(inv.getLeggings())) {
					inv.setLeggings(item);
					added = true;
				} else if (item.getType().name().endsWith("BOOTS") && InventoryUtil.isNothing(inv.getBoots())) {
					inv.setBoots(item);
					added = true;
				}
			}

			if (!added) {
				if (addToEnderChest) added = Util.addToInventory(player.getEnderChest(), item, stackExisting, ignoreMaxStackSize);
				if (!added && addToInventory) {

					ItemStack preferredItem = null;
					MagicItemData magicItemData = null;
					if (preferredSlot >= 0) {
						preferredItem = inv.getItem(preferredSlot);
						magicItemData = MagicItems.getMagicItemDataFromItemStack(preferredItem);
					}

					if (offhand) player.getEquipment().setItemInOffHand(item);
					else if (requiredSlot >= 0) {
						ItemStack old = inv.getItem(requiredSlot);
						MagicItemData oldItemData = MagicItems.getMagicItemDataFromItemStack(old);
						if (old != null && (oldItemData != null && oldItemData.equals(itemData))) item.setAmount(item.getAmount() + old.getAmount());
						inv.setItem(requiredSlot, item);
						added = true;
						updateInv = true;
					} else if (preferredSlot >= 0 && InventoryUtil.isNothing(preferredItem)) {
						inv.setItem(preferredSlot, item);
						added = true;
						updateInv = true;
					} else if (preferredSlot >= 0 && (magicItemData != null && magicItemData.equals(itemData)) && preferredItem.getAmount() + item.getAmount() < item.getType().getMaxStackSize()) {
						item.setAmount(item.getAmount() + preferredItem.getAmount());
						inv.setItem(preferredSlot, item);
						added = true;
						updateInv = true;
					} else if (!added) {
						added = Util.addToInventory(inv, item, stackExisting, ignoreMaxStackSize);
						if (added) updateInv = true;
					}
				}
				if (!added && (dropIfInventoryFull || !addToInventory)) {
					Item i = player.getWorld().dropItem(loc, item);
					i.setItemStack(item);
					i.setPickupDelay(pickupDelay);
					i.setGravity(itemHasGravity);
					playSpellEffects(EffectPosition.SPECIAL, i);
				}
			} else updateInv = true;
		}

		if (updateInv && forceUpdateInventory) player.updateInventory();
		playSpellEffects(EffectPosition.CASTER, player);
	}
	
	private void individual(List<ItemStack> items, float power) {
		for (int i = 0; i < itemTypes.length; i++) {
			double r = rand.nextDouble() * 100;
			if (powerAffectsChance) r = r / power;
			if (itemTypes[i] != null && r < itemChances[i]) addItem(i, items, power);
		}
	}
	
	private void together(List<ItemStack> items, float power) {
		double r = rand.nextDouble() * 100;
		double m = 0;
		for (int i = 0; i < itemTypes.length; i++) {
			if (itemTypes[i] != null && r < itemChances[i] + m) {
				addItem(i, items, power);
				return;
			} else m += itemChances[i];
		}
	}
	
	private void addItem(int i, List<ItemStack> items, float power) {
		int quant = itemMinQuantities[i];
		if (itemMaxQuantities[i] > itemMinQuantities[i]) quant = rand.nextInt(itemMaxQuantities[i] - itemMinQuantities[i]) + itemMinQuantities[i];
		if (powerAffectsQuantity) quant = Math.round(quant * power);
		if (quant > 0) {
			ItemStack item = itemTypes[i].clone();
			item.setAmount(quant);
			if (expiration > 0) expirationHandler.addExpiresLine(item, expiration);
			items.add(item);
		}
	}

	@Override
	public boolean castAtLocation(LivingEntity caster, Location target, float power) {
		return castAtLocation(target, power);
	}
	
	@Override
	public boolean castAtLocation(Location target, float power) {
		List<ItemStack> items = new ArrayList<>();
		if (calculateDropsIndividually) individual(items, power);
		else together(items, power);

		Location loc = target.clone();
		if (!BlockUtils.isAir(loc.getBlock().getType())) loc.add(0, 1, 0);
		if (!BlockUtils.isAir(loc.getBlock().getType())) loc.add(0, 1, 0);
		for (ItemStack item : items) {
			Item dropped = loc.getWorld().dropItem(loc, item);
			dropped.setItemStack(item);
			dropped.setPickupDelay(pickupDelay);
			if (randomVelocity > 0) {
				Vector v = new Vector(rand.nextDouble() - 0.5, rand.nextDouble() / 2, rand.nextDouble() - 0.5);
				v.normalize().multiply(randomVelocity);
				dropped.setVelocity(v);
			}
			dropped.setGravity(itemHasGravity);
			playSpellEffects(EffectPosition.SPECIAL, dropped);
		}
		return true;
	}

	@Override
	public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
		return castAtEntity(target, power);
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		if (!(target instanceof Player)) {
			castAtLocation(target.getLocation(), power);
			return true;
		}
		conjureItems((Player) target, power);
		return true;
	}
	
	@Override
	public void turnOff() {
		expirationHandler = null;
	}
	
	private static class ExpirationHandler implements Listener {
		
		private final String expPrefix =  ChatColor.BLACK.toString() + ChatColor.MAGIC.toString() + "MSExp:";
		
		private ExpirationHandler() {
			MagicSpells.registerEvents(this);
		}

		private void addExpiresLine(ItemStack item, double expireHours) {
			ItemMeta meta = item.getItemMeta();
			List<String> lore;
			if (meta.hasLore()) lore = new ArrayList<>(meta.getLore());
			else lore = new ArrayList<>();

			long expiresAt = System.currentTimeMillis() + (long) (expireHours * TimeUtil.MILLISECONDS_PER_HOUR);
			lore.add(getExpiresText(expiresAt));
			lore.add(expPrefix + expiresAt);
			meta.setLore(lore);
			item.setItemMeta(meta);
		}
		
		@EventHandler(priority = EventPriority.LOWEST)
		private void onJoin(PlayerJoinEvent event) {
			PlayerInventory inv = event.getPlayer().getInventory();
			processInventory(inv);
			ItemStack[] armor = inv.getArmorContents();
			processInventoryContents(armor);
			inv.setArmorContents(armor);
		}
		
		@EventHandler(priority = EventPriority.LOWEST)
		private void onInvOpen(InventoryOpenEvent event) {
			processInventory(event.getInventory());
		}
		
		@EventHandler(priority = EventPriority.LOWEST)
		private void onRightClick(PlayerInteractEvent event) {
			if (!event.hasItem()) return;
			ItemStack item = event.getPlayer().getEquipment().getItemInMainHand();
			ExpirationResult result = updateExpiresLineIfNeeded(item);
			if (result == ExpirationResult.EXPIRED) {
				event.getPlayer().getEquipment().setItemInMainHand(null);
				event.setCancelled(true);
			}
		}
		
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		private void onPickup(EntityPickupItemEvent event) {
			processItemDrop(event.getItem());
			if (event.getItem().isDead()) event.setCancelled(true);
		}
		
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		private void onDrop(PlayerDropItemEvent event) {
			processItemDrop(event.getItemDrop());
		}
		
		@EventHandler(priority = EventPriority.LOWEST)
		private void onItemSpawn(ItemSpawnEvent event) {
			processItemDrop(event.getEntity());
		}
		
		private void processInventory(Inventory inv) {
			ItemStack[] contents = inv.getContents();
			processInventoryContents(contents);
			inv.setContents(contents);
		}
		
		private void processInventoryContents(ItemStack[] contents) {
			for (int i = 0; i < contents.length; i++) {
				ExpirationResult result = updateExpiresLineIfNeeded(contents[i]);
				if (result == ExpirationResult.EXPIRED) contents[i] = null;
			}
		}
		
		private boolean processItemDrop(Item drop) {
			ItemStack item = drop.getItemStack();
			ExpirationResult result = updateExpiresLineIfNeeded(item);
			if (result == ExpirationResult.UPDATE) drop.setItemStack(item);
			else if (result == ExpirationResult.EXPIRED) {
				drop.remove();
				return true;
			}
			return false;
		}
		
		private ExpirationResult updateExpiresLineIfNeeded(ItemStack item) {
			if (item == null) return ExpirationResult.NO_UPDATE;
			if (!item.hasItemMeta()) return ExpirationResult.NO_UPDATE;

			ItemMeta meta = item.getItemMeta();
			if (!meta.hasLore()) return ExpirationResult.NO_UPDATE;

			List<String> lore = new ArrayList<>(meta.getLore());
			if (lore.size() < 2) return ExpirationResult.NO_UPDATE;

			String lastLine = lore.get(lore.size() - 1);
			if (!lastLine.startsWith(expPrefix)) return ExpirationResult.NO_UPDATE;

			long expiresAt = Long.parseLong(lastLine.replace(expPrefix, ""));
			if (expiresAt < System.currentTimeMillis()) return ExpirationResult.EXPIRED;

			lore.set(lore.size() - 2, getExpiresText(expiresAt));
			meta.setLore(lore);
			item.setItemMeta(meta);
			return ExpirationResult.UPDATE;
		}
	
		private String getExpiresText(long expiresAt) {
			if (expiresAt < System.currentTimeMillis()) return ChatColor.GRAY + "Expired";
			double hours = (expiresAt - System.currentTimeMillis()) / ((double) TimeUtil.MILLISECONDS_PER_HOUR);
			if (hours / 24 >= 15) return ChatColor.GRAY + "Expires in " + ChatColor.WHITE + ((long) hours / TimeUtil.HOURS_PER_WEEK) + ChatColor.GRAY + " weeks";
			if (hours / 24 >= 3) return ChatColor.GRAY + "Expires in " + ChatColor.WHITE + ((long) hours / TimeUtil.HOURS_PER_DAY) + ChatColor.GRAY + " days";
			if (hours >= 2) return ChatColor.GRAY + "Expires in " + ChatColor.WHITE + (long) hours + ChatColor.GRAY + " hours";
			return ChatColor.GRAY + "Expires in " + ChatColor.WHITE + '1' + ChatColor.GRAY + " hour";
		}		
		
	}
	
	private enum ExpirationResult {
		
		NO_UPDATE,
		UPDATE,
		EXPIRED
		
	}
	
}
