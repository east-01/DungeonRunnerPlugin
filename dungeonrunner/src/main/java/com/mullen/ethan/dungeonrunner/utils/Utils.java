package com.mullen.ethan.dungeonrunner.utils;

import java.text.DecimalFormat;

import org.bukkit.block.BlockFace;

import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;

public class Utils {

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
