package com.nisovin.magicspells.util;

import com.nisovin.magicspells.Spell;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class RayTrace {

	Vector origin, direction;

	public RayTrace(Vector origin, Vector direction) {
		this.origin = origin;
		this.direction = direction;
	}

	public Vector getPosition(double blocksAway) {
		return origin.clone().add(direction.clone().multiply(blocksAway));
	}

	//checks if a position is on contained within the position
	public boolean isOnLine(Vector position) {
		double t = (position.getX() - origin.getX()) / direction.getX();
		return position.getBlockY() == origin.getY() + (t * direction.getY()) && position.getBlockZ() == origin.getZ() + (t * direction.getZ());
	}

	//get all postions on a raytrace
	public ArrayList<Vector> traverse(double blocksAway, double accuracy) {
		ArrayList<Vector> positions = new ArrayList<>();
		for (double d = 0; d <= blocksAway; d += accuracy) {
			positions.add(getPosition(d));
		}
		return positions;
	}

	//intersection detection for current raytrace with return
	public Vector positionOfIntersection(Vector min, Vector max, double blocksAway, double accuracy) {
		ArrayList<Vector> positions = traverse(blocksAway, accuracy);
		for (Vector position : positions) {
			if (intersects(position, min, max)) {
				return position;
			}
		}
		return null;
	}

	//intersection detection for current raytrace
	public boolean intersects(Vector min, Vector max, double blocksAway, double accuracy) {
		ArrayList<Vector> positions = traverse(blocksAway, accuracy);
		for (Vector position : positions) {
			if (intersects(position, min, max)) {
				return true;
			}
		}
		return false;
	}

	//bounding box instead of vector
	public Vector positionOfIntersection(AABB boundingBox, double blocksAway, double accuracy) {
		ArrayList<Vector> positions = traverse(blocksAway, accuracy);
		for (Vector position : positions) {
			if (intersects(position, boundingBox.min, boundingBox.max)) {
				return position;
			}
		}
		return null;
	}

	//bounding box instead of vector
	public boolean intersects(AABB boundingBox, double blocksAway, double accuracy) {
		ArrayList<Vector> positions = traverse(blocksAway, accuracy);
		for (Vector position : positions) {
			if (intersects(position, boundingBox.min, boundingBox.max)) {
				return true;
			}
		}
		return false;
	}

	public boolean intersects(AABB boundingBox, double blocksAway, double accuracy, Spell spell, World world) {
		ArrayList<Vector> positions = traverse(blocksAway, accuracy);
		return intersects(boundingBox, positions, spell, world);
	}

	public boolean intersects(AABB boundingBox, List<Vector> positions, Spell spell, World world) {
		for (Vector position : positions) {
			Block block = world.getBlockAt(position.toLocation(world));

			if (!block.getType().isTransparent()) {
				return false;
			}
			if (intersects(position, boundingBox.min, boundingBox.max)) {
				return true;
			}
		}
		return false;
	}

	//general intersection detection
	public static boolean intersects(Vector position, Vector min, Vector max) {
		if (position.getX() < min.getX() || position.getX() > max.getX()) {
			return false;
		} else if (position.getY() < min.getY() || position.getY() > max.getY()) {
			return false;
		} else if (position.getZ() < min.getZ() || position.getZ() > max.getZ()) {
			return false;
		}
		return true;
	}

	//debug / effects
	public void highlight(World world, double blocksAway, double accuracy) {
		for (Vector position : traverse(blocksAway, accuracy)) {
			world.spawnParticle(Particle.FLAME, position.toLocation(world), 1, 0.0, 0.0, 0.0, 0.0);
		}
	}

}