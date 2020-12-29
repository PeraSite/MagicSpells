package com.nisovin.magicspells.util.itemreader;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.util.magicitems.MagicItemData;

public class SkullHandler {

	private static final String SKULL_OWNER_CONFIG_NAME = "skull-owner";
	private static final String UUID_CONFIG_NAME = "uuid";
	private static final String TEXTURE_CONFIG_NAME = "texture";
	private static final String SIGNATURE_CONFIG_NAME = "signature";

	public static ItemMeta process(ConfigurationSection config, ItemMeta meta, MagicItemData data) {
		if (!(meta instanceof SkullMeta)) return meta;
		
		SkullMeta skullMeta = (SkullMeta) meta;

		OfflinePlayer offlinePlayer = null;

		if (config.contains(SKULL_OWNER_CONFIG_NAME)) {
			offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(config.get(SKULL_OWNER_CONFIG_NAME).toString()));
			skullMeta.setOwningPlayer(offlinePlayer);
		}

		String uuid = null;
		String texture = null;
		String signature = null;

		if (config.contains(UUID_CONFIG_NAME) && config.isString(UUID_CONFIG_NAME)) {
			uuid = config.getString(UUID_CONFIG_NAME);
		}
		if (config.contains(TEXTURE_CONFIG_NAME) && config.isString(TEXTURE_CONFIG_NAME)) {
			texture = config.getString(TEXTURE_CONFIG_NAME);
		}
		if (config.contains(SIGNATURE_CONFIG_NAME) && config.isString(SIGNATURE_CONFIG_NAME)) {
			signature = config.getString(SIGNATURE_CONFIG_NAME);
		}

		if (texture != null && skullMeta.getOwningPlayer() != null) {
			Util.setTexture(skullMeta, texture, signature, uuid, skullMeta.getOwningPlayer());
		}

		if (data != null) {
			data.setSkullOwner(offlinePlayer);
			data.setUUID(uuid);
			data.setTexture(texture);
			data.setSignature(signature);
		}

		return skullMeta;
	}

	public static ItemMeta process(ItemMeta meta, MagicItemData data) {
		if (data == null) return meta;
		if (!(meta instanceof SkullMeta)) return meta;

		SkullMeta skullMeta = (SkullMeta) meta;

		OfflinePlayer offlinePlayer = data.getSkullOwner();
		if (offlinePlayer != null) skullMeta.setOwningPlayer(offlinePlayer);

		String uuid = data.getUUID();
		String texture = data.getTexture();
		String signature = data.getSignature();

		if (texture != null && skullMeta.getOwningPlayer() != null) {
			Util.setTexture(skullMeta, texture, signature, uuid, skullMeta.getOwningPlayer());
		}
		return skullMeta;
	}

	public static MagicItemData process(ItemStack itemStack, MagicItemData data) {
		if (data == null) return null;
		if (itemStack == null) return data;
		if (!(itemStack.getItemMeta() instanceof SkullMeta)) return data;

		SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
		data.setSkullOwner(meta.getOwningPlayer());
		return data;
	}
	
}
