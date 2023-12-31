package com.mullen.ethan.dungeonrunner.utils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.dungeons.managers.RoomManager;
import com.mullen.ethan.dungeonrunner.fileloading.DungeonTheme;

public class Utils {

	public static DungeonTheme pickTheme(HashMap<DungeonTheme, Integer> themeWeights) {
		int totalWeight = 0;
		for(DungeonTheme theme : themeWeights.keySet()) {
			totalWeight += themeWeights.get(theme);
		}
		int randomWeight = new Random().nextInt(totalWeight) + 1;

		for(DungeonTheme theme : themeWeights.keySet()) {
			randomWeight -= themeWeights.get(theme);
			if (randomWeight <= 0) {
				return theme;
			}
		}
		return null;
	}
	
	public static int findRandomOpenSlot(Inventory inv) {
		Random rand = new Random();
		int slot = -1;
		for(int slotAttempt = 0; slotAttempt <= 100; slotAttempt++) {
			int testSlot = rand.nextInt(inv.getSize());
			ItemStack item = inv.getItem(testSlot);
			if(item == null || item.getType() == Material.AIR) {
				slot = testSlot;
				break;
			}
		}
		if(slot == -1) { // Safety measure incase we somehow cant find a slot
			slot = Utils.findOpenSlot(inv);
		}
		return slot;
	}
	
	public static int findOpenSlot(Inventory inv) {
		for(int i = 0; i < inv.getSize(); i++) {
			if(inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
				return i;
			}
		}
		return -1;
	}
	
	public static Cube getDungeonBounds(Dungeon d) {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for(RoomManager rm : d.getRoomManagers()) {
			Cube cube = rm.getRoomData().getCube();
			if(cube.getStartX() < minX) minX = cube.getStartX();
			if(cube.getStartY() < minY) minY = cube.getStartY();
			if(cube.getStartZ() < minZ) minZ = cube.getStartZ();
			if(cube.getEndX() > maxX) maxX = cube.getEndX();
			if(cube.getEndY() > maxY) maxY = cube.getEndY();
			if(cube.getEndZ() > maxZ) maxZ = cube.getEndZ();
		}
		return new Cube(d.getStartLocation().getWorld(), minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	public static BlockFace invertFace(BlockFace face) {
		switch(face) {
		case NORTH:
			return BlockFace.SOUTH;
		case SOUTH:
			return BlockFace.NORTH;
		case EAST:
			return BlockFace.WEST;
		case WEST:
			return BlockFace.EAST;
		default:
			return null;
		}
	}

	public static int directionToDegrees(BlockFace face) {
		switch(face) {
		case NORTH:
			return 0;
		case SOUTH:
			return 180;
		case EAST:
			return 90;
		case WEST:
			return 270;
		default:
			return -1;
		}
	}

	public static StructureRotation convertDirections(BlockFace entry, BlockFace exit) {
		int val = directionToDegrees(exit) - directionToDegrees(entry);
		if(val < 0) val += 360;
		switch(val) {
		case 0:
			return StructureRotation.NONE;
		case 90:
			return StructureRotation.ROTATION_90;
		case 180:
			return StructureRotation.ROTATION_180;
		case 270:
			return StructureRotation.ROTATION_270;
		}
		return null;
	}

	public static String formatFloat(float number) {
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        return decimalFormat.format(number);
    }
	
	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
}
