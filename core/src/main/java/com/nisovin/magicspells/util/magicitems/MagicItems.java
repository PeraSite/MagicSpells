package com.nisovin.magicspells.util.magicitems;

import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.HashMap;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.ItemUtil;
import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.itemreader.*;
import com.nisovin.magicspells.util.AttributeUtil;
import com.nisovin.magicspells.handlers.DebugHandler;
import com.nisovin.magicspells.handlers.EnchantmentHandler;
import com.nisovin.magicspells.util.managers.AttributeManager;
import com.nisovin.magicspells.util.itemreader.alternative.AlternativeReaderManager;

import com.google.common.collect.Multimap;
import com.google.common.collect.HashMultimap;

public class MagicItems {

	private static final Map<String, MagicItem> magicItems = new HashMap<>();
	private static final Map<ItemStack, MagicItemData> itemStackCache = new HashMap<>();

	public static Map<String, MagicItem> getMagicItems() {
		return magicItems;
	}

	public static Collection<String> getMagicItemKeys() {
		return magicItems.keySet();
	}

	public static Collection<MagicItem> getMagicItemValues() {
		return magicItems.values();
	}

	public static MagicItem getMagicItemByInternalName(String internalName) {
		if (!magicItems.containsKey(internalName)) return null;
		if (magicItems.get(internalName) == null) return null;
		return magicItems.get(internalName);
	}

	public static ItemStack getItemByInternalName(String internalName) {
		if (!magicItems.containsKey(internalName)) return null;
		if (magicItems.get(internalName) == null) return null;
		if (magicItems.get(internalName).getItemStack() == null) return null;
		return magicItems.get(internalName).getItemStack().clone();
	}

	public static MagicItemData getMagicItemDataByInternalName(String internalName) {
		if (!magicItems.containsKey(internalName)) return null;
		if (magicItems.get(internalName) == null) return null;
		return magicItems.get(internalName).getMagicItemData();
	}

	public static MagicItemData getMagicItemDataFromItemStack(ItemStack itemStack) {
		if (itemStack == null || BlockUtils.isAir(itemStack.getType())) return null;

		MagicItemData cached = itemStackCache.get(itemStack);
		// We can do this because itemStackCache doesn't have any null values
		if (cached != null) return cached;

		MagicItemData data = new MagicItemData();
		ItemMeta meta = itemStack.getItemMeta();

		// type
		data.setType(itemStack.getType());

		// name
		data = NameHandler.process(itemStack, data);

		// amount
		data.setAmount(itemStack.getAmount());

		// durability
		if (ItemUtil.hasDurability(itemStack.getType())) data = DurabilityHandler.process(itemStack, data);

		// repairCost
		data = RepairableHandler.process(itemStack, data);

		// customModelData
		data = CustomModelDataHandler.process(itemStack, data);

		// power, fireworkEffects
		data = FireworkHandler.process(itemStack, data);

		// unbreakable
		data.setUnbreakable(meta.isUnbreakable());

		// tooltip
		boolean tooltip = true;
		for (ItemFlag itemFlag : ItemFlag.values()) {
			if (!meta.getItemFlags().contains(itemFlag)) tooltip = false;
		}
		data.setHideTooltip(tooltip);

		// color
		data = LeatherArmorHandler.process(itemStack, data);

		// potion, potionEffects, potionColor
		data = PotionHandler.process(itemStack, data);

		// suspiciousStew
		if (itemStack.getType().name().contains("SUSPICIOUS")) data = SuspiciousStewHandler.process(itemStack, data);

		// fireworkEffect
		data = FireworkEffectHandler.process(itemStack, data);

		// skullOwner
		data = SkullHandler.process(itemStack, data);

		// author, title, pages
		data = WrittenBookHandler.process(itemStack, data);

		// enchantments
		data.setEnchantments(meta.getEnchants());

		// attributes
		data.setAttributes(meta.getAttributeModifiers());

		// lore
		data.setLore(meta.getLore());

		// patterns
		data = BannerHandler.process(itemStack, data);

		itemStackCache.put(itemStack, data);
		return data;
	}

	public static MagicItem getMagicItemFromString(String str) {
		if (str == null) return null;
		if (magicItems.containsKey(str)) return magicItems.get(str);

		MagicItem magicItem;
		MagicItemData itemData = MagicItemDataParser.parseMagicItemData(str);
		if (itemData == null) return null;

		magicItem = getMagicItemFromData(itemData);
		return magicItem;
	}

	public static MagicItem getMagicItemFromData(MagicItemData data) {
		if (data == null) return null;

		Material type = data.getType();
		if (type == null) return null;
		ItemStack item = new ItemStack(type);
		ItemMeta meta = item.getItemMeta();

		if (data.getAmount() > 0) item.setAmount(data.getAmount());
		if (data.getName() != null) meta = NameHandler.process(meta, data);
		if (data.getLore() != null) meta = LoreHandler.process(meta, data);
		if (data.getCustomModelData() > 0) meta = CustomModelDataHandler.process(meta, data);

		boolean emptyEnchants = false;
		if (data.getEnchantments() != null) {
			for (Enchantment enchant : data.getEnchantments().keySet()) {
				int level = data.getEnchantments().get(enchant);

				if (meta instanceof EnchantmentStorageMeta) ((EnchantmentStorageMeta) meta).addStoredEnchant(enchant, level, true);
				else if (meta != null) meta.addEnchant(enchant, level, true);
			}
			if (data.getEnchantments().isEmpty()) emptyEnchants = true;
		}

		// Armor color
		meta = LeatherArmorHandler.process(meta, data);

		// Potion effects and potion color
		meta = PotionHandler.process(meta, data);

		// Skull owner
		meta = SkullHandler.process(meta, data);

		// Durability
		meta = DurabilityHandler.process(meta, data);

		// Repair cost
		meta = RepairableHandler.process(meta, data);

		// Written book
		meta = WrittenBookHandler.process(meta, data);

		// Banner
		meta = BannerHandler.process(meta, data);

		// Firework Star
		meta = FireworkEffectHandler.process(meta, data);

		// Firework
		meta = FireworkHandler.process(meta, data);

		// Suspicious Stew
		// compatibility with 1.13
		if (type.name().contains("SUSPICIOUS")) meta = SuspiciousStewHandler.process(meta, data);

		// Unbreakable
		if (meta != null) meta.setUnbreakable(data.isUnbreakable());

		// Hide tooltip
		if (meta != null && data.isTooltipHidden()) meta.addItemFlags(ItemFlag.values());

		// Empty enchant
		if (emptyEnchants) item = ItemUtil.addFakeEnchantment(item);

		// Set meta
		item.setItemMeta(meta);

		// Attributes
		AttributeManager attributeManager = MagicSpells.getAttributeManager();
		if (data.getAttributes() != null) {
			for (Attribute attribute : data.getAttributes().keys()) {
				Collection<AttributeModifier> attributeModifiers = data.getAttributes().get(attribute);
				for (AttributeModifier modifier : attributeModifiers) {
					attributeManager.addItemAttribute(item, attribute, modifier);
				}
			}
		}

		return new MagicItem(item, data);
	}

	public static MagicItem getMagicItemFromSection(ConfigurationSection section) {
		try {
			// It MUST have a type option
			if (!section.contains("type")) return null;

			// See if this is managed by an alternative reader
			ItemStack item = AlternativeReaderManager.deserialize(section);
			if (item != null) return new MagicItem(item, getMagicItemDataFromItemStack(item));

			MagicItemData itemData = new MagicItemData();

			Material type = Util.getMaterial(section.getString("type"));
			if (type == null) return null;
			item = new ItemStack(type);
			itemData.setType(type);
			ItemMeta meta = item.getItemMeta();

			// Name
			meta = NameHandler.process(section, meta, itemData);

			// Lore
			meta = LoreHandler.process(section, meta, itemData);

			// CustomModelData
			meta = CustomModelDataHandler.process(section, meta, itemData);

			// Enchants
			// <enchantmentName> <level>
			boolean emptyEnchants = false;
			if (section.contains("enchants") && section.isList("enchants")) {
				List<String> enchants = section.getStringList("enchants");
				for (String enchant : enchants) {

					String[] data = enchant.split(" ");
					Enchantment e = EnchantmentHandler.getEnchantment(data[0]);
					if (e == null) {
						MagicSpells.error('\'' + data[0] + "' could not be connected to an enchantment");
						continue;
					}

					int level = 0;
					if (data.length > 1) {
						try {
							level = Integer.parseInt(data[1]);
						} catch (NumberFormatException ex) {
							DebugHandler.debugNumberFormat(ex);
						}
					}

					if (meta instanceof EnchantmentStorageMeta) ((EnchantmentStorageMeta) meta).addStoredEnchant(e, level, true);
					else meta.addEnchant(e, level, true);
				}

				if (enchants.isEmpty()) emptyEnchants = true;

				if (meta instanceof EnchantmentStorageMeta) itemData.setEnchantments(((EnchantmentStorageMeta) meta).getStoredEnchants());
				else itemData.setEnchantments(meta.getEnchants());
			}

			// Armor color
			meta = LeatherArmorHandler.process(section, meta, itemData);

			// Potion effects, color, type
			meta = PotionHandler.process(section, meta, itemData);

			// Skull owner
			meta = SkullHandler.process(section, meta, itemData);

			// Durability
			if (ItemUtil.hasDurability(item.getType())) meta = DurabilityHandler.process(section, meta, itemData);

			// Repair cost
			meta = RepairableHandler.process(section, meta, itemData);

			// Written book
			meta = WrittenBookHandler.process(section, meta, itemData);

			// Banner
			meta = BannerHandler.process(section, meta, itemData);

			// Firework Star
			meta = FireworkEffectHandler.process(section, meta, itemData);

			// Firework
			meta = FireworkHandler.process(section, meta, itemData);

			// Suspicious Stew
			// compatibility with 1.13
			if (type.name().contains("SUSPICIOUS")) meta = SuspiciousStewHandler.process(section, meta, itemData);

			// Unbreakable
			boolean unbreakable = section.getBoolean("unbreakable", false);
			meta.setUnbreakable(unbreakable);
			itemData.setUnbreakable(unbreakable);

			// Hide tooltip
			if (section.getBoolean("hide-tooltip", MagicSpells.hideMagicItemTooltips())) {
				meta.addItemFlags(ItemFlag.values());
				itemData.setHideTooltip(true);
			}

			// Set meta
			item.setItemMeta(meta);

			// Empty enchant
			if (emptyEnchants) item = ItemUtil.addFakeEnchantment(item);

			// Attributes
			//<attribute name> <value> (operation) (slot)
			AttributeManager attributeManager = MagicSpells.getAttributeManager();
			if (section.contains("attributes") && section.isList("attributes")) {
				List<String> attributes = section.getStringList("attributes");
				Multimap<Attribute, AttributeModifier> itemAttributes = HashMultimap.create();
				for (String str : attributes) {
					String[] args = str.split(" ");
					if (args.length < 2) continue;

					Attribute attribute = AttributeUtil.AttributeType.getAttribute(args[0]);
					double value = Double.parseDouble(args[1]);

					AttributeModifier.Operation operation = AttributeUtil.AttributeOperation.ADD_NUMBER.toBukkitOperation();
					if (args.length >= 3) operation = AttributeUtil.AttributeOperation.getOperation(args[2]);

					EquipmentSlot slot = null;
					if (args.length >= 4) {
						try {
							slot = EquipmentSlot.valueOf(args[3].toUpperCase());
						} catch (Exception ignored) {}
					}

					AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), args[0], value, operation, slot);
					attributeManager.addItemAttribute(item, attribute, modifier);
					itemAttributes.put(attribute, modifier);
				}

				itemData.setAttributes(itemAttributes);
			}

			return new MagicItem(item, itemData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
