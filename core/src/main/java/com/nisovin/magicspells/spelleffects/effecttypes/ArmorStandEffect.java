package com.nisovin.magicspells.spelleffects.effecttypes;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.util.EntityData;
import com.nisovin.magicspells.spelleffects.SpellEffect;
import com.nisovin.magicspells.util.magicitems.MagicItem;
import com.nisovin.magicspells.util.magicitems.MagicItems;

public class ArmorStandEffect extends SpellEffect {

	public static final String ENTITY_TAG = "MS_ARMOR_STAND";

	private EntityData entityData;

	private boolean gravity;

	private String customName;
	private boolean customNameVisible;

	private ItemStack headItem;
	private ItemStack mainhandItem;
	private ItemStack offhandItem;

	@Override
	protected void loadFromConfig(ConfigurationSection config) {
		ConfigurationSection section = config.getConfigurationSection("armorstand");
		if (section == null) return;

		entityData = new EntityData(section);
		entityData.setEntityType(EntityType.ARMOR_STAND);

		gravity = section.getBoolean("gravity", false);

		customName = section.getString("custom-name", "");
		customNameVisible = section.getBoolean("custom-name-visible", false);

		String strMagicItem = section.getString("head", "");
		MagicItem magicItem = MagicItems.getMagicItemFromString(strMagicItem);
		if (magicItem != null) headItem = magicItem.getItemStack();

		strMagicItem = section.getString("mainhand", "");
		magicItem = MagicItems.getMagicItemFromString(strMagicItem);
		if (magicItem != null) mainhandItem = magicItem.getItemStack();

		strMagicItem = section.getString("offhand", "");
		magicItem = MagicItems.getMagicItemFromString(strMagicItem);
		if (magicItem != null) offhandItem = magicItem.getItemStack();

	}

	@Override
	protected ArmorStand playArmorStandEffectLocation(Location location) {
		ArmorStand armorStand = (ArmorStand) entityData.spawn(location);
		armorStand.addScoreboardTag(ENTITY_TAG);
		armorStand.setGravity(gravity);
		armorStand.setSilent(true);
		armorStand.setCustomName(customName);
		armorStand.setCustomNameVisible(customNameVisible);

		if (headItem != null) armorStand.setItem(EquipmentSlot.HEAD, headItem);
		if (mainhandItem != null) armorStand.setItem(EquipmentSlot.HAND, mainhandItem);
		if (offhandItem != null) armorStand.setItem(EquipmentSlot.OFF_HAND, offhandItem);
		return armorStand;
	}

}
