package com.nisovin.magicspells.volatilecode;

import org.bukkit.entity.*;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ItemStack;

public interface VolatileCodeHandle {

	void addPotionGraphicalEffect(LivingEntity entity, int color, int duration);

	void sendFakeSlotUpdate(Player player, int slot, ItemStack item);

	boolean simulateTnt(Location target, LivingEntity source, float explosionSize, boolean fire);

	void setExperienceBar(Player player, int level, float percent);

	void setFallingBlockHurtEntities(FallingBlock block, float damage, int max);

	void playDragonDeathEffect(Location location);

	void setClientVelocity(Player player, Vector velocity);

	ItemStack setNBTString(ItemStack item, String key, String value);

	String getNBTString(ItemStack item, String key);

	void setInventoryTitle(Player player, String title);

	Recipe createSmithingRecipe(NamespacedKey namespaceKey, ItemStack result, Material base, Material addition);

	String colorize(String message);

}
