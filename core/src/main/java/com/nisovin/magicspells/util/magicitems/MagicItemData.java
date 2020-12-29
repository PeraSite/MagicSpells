package com.nisovin.magicspells.util.magicitems;

import java.util.*;

import org.bukkit.*;
import org.bukkit.potion.PotionType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.attribute.AttributeModifier;

import com.nisovin.magicspells.util.Util;

import com.google.common.collect.Multimap;

public class MagicItemData {

	private Material type;

	private String name = null;

	private int amount = 1;
	private int durability = -1;
	private int repairCost;
	private int customModelData = 0;

	private int power;

	private boolean unbreakable;
	private boolean hideTooltip;

	private Color color = null;

	private PotionType potionType = PotionType.UNCRAFTABLE;
	private FireworkEffect fireworkEffect = null;
	private OfflinePlayer skullOwner = null;

	private String title = null;
	private String author = null;

	private String uuid = null;
	private String texture = null;
	private String signature = null;

	private Map<Enchantment, Integer> enchantments = new HashMap<>();

	private Multimap<Attribute, AttributeModifier> attributes = null;

	private List<String> lore = null;
	private List<String> pages = null;
	private List<Pattern> patterns = null;
	private List<PotionEffect> potionEffects = null;
	private List<FireworkEffect> fireworkEffects = null;

	public void setType(Material type) {
		this.type = type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void setDurability(int durability) {
		this.durability = durability;
	}

	public void setRepairCost(int repairCost) {
		this.repairCost = repairCost;
	}

	public void setCustomModelData(int customModelData) {
		this.customModelData = customModelData;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public void setUnbreakable(boolean unbreakable) {
		this.unbreakable = unbreakable;
	}

	public void setHideTooltip(boolean hideTooltip) {
		this.hideTooltip = hideTooltip;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setPotionType(PotionType potionType) {
		this.potionType = potionType;
	}

	public void setFireworkEffect(FireworkEffect fireworkEffect) {
		this.fireworkEffect = fireworkEffect;
	}

	public void setSkullOwner(OfflinePlayer skullOwner) {
		this.skullOwner = skullOwner;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

	public void setTexture(String texture) {
		this.texture = texture;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public void setPotionEffects(List<PotionEffect> potionEffects) {
		this.potionEffects = potionEffects;
	}

	public void setEnchantments(Map<Enchantment, Integer> enchantments) {
		this.enchantments = enchantments;
	}

	public void setAttributes(Multimap<Attribute, AttributeModifier> attributes) {
		this.attributes = attributes;
	}

	public void setLore(List<String> lore) {
		this.lore = lore;
	}

	public void setPages(List<String> pages) {
		this.pages = pages;
	}

	public void setPatterns(List<Pattern> patterns) {
		this.patterns = patterns;
	}

	public void setFireworkEffects(List<FireworkEffect> fireworkEffects) {
		this.fireworkEffects = fireworkEffects;
	}

	public Material getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public int getAmount() {
		return amount;
	}

	public int getDurability() {
		return durability;
	}

	public int getRepairCost() {
		return repairCost;
	}

	public int getCustomModelData() {
		return customModelData;
	}

	public int getPower() {
		return power;
	}

	public boolean isUnbreakable() {
		return unbreakable;
	}

	public boolean isTooltipHidden() {
		return hideTooltip;
	}

	public Color getColor() {
		return color;
	}

	public PotionType getPotionType() {
		return potionType;
	}

	public FireworkEffect getFireworkEffect() {
		return fireworkEffect;
	}

	public OfflinePlayer getSkullOwner() {
		return skullOwner;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}

	public String getUUID() {
		return uuid;
	}

	public String getTexture() {
		return texture;
	}

	public String getSignature() {
		return signature;
	}

	public List<PotionEffect> getPotionEffects() {
		return potionEffects;
	}

	public Map<Enchantment, Integer> getEnchantments() {
		return enchantments;
	}

	public Multimap<Attribute, AttributeModifier> getAttributes() {
		return attributes;
	}

	public List<String> getLore() {
		return lore;
	}

	public List<String> getPages() {
		return pages;
	}

	public List<Pattern> getPatterns() {
		return patterns;
	}

	public List<FireworkEffect> getFireworkEffects() {
		return fireworkEffects;
	}

	public boolean equals(MagicItemData data) {
		String dataName = data.getName();
		String internalName = name;
		if (dataName != null) dataName = Util.decolorize(data.getName());
		if (internalName != null) internalName = Util.decolorize(internalName);

		Map<Enchantment, Integer> enchants = new HashMap<>(data.getEnchantments());
		if (enchants.containsKey(Enchantment.FROST_WALKER) && enchants.get(Enchantment.FROST_WALKER) == 65535) enchants.clear();

		return
				enumEquals(data.getType(), type) &&
				Objects.equals(dataName, internalName) &&
				data.getDurability() == durability &&
				data.getRepairCost() == repairCost &&
				data.getCustomModelData() == customModelData &&
				data.getPower() == power &&
				data.isUnbreakable() == unbreakable &&
				data.isTooltipHidden() == hideTooltip &&
				Objects.equals(data.getColor(), color) &&
				enumEquals(data.getPotionType(), potionType) &&
				hasEqualPotionEffects(data.getPotionEffects(), potionEffects) &&
				Objects.equals(data.getFireworkEffect(), fireworkEffect) &&
				Objects.equals(data.getSkullOwner(), skullOwner) &&
				Objects.equals(data.getTitle(), title) &&
				Objects.equals(data.getAuthor(), author) &&
				Objects.equals(enchants, enchantments) &&
				Objects.equals(data.getLore(), lore) &&
				Objects.equals(data.getPages(), pages) &&
				Objects.equals(data.getPatterns(), patterns) &&
				Objects.equals(data.getFireworkEffects(), fireworkEffects) &&
				hasEqualAttributes(data.getAttributes(), attributes);
	}

	private boolean hasEqualPotionEffects(List<PotionEffect> listA, List<PotionEffect> listB) {
		if (listA == null && listB == null) return true;
		if (listA == null && listB != null) return false;
		if (listA != null && listB == null) return false;
		return listA.containsAll(listB) && listB.containsAll(listA);
	}

	private boolean hasEqualAttributes(Multimap<Attribute, AttributeModifier> mapA, Multimap<Attribute, AttributeModifier> mapB) {
		if (mapA == null && mapB == null) return true;
		if (mapA == null && mapB != null) return false;
		if (mapA != null && mapB == null) return false;

		if (!containsAttributes(mapA, mapB)) return false;
		return containsAttributes(mapB, mapA);
	}

	public boolean containsAttributes(Multimap<Attribute, AttributeModifier> mapA, Multimap<Attribute, AttributeModifier> mapB) {
		for (Attribute attr : mapA.keys()) {
			Collection<AttributeModifier> mods = mapA.get(attr);
			if (!mapB.containsKey(attr)) return false;

			Collection<AttributeModifier> modifiers = mapB.get(attr);

			for (AttributeModifier modifier : mods) {
				double amount = modifier.getAmount();
				String name = modifier.getName();
				AttributeModifier.Operation operation = modifier.getOperation();
				EquipmentSlot slot = modifier.getSlot();

				boolean correctAttr = false;
				insideLoop: for (AttributeModifier mod : modifiers) {
					if (mod.getAmount() != amount) {
						correctAttr = false;
						continue;
					}
					if (mod.getOperation() != operation) {
						correctAttr = false;
						continue;
					}
					if (!mod.getName().equals(name)) {
						correctAttr = false;
						continue;
					}
					if (mod.getSlot() != slot) {
						correctAttr = false;
						continue;
					}

					correctAttr = true;
					break insideLoop;
				}

				if (!correctAttr) return false;
			}
		}

		return true;
	}

	public boolean enumEquals(Object o, Object object) {
		if (o == null && object == null) return true;
		if (o == null && object != null) return false;
		if (o != null && object == null) return false;
		return o == object;
	}

	@Override
	public String toString() {
		boolean previous = false;
		StringBuilder output = new StringBuilder();
		if (type != null) output.append(type.name());

		output.append("{");
		if (name != null) {
			output.append("name:'").append(name.replaceAll("'", "\\\\'")).append("'");
			previous = true;
		}

		if (amount > 1) {
			if (previous) output.append(",");
			output.append("amount:").append(amount);
			previous = true;
		}

		if (durability > -1) {
			if (previous) output.append(",");
			output.append("durability:").append(durability);
			previous = true;
		}

		if (customModelData > 0) {
			if (previous) output.append(",");
			output.append("customModelData:").append(customModelData);
			previous = true;
		}

		if (unbreakable) {
			if (previous) output.append(",");
			output.append("unbreakable:").append(unbreakable);
			previous = true;
		}

		if (color != null) {
			if (previous) output.append(",");
			String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
			output.append("color:\"").append(hex).append("\"");
			previous = true;
		}

		if (potionType != null) {
			if (previous) output.append(",");
			output.append("potion:\"").append(potionType.name()).append("\"");
			previous = true;
		}

		if (title != null) {
			if (previous) output.append(",");
			output.append("title:\"").append(title).append("\"");
			previous = true;
		}

		if (author != null) {
			if (previous) output.append(",");
			output.append("author:\"").append(author).append("\"");
			previous = true;
		}

		if (enchantments != null && !enchantments.isEmpty()) {
			if (previous) output.append(",");
			output.append("enchants:{");
			boolean previousEnchantment = false;
			for (Enchantment enchantment : enchantments.keySet()) {
				if (previousEnchantment) output.append(",");
				output.append(enchantment.getKey().getKey()).append(":").append(enchantments.get(enchantment));
				previousEnchantment = true;
			}
			output.append("}");
			previous = true;
		}

		if (lore != null && !lore.isEmpty()) {
			if (previous) output.append(",");
			output.append("lore:[");
			boolean previousLore = false;
			for (String lore : lore) {
				if (previousLore) output.append(",");
				output.append("\"").append(lore).append("\"");
				previousLore = true;
			}
			output.append("]");
			previous = true;
		}

		output.append("}");

		return output.toString();
	}

	@Override
	public MagicItemData clone() {
		MagicItemData data = new MagicItemData();
		data.setType(type);
		data.setName(name);
		data.setAmount(amount);
		data.setDurability(durability);
		data.setRepairCost(repairCost);
		data.setCustomModelData(customModelData);
		data.setPower(power);
		data.setUnbreakable(unbreakable);
		data.setHideTooltip(hideTooltip);
		data.setColor(color);
		data.setPotionType(potionType);
		data.setFireworkEffect(fireworkEffect);
		data.setSkullOwner(skullOwner);
		data.setTitle(title);
		data.setAuthor(author);
		data.setUUID(uuid);
		data.setTexture(texture);
		data.setSignature(signature);
		data.setEnchantments(enchantments);
		data.setAttributes(attributes);
		data.setLore(lore);
		data.setPages(pages);
		data.setPatterns(patterns);
		data.setPotionEffects(potionEffects);
		data.setFireworkEffects(fireworkEffects);
		return data;
	}

}
