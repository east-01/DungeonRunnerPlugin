package com.mullen.ethan.dungeonrunner.dungeons.generator;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class DungeonGeneratorUtils {

	public static final int PLOT_SIZE = 1000;

	private Main main;
	private DungeonGenerator generator;
	
	public DungeonGeneratorUtils(Main main, DungeonGenerator generator) {
		this.main = main;
		this.generator = generator;
	}
	
	public RoomData doTreeWalk(RoomData startRoom) {
		// Do the tree walk
		RoomData parentRoom = startRoom;
		for(int j = 0; j < 1000; j++) {
			List<RoomData> childRooms = parentRoom.getChildren();
			// Choice to use the current room
			boolean useRoom = generator.rand.nextBoolean();
			if(parentRoom.getOpenDoors().size() == 0) {
				useRoom = false;
			} else if(childRooms.size() == 0) {
				useRoom = true;
			}

			if(useRoom) {
				break;
			} else {
				parentRoom = childRooms.get(generator.rand.nextInt(childRooms.size()));
			}
		}
		return parentRoom;
	}

	@Deprecated
	public Vector3 findAvailableStartLocation() {
		int plotX = -1;
		int plotZ = -1;
		for(int x = 0; x < 1000; x++) {
			if (plotX != -1) break;
			for(int z = 0; z < 1000; z++) {
				Block b = new Location(main.getDungeonWorld(), x * PLOT_SIZE, 80, z * PLOT_SIZE).getBlock();
				if(b.getType() == Material.AIR) {
					plotX = x;
					plotZ = z;
					break;
				}
			}
		}
		if(plotX == -1 || plotZ == -1) {
			System.err.println("ERROR: Failed to find plot location");
			return null;
		} else {
			return new Vector3(plotX*PLOT_SIZE, 80, plotZ*PLOT_SIZE);
		}
	}
	
	public int getFurthestDistance() {
		int largest = 0;
		for(RoomData roomData : generator.getAllRooms()) {
			if(roomData.getDistance() > largest) {
				largest = roomData.getDistance();
			}
		}
		return largest;
	}
	
}
