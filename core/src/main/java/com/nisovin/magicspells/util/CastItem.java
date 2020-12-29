package com.nisovin.magicspells.util;

import java.util.Map;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.magicitems.MagicItem;
import com.nisovin.magicspells.util.magicitems.MagicItems;
import com.nisovin.magicspells.util.magicitems.MagicItemData;
import com.nisovin.magicspells.util.itemreader.PotionHandler;
import com.nisovin.magicspells.util.itemreader.DurabilityHandler;
import com.nisovin.magicspells.util.itemreader.WrittenBookHandler;
import com.nisovin.magicspells.util.itemreader.LeatherArmorHandler;

public class CastItem {

	private Material type = null;
	private String name = null;

	private int amount = 0;
	private int durability = -1;
	private int customModelData = 0;
	private boolean unbreakable = false;

	private Color color = null;
	private PotionType potionType = null;
	private String title = null;
	private String author = null;

	private Map<Enchantment, Integer> enchants = null;
	private List<String> lore = null;

	public CastItem() {

	}

	public CastItem(ItemStack item) {
		if (item == null) throw new NullPointerException("itemStack");
		ItemMeta meta = item.getItemMeta();

		type = item.getType();
		if (isTypeValid()) {
			if (!MagicSpells.ignoreCastItemNames()) {
				if (meta.getDisplayName().isEmpty()) name = null;
				else if (MagicSpells.ignoreCastItemNameColors()) name = Util.decolorize(meta.getDisplayName());
				else name = meta.getDisplayName();
			}
			if (!MagicSpells.ignoreCastItemAmount()) amount = item.getAmount();
			if (!MagicSpells.ignoreCastItemDurability(type) && ItemUtil.hasDurability(type)) durability = DurabilityHandler.getDurability(meta);
			if (!MagicSpells.ignoreCastItemCustomModelData()) customModelData = ItemUtil.getCustomModelData(meta);
			if (!MagicSpells.ignoreCastItemBreakability()) unbreakable = meta.isUnbreakable();
			if (!MagicSpells.ignoreCastItemColor()) color = LeatherArmorHandler.getColor(meta);
			if (!MagicSpells.ignoreCastItemPotionType()) potionType = PotionHandler.getPotionType(meta);
			if (!MagicSpells.ignoreCastItemTitle()) title = WrittenBookHandler.getTitle(meta);
			if (!MagicSpells.ignoreCastItemAuthor()) author = WrittenBookHandler.getAuthor(meta);
			if (!MagicSpells.ignoreCastItemEnchants()) enchants = meta.getEnchants();
			if (!MagicSpells.ignoreCastItemLore()) lore = meta.getLore();
		}
	}

	public CastItem(String string) {
		MagicItem magicItem = MagicItems.getMagicItemFromString(string);
		if (magicItem != null && magicItem.getMagicItemData() != null) {
			MagicItemData data = magicItem.getMagicItemData();
			type = data.getType();
			if (isTypeValid()) {
				if (!MagicSpells.ignoreCastItemNames() && data.getName() != null) {
					if (MagicSpells.ignoreCastItemNameColors()) name = Util.decolorize(data.getName());
					else name = Util.colorize(data.getName());
				}
				if (!MagicSpells.ignoreCastItemAmount()) amount = data.getAmount();
				if (!MagicSpells.ignoreCastItemDurability(type) && ItemUtil.hasDurability(type)) durability = data.getDurability();
				if (!MagicSpells.ignoreCastItemCustomModelData()) customModelData = data.getCustomModelData();
				if (!MagicSpells.ignoreCastItemBreakability()) unbreakable = data.isUnbreakable();
				if (!MagicSpells.ignoreCastItemColor()) color = data.getColor();
				if (!MagicSpells.ignoreCastItemPotionType()) potionType = data.getPotionType();
				if (!MagicSpells.ignoreCastItemTitle()) title = data.getTitle();
				if (!MagicSpells.ignoreCastItemAuthor()) author = data.getAuthor();
				if (!MagicSpells.ignoreCastItemEnchants()) enchants = data.getEnchantments();
				if (!MagicSpells.ignoreCastItemLore()) lore = data.getLore();
			}
		}
	}

	public boolean isTypeValid() {
		return type != null && !BlockUtils.isAir(type);
	}

	public Material getType() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CastItem) return equalsCastItem((CastItem) o);
		if (o instanceof ItemStack) return equalsCastItem(new CastItem((ItemStack) o));
		return false;
	}

	public boolean equalsCastItem(CastItem i) {
		if (i == null) return false;
		if (i.type != type) return false;
		if (i.durability != durability) return false;
		if (!MagicSpells.ignoreCastItemNames()) return objectEquals(i.name, name);
		if (!MagicSpells.ignoreCastItemCustomModelData()) return i.customModelData == customModelData;
		if (!MagicSpells.ignoreCastItemBreakability()) return i.unbreakable == unbreakable;
		if (!MagicSpells.ignoreCastItemColor()) return objectEquals(i.color, color);
		if (!MagicSpells.ignoreCastItemPotionType()) return i.potionType == potionType;
		if (!MagicSpells.ignoreCastItemTitle()) return objectEquals(i.title, title);
		if (!MagicSpells.ignoreCastItemAuthor()) return objectEquals(i.author, author);
		if (!MagicSpells.ignoreCastItemEnchants()) return objectEquals(i.enchants, enchants);
		if (!MagicSpells.ignoreCastItemLore()) return objectEquals(i.lore, lore);
		return true;
	}

	public boolean objectEquals(Object o, Object object) {
		if (o == null && object == null) return true;
		if (o == null && object != null) return false;
		if (o != null && object == null) return false;
		return o.equals(object);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		if (type == null) return "";
		MagicItemData data = new MagicItemData();
		data.setType(type);
		data.setName(name);
		data.setAmount(amount);
		data.setDurability(durability);
		data.setCustomModelData(customModelData);
		data.setUnbreakable(unbreakable);
		data.setColor(color);
		data.setPotionType(potionType);
		data.setTitle(title);
		data.setAuthor(author);
		data.setEnchantments(enchants);
		data.setLore(lore);
		return data.toString();
	}

}
