package com.mullen.ethan.dungeonrunner.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * A cube class that allows for useful manipulation
 * @author mulle
 *
 */
public class Cube {

	private World world;
	private int startX, startY, startZ;
	private int endX, endY, endZ;

	public Cube(World world, int startX, int startY, int startZ, int endX, int endY, int endZ) {
		this.world = world;
		this.startX = startX;
		this.startY = startY;
		this.startZ = startZ;
		this.endX = endX;
		this.endY = endY;
		this.endZ = endZ;
	}

	public Cube(Vector3 start, Vector3 end) {
		this.startX = (int) Math.min(start.x, end.x);
		this.startY = (int) Math.min(start.y, end.y);
		this.startZ = (int) Math.min(start.z, end.z);
		this.endX = (int) Math.max(start.x, end.x);
		this.endY = (int) Math.max(start.y, end.y);
		this.endZ = (int) Math.max(start.z, end.z);
	}

	public Vector3 getCenter() {
		float centerX = (float) (startX + (endX-startX)/2.0);
		float centerY = (float) (startY + (endY-startY)/2.0);
		float centerZ = (float) (startZ + (endZ-startZ)/2.0);
		return new Vector3(centerX, centerY, centerZ);
	}

	/*
	 * Get the face a certain point is on the cube
	 */
	public BlockFace getFace(Vector3 point) {
		if(point.x == startX) {
			return BlockFace.WEST;
		} else if(point.x == endX) {
			return BlockFace.EAST;
		} else if(point.z == startZ) {
			return BlockFace.NORTH;
		} else if(point.z == endZ) {
			return BlockFace.SOUTH;
		} else {
			System.err.println("Failed a getFace() computation.");
			return null;
		}
	}

	public List<Block> getAllBlocks() {
		if(world == null) {
			System.err.println("Failed to get all blocks because world is null.");
			return null;
		}
		List<Block> blocks = new ArrayList<Block>();
		for(int x = startX; x <= endX; x++) {
			for(int y = startY; y <= endY; y++) {
				for(int z = startZ; z <= endZ; z++) {
					blocks.add(new Location(world, x, y, z).getBlock());
				}
			}
		}
		return blocks;
	}
	
	public void fill(Material m) {
		if(world == null) {
			System.err.println("Failed to fill because world is null.");
			return;
		}
		for(int y = startY; y <= endY; y++) {
			for(int x = startX; x <= endX; x++) {
				for(int z = startZ; z <= endZ; z++) {
					new Location(world, x, y, z).getBlock().setType(m);
				}
			}
		}
	}

	public void frame(Material m) {
		if(world == null) {
			System.err.println("Failed to frame because world is null.");
			return;
		}
		for(int x = startX; x <= endX; x++) {
			for(int y = startY; y <= endY; y++) {
				for(int z = startZ; z <= endZ; z++) {
					boolean isAtFrame = (x == startX || x == endX) && (y == startY || y == endY) && (z == startZ || z == endZ);
					if(isAtFrame) new Location(world, x, y, z).getBlock().setType(m);
				}
			}
		}
	}
	
	/**
	 * Replace all blocks with material toReplace with material replaceWith
	 * @param toReplace The material to replace
	 * @param replaceWith The material to replace with
	 * @return A list of affected blocks
	 */
	public List<Block> replace(Material toReplace, Material replaceWith) {
		List<Block> affectedBlocks = new ArrayList<Block>();
		if(world == null) {
			System.err.println("Failed to replace because world is null.");
			return affectedBlocks;
		}
		for(int x = startX; x <= endX; x++) {
			for(int y = startY; y <= endY; y++) {
				for(int z = startZ; z <= endZ; z++) {
					Block b = new Location(world, x, y, z).getBlock();
					if(b.getType() == toReplace) {
						b.setType(replaceWith);
						affectedBlocks.add(b);
					}
				}
			}
		}
		return affectedBlocks;
	}

	public boolean isEmpty() {
		if (world == null) {
			System.err.println("Failed to test for empty because world is null.");
			return true;
		}
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				for (int z = startZ; z <= endZ; z++) {
					if (new Location(world, x, y, z).getBlock().getType() != Material.AIR)
						return false;
				}
			}
		}
		return true;
	}

	public boolean isNonSolid() {
		if (world == null) {
			System.err.println("Failed to test for empty because world is null.");
			return true;
		}
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				for (int z = startZ; z <= endZ; z++) {
					if (new Location(world, x, y, z).getBlock().getType().isSolid())
						return false;
				}
			}
		}
		return true;
	}

	public boolean isEmpty(List<Cube> whitelist) {
		if(world == null) {
			System.err.println("Failed to test for empty because world is null.");
			return true;
		}
		for(int x = startX; x <= endX; x++) {
			for(int y = startY; y <= endY; y++) {
				for(int z = startZ; z <= endZ; z++) {
					
					if(new Location(world, x, y, z).getBlock().getType() == Material.AIR) continue;

					// means the block is NOT air
					
					boolean isWhitelisted = false;
					for(Cube white : whitelist) {
						if(white.contains(x, y, z)) {
							isWhitelisted = true;
							break;
						}
					}
					if(!isWhitelisted) return false;
					
				}
			}
		}
		return true;		
	}
	
	/**
	 * Shrinks the cubes bounds by a certain amount in a certain direction
	 * @param direction The direction that the cube will shrink towards
	 * @param amount The amount that the cube will shrunk by
	 */
	public void shrink(BlockFace direction, int amount) {
		switch(direction) {
		case NORTH:
			endZ -= amount;
			break;
		case EAST:
			startX += amount;
			break;
		case SOUTH:
			startZ += amount;
			break;
		case WEST:
			endX -= amount;
			break;
		default:
			break;

		}
	}

	public boolean contains(float locX, float locY, float locZ) {
		return locX >= startX && locX <= endX &&
			   locY >= startY && locY <= endY &&
			   locZ >= startZ && locZ <= endZ;
	}
	
	public boolean contains(Location loc) {
		return contains((float)loc.getX(), (float)loc.getY(), (float)loc.getZ());
	}
	
	@Override
	public String toString() {
		return new String("Cube: (" + new Vector3(startX, startY, startZ).toString() + ") - (" + new Vector3(endX, endY, endZ).toString() + ")");
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public int getStartX() {
		return startX;
	}

	public void setStartX(int startX) {
		this.startX = startX;
	}

	public int getStartY() {
		return startY;
	}

	public void setStartY(int startY) {
		this.startY = startY;
	}

	public int getStartZ() {
		return startZ;
	}

	public void setStartZ(int startZ) {
		this.startZ = startZ;
	}

	public int getEndX() {
		return endX;
	}

	public void setEndX(int endX) {
		this.endX = endX;
	}

	public int getEndY() {
		return endY;
	}

	public void setEndY(int endY) {
		this.endY = endY;
	}

	public int getEndZ() {
		return endZ;
	}

	public void setEndZ(int endZ) {
		this.endZ = endZ;
	}

}
