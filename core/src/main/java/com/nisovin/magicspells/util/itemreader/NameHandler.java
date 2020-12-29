package com.nisovin.magicspells.util.itemreader;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.util.magicitems.MagicItemData;

public class NameHandler {

	private static final String CONFIG_NAME = "name";

	public static ItemMeta process(ConfigurationSection config, ItemMeta meta, MagicItemData data) {
		if (!config.contains(CONFIG_NAME)) return meta;
		if (!config.isString(CONFIG_NAME)) return meta;

		meta.setDisplayName(Util.colorize(config.getString(CONFIG_NAME)));
		if (data != null) data.setName(Util.decolorize(config.getString(CONFIG_NAME)));
		return meta;
	}

	public static ItemMeta process(ItemMeta meta, MagicItemData data) {
		if (meta == null) return null;
		if (data == null) return meta;
		if (data.getName() == null) return meta;

		meta.setDisplayName(Util.colorize(data.getName()));
		return meta;
	}

	public static MagicItemData process(ItemStack itemStack, MagicItemData itemData) {
		if (itemData == null) return null;
		if (itemStack == null) return itemData;
		ItemMeta meta = itemStack.getItemMeta();
		if (meta == null) return itemData;

		if (!meta.getDisplayName().isEmpty()) itemData.setName(Util.decolorize(meta.getDisplayName()));
		return itemData;
	}
	
}
