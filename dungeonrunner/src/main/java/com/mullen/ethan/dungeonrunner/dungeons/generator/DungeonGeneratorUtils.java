package com.mullen.ethan.dungeonrunner.dungeons.generator;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureType;
import com.mullen.ethan.dungeonrunner.fileloading.DungeonTheme;
import com.mullen.ethan.dungeonrunner.utils.WeightedList;

public class DungeonGeneratorUtils {

	public static final int PLOT_SIZE = 1000;

	private Main main;
	private DungeonGenerator generator;
	
	public DungeonGeneratorUtils(Main main, DungeonGenerator generator) {
		this.main = main;
		this.generator = generator;
	}
	
	public RoomData selectParentRoom(RoomData startRoom) {
		// Do the tree walk
		RoomData currentRoom = startRoom;
		for(int j = 0; j < 1000; j++) {
			// If there's no chilren, we have to use this room
			if(currentRoom.getChildren().size() == 0) break;
			
			// Early-select random chance
			boolean canUseRoom = currentRoom.getOpenDoors().size() > 0;
			boolean earlyUse = generator.rand.nextBoolean();
			if(canUseRoom && earlyUse) break;
			
			List<RoomData> childOptions = currentRoom.getChildren();
			currentRoom = childOptions.get(generator.rand.nextInt(childOptions.size()));
			
		}
		return currentRoom;
	}
		
	public List<File> selectRoomType(RoomData parentRoom, int roomNumber) {
		GeneratorSettings settings = generator.getSettings();
		DungeonTheme theme = main.getThemeManager().getTheme(settings.getTheme());
		
		if(roomNumber == 0) return theme.getStructures(StructureType.START_ROOM);
		if(generator.getChestRoomCount() == settings.getRoomLimit()) return theme.getStructures(StructureType.BOSS_ROOM);
		
		StructureType[] initialPattern = new StructureType[] {StructureType.LARGE_ROOM, StructureType.SMALL_ROOM, StructureType.HALLWAY};
		if(parentRoom.getDistance() < initialPattern.length) {
			return theme.getStructures(initialPattern[parentRoom.getDistance()]);
		} else {
			StructureType[] pattern = new StructureType[] {StructureType.SMALL_ROOM, StructureType.LARGE_ROOM, StructureType.SMALL_ROOM, StructureType.HALLWAY};
			int dist = parentRoom.getDistance() - initialPattern.length;
			dist = dist % pattern.length;
			return theme.getStructures(pattern[dist]);
		}
		  
	}
	
	public File selectRoomFile(RoomData parentRoom, int roomNumber) {
		List<File> options = selectRoomType(parentRoom, roomNumber);
		if(options.isEmpty()) {
			main.getLogger().severe("DungeonGeneratorUtils#selectRoomFile() Room options is empty! (parentType: " + parentRoom.getType() + ", roomNumber: " + roomNumber + ")");
			return null;
		}
		
		// Count the amount of instances for each type of room in the dungeon
		HashMap<File, Integer> instances = generator.getInstances();				
		WeightedList<File> list = new WeightedList<File>();
		for(File option : options) {
			int amt = 0;
			if(instances.containsKey(option)) amt = instances.get(option);
			list.addValue(option, weightFunction(amt));
		}
		
		return options.get(generator.rand.nextInt(options.size()));
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
	
	private int weightFunction(int amtOfInstances) {
		if(amtOfInstances == 0) {
			return 100;
		} else if(amtOfInstances <= 3) {
			return -5*amtOfInstances + 30;
		} else {
			return (int) Math.max(-0.5*amtOfInstances+16.5, 3);
		}
	}
	
}
