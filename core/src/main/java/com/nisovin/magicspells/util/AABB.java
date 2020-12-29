package com.nisovin.magicspells.util;

import net.minecraft.server.v1_15_R1.AxisAlignedBB;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class AABB {

	//min and max points of hit box
	Vector max;
	Vector min;

	AABB(Vector min, Vector max) {
		this.max = max;
		this.min = min;
	}

	AABB(Entity entity) {
		AxisAlignedBB bb = ((CraftEntity) entity).getHandle().getBoundingBox();
		min = new Vector(bb.minX, bb.minY, bb.minZ);
		max = new Vector(bb.maxX, bb.maxY, bb.maxZ);
	}

	public AABB(BoundingBox bb) {
		min = new Vector(bb.getMinX(), bb.getMinY(), bb.getMinZ());
		max = new Vector(bb.getMaxX(), bb.getMaxY(), bb.getMaxZ());
	}

	public Vector midPoint() {
		return max.clone().add(min).multiply(0.5);
	}

}
