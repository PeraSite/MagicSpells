package com.nisovin.magicspells.spells.buff;

import java.util.Set;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.Random;
import java.util.HashSet;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class LifewalkSpell extends BuffSpell {
	
	private Set<UUID> lifewalkers;
	private Map<Material, Integer> blocks;

	private Grower grower;
	private Random random;

	private int tickInterval;
	
	public LifewalkSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		tickInterval = getConfigInt("tick-interval", 15);

		random = ThreadLocalRandom.current();
		blocks = new HashMap<>();
		lifewalkers = new HashSet<>();

		List<String> blockList = getConfigStringList("blocks", null);
		if (blockList != null) {
			for (String str : blockList) {
				String[] string = str.split(" ");
				Material material;
				int chance = 0;
				if (string.length < 2) MagicSpells.error("LifewalkSpell " + internalName + " has an invalid block defined");

				material = Util.getMaterial(string[0]);
				if (material == null) MagicSpells.error("LifewalkSpell " + internalName + " has an invalid block defined " + string[0]);
				if (string.length >= 2 && string[1] == null) MagicSpells.error("LifewalkSpell " + internalName + " has an invalid chance defined for block " + string[0]);
				else if (string.length >= 2) chance = Integer.parseInt(string[1]);

				if (material != null && chance > 0) blocks.put(material, chance);
			}
		} else {
			blocks.put(Material.TALL_GRASS, 25);
			blocks.put(Material.FERN, 20);
			blocks.put(Material.POPPY, 15);
			blocks.put(Material.DANDELION, 10);
			blocks.put(Material.OAK_SAPLING, 5);
		}

	}

	@Override
	public boolean castBuff(LivingEntity entity, float power, String[] args) {
		lifewalkers.add(entity.getUniqueId());
		if (grower == null) grower = new Grower();
		return true;
	}

	@Override
	public boolean isActive(LivingEntity entity) {
		return lifewalkers.contains(entity.getUniqueId());
	}

	@Override
	public void turnOffBuff(LivingEntity entity) {
		lifewalkers.remove(entity.getUniqueId());
		if (!lifewalkers.isEmpty()) return;
		if (grower == null) return;
		
		grower.stop();
		grower = null;
	}
	
	@Override
	protected void turnOff() {
		lifewalkers.clear();
		if (grower == null) return;
		
		grower.stop();
		grower = null;
	}

	private class Grower implements Runnable {
		
		private int taskId;

		private Grower() {
			taskId = MagicSpells.scheduleRepeatingTask(this, tickInterval, tickInterval);
		}
		
		public void stop() {
			MagicSpells.cancelTask(taskId);
		}
		
		@Override
		public void run() {
			for (UUID id : lifewalkers) {
				Entity entity = Bukkit.getEntity(id);
				if (entity == null) continue;
				if (!entity.isValid()) continue;
				if (!(entity instanceof LivingEntity)) continue;

				LivingEntity livingEntity = (LivingEntity) entity;
				if (isExpired(livingEntity)) {
					turnOff(livingEntity);
					continue;
				}

				Block feet = livingEntity.getLocation().getBlock();
				Block ground = feet.getRelative(BlockFace.DOWN);

				if (!BlockUtils.isAir(feet.getType())) continue;
				if (ground.getType() != Material.DIRT && ground.getType() != Material.GRASS_BLOCK) continue;
				if (ground.getType() == Material.DIRT) ground.setType(Material.GRASS_BLOCK);

				int rand = random.nextInt(100);

				for (Material m : blocks.keySet()) {
					int chance = blocks.get(m);

					if (rand > chance) continue;

					feet.setType(m);
					addUseAndChargeCost(livingEntity);

					rand = random.nextInt(100);
				}
			}
		}
	}

}
