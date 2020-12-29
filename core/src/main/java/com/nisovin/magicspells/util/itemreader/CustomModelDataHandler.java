package com.nisovin.magicspells.util.itemreader;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.util.ItemUtil;
import com.nisovin.magicspells.util.magicitems.MagicItemData;

public class CustomModelDataHandler {

	private static final String CONFIG_NAME = "custom-model-data";

	public static ItemMeta process(ConfigurationSection config, ItemMeta meta, MagicItemData data) {
		if (!config.contains(CONFIG_NAME)) return meta;
		if (!config.isInt(CONFIG_NAME)) return meta;

		int customModelData = config.getInt(CONFIG_NAME);

		ItemUtil.setCustomModelData(meta, customModelData);
		data.setCustomModelData(customModelData);
		return meta;
	}

	public static ItemMeta process(ItemMeta meta, MagicItemData data) {
		if (meta == null) return null;
		if (data == null) return meta;

		int customModelData = data.getCustomModelData();
		if (customModelData <= 0) return meta;

		ItemUtil.setCustomModelData(meta, customModelData);
		return meta;
	}

	public static MagicItemData process(ItemStack itemStack, MagicItemData itemData) {
		if (itemData == null) return null;
		if (itemStack == null) return itemData;

		itemData.setCustomModelData(ItemUtil.getCustomModelData(itemStack.getItemMeta()));
		return itemData;
	}

}
