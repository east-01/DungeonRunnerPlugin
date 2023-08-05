package com.mullen.ethan.dungeonrunner.dungeons.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureManager;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureType;
import com.mullen.ethan.dungeonrunner.fileloading.DungeonTheme;
import com.mullen.ethan.dungeonrunner.startwell.QueueRoom;
import com.mullen.ethan.dungeonrunner.utils.Cube;
import com.mullen.ethan.dungeonrunner.utils.Utils;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class DungeonGenerator {

	public static final int ROOM_GENERATE_ATTEMPT_LIMIT = 100;
	public static final int RADIUS = 200;
	public static final String GENERATOR_PREFIX = ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "DungeonGenerator" + ChatColor.DARK_AQUA + "] " + ChatColor.AQUA;
	
	public Random rand;

	private Main main;
	private StructureManager structureManager;
	private DungeonGeneratorUtils utils;
	/** A runnable that is executed when the dungeon is done generating */
	private Runnable completeRunnable;

	private boolean fatalErrorFlag;
	
	private Semaphore sem;

	private GeneratorSettings settings;
	private Vector3 startLocation;
	
	private RoomData startRoom;
	private RoomData bossRoom;
	private List<RoomData> allRooms;
	private HashMap<File, Integer> instances; // A counter for each instance of a room type
	private int chestRoomCount; // The count of SMALL_ROOM and LARGE_ROOM structures
	
	public DungeonGenerator(Main main, GeneratorSettings settings, Runnable completeRunnable) {

		this.main = main;
		this.rand = settings.getSeed() != -1 ? new Random(settings.getSeed()) : new Random();
		this.utils = new DungeonGeneratorUtils(main, this);
		
		this.fatalErrorFlag = false;
		
		this.sem = new Semaphore(0);
		
		this.structureManager = new StructureManager(main, sem);
		this.completeRunnable = completeRunnable;

		this.allRooms = new ArrayList<RoomData>();
		this.instances = new HashMap<>();
		this.chestRoomCount = 0;
		
		// Search for open plot
		this.startLocation = new Vector3(0, 80, 0);
		this.settings = settings;
			
	}

	public void generate() {generate(false);}
	
	public void generate(boolean quickClear) {
		Bukkit.getConsoleSender().sendMessage(GENERATOR_PREFIX + "Generating a new dungeon, clearing blocks...");
		int maxHeight = 160;
		int minHeight = QueueRoom.MAX_HEIGHT + 1;
		
		// Clear blocks. Once that's done, start the generate thread
		if(!quickClear) {
			int increment = 30;
			int counter = 0;
			for (int y = minHeight; y < maxHeight; y += increment) {
			    final int currentHeight = y; // Store the current height in a final variable for the anonymous inner class
			    Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
			        @Override
			        public void run() {
			            int startY = currentHeight + 1;
			            int endY = currentHeight + increment;
			            Cube c = new Cube(new Vector3(-RADIUS, startY, -RADIUS), new Vector3(RADIUS, endY, RADIUS));
			            c.setWorld(main.getDungeonWorld());
			            c.fill(Material.AIR);
			            
			            if(endY >= maxHeight) {
			        		Bukkit.getConsoleSender().sendMessage(GENERATOR_PREFIX + "Starting generate thread.");
			        		generateThread.start();
			            }
			            
			        }
			    }, 10L * counter);
			    counter++;
			}
		} else {
            Cube c = new Cube(new Vector3(-RADIUS, minHeight, -RADIUS), new Vector3(RADIUS, maxHeight, RADIUS));
            c.setWorld(main.getDungeonWorld());
            c.fill(Material.AIR);
            generateThread.start();
		}
	}

	public void generationComplete(boolean success) {
		generateThread.interrupt();

		postProcessRooms();

		if(success) {
    		Bukkit.getConsoleSender().sendMessage(GENERATOR_PREFIX + "Dungeon generate complete.");
			completeRunnable.run();
		}
	}

	/*
	 * Thread structure:
	 *   generate() gets called, starting the generate thread
	 *   generateStructure called
	 *     StructureBlockLibAPI set up, blocks NOT placed yet.
	 *   SEMAPHORE DOWN: Stops generate thread from continuing until RoomData populated
	 *   StructureBlockLibAPI does its thing, data populated
	 *   onResult lambda expression called
	 *   SEMAPHORE UP: Generate thread can continue as normal
	 */

	private Thread generateThread = new Thread(() -> {

		// Generate start room
		try {
			DungeonTheme theme = main.getThemeManager().getTheme(settings.getTheme());
			File startRoomFile = theme.getStructures(StructureType.START_ROOM).get(rand.nextInt(theme.getStructures(StructureType.START_ROOM).size()));
			startRoom = loadgetRoomData(startRoomFile);
			startRoom.setLocation(startLocation);
			startRoom.applyRotation(StructureRotation.NONE);
							
			structureManager.generateStructure(startRoomFile, startLocation, startRoom.getRotation());
			sem.acquire();
			
			allRooms.add(startRoom);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Generate other rooms
		boolean success = true;
		while(chestRoomCount < settings.getRoomLimit() || bossRoom == null) {
			if(fatalErrorFlag) break;
			
			int attempt;
			for(attempt = 0; attempt < ROOM_GENERATE_ATTEMPT_LIMIT; attempt++) {
				if(fatalErrorFlag) break;
				boolean roomGenerateSuccess = generateRoom();
				if(roomGenerateSuccess) {
					// Add to instances map
					RoomData room = allRooms.get(allRooms.size()-1);
					int amt = 0;
					if(instances.containsKey(room.getFile())) amt = instances.get(room.getFile());
					instances.put(room.getFile(), amt + 1);
					// Add to room count
					if(room.getType() == StructureType.SMALL_ROOM || room.getType() == StructureType.LARGE_ROOM) chestRoomCount++;
					// Check if it's the boss room
					if(room.getType() == StructureType.BOSS_ROOM) bossRoom = room;
					// Print progress
					float progress = (float)chestRoomCount/(float)settings.getRoomLimit();
					Bukkit.getConsoleSender().sendMessage(GENERATOR_PREFIX + "Dungeon generation progress: " + ChatColor.ITALIC + Utils.formatFloat(progress*100) + "%");
					Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
						@Override
						public void run() {
							main.getQueueRoom().setAnchorLevel(progress);	
						}							
					});
					break;
				}
			}

			if(attempt >= ROOM_GENERATE_ATTEMPT_LIMIT-1) {
				Bukkit.getConsoleSender().sendMessage("ERROR: Failed dungeon generation due to room generation limit being reached.");
				success = false;
				break;
			}

		}

		boolean successCopy = success;
		// This is used to make sure that the generation complete is called in sync with the main server thread.
		Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
			@Override
			public void run() {
				generationComplete(successCopy);
			}
		});

	});

	/**
	 * Generates a new room. Will algorithmically determine which type of room this should be.
	 * 
	 * Room generation is divided into three phases:
	 *   1. Pick a parent room
	 *   2. Create the child ("current") room
	 *   3. Verify choices and then generate the room if passing
	 * @return Success status
	 */
	private boolean generateRoom() {
		try {

			int roomNumber = allRooms.size();

//		  ##### PHASE 1: Pick a parent room #####
			RoomData parentRoom = utils.selectParentRoom(startRoom);

			// Read door locs, do math to connect doors
			Vector3 parentLoc = parentRoom.getLocation();
			List<Vector3> openDoors = parentRoom.getOpenDoors();
			if(openDoors.isEmpty()) return false;
			// Choose random door to connect to
			Vector3 parentDoor = openDoors.get(rand.nextInt(openDoors.size()));
			Vector3 parentDoorWorldLoc = parentLoc.add(parentDoor);
			BlockFace parentDirection = parentRoom.getDoorExitDirection(parentDoor);
			
//		  ##### PHASE 2: Create current room #####
			File roomFile = utils.selectRoomFile(parentRoom, roomNumber);
			if(roomFile == null) return false;			
			RoomData currentRoom = loadgetRoomData(roomFile);
			
			// Solve for the correct rotation using the existing one
			int destDoorNumber = rand.nextInt(currentRoom.getOpenDoors().size());
			Vector3 temp_childDoorLoc = currentRoom.getOpenDoors().get(destDoorNumber);
			BlockFace childDirection = currentRoom.getDoorEntryDirection(temp_childDoorLoc);

			// If the current door direction doesn't match the desired one, solve for the correct structure rotation
			if(childDirection != parentDirection) {
				currentRoom.applyRotation(Utils.convertDirections(childDirection, parentDirection));
			}
			
			Vector3 childDoor = currentRoom.getOpenDoors().get(destDoorNumber);
			// Follows rule: childLoc = (parentLoc + parentDoorOffset) - childDoorOffset
			Vector3 childLoc = parentDoorWorldLoc.subtract(childDoor);

			currentRoom.setLocation(childLoc);
			currentRoom.addClosedDoor(childDoor);
			currentRoom.setParent(parentRoom);
			currentRoom.setDistance(parentRoom.getDistance() + 1);

//		  ##### PHASE 3: Verify room and generate #####
			Cube roomCube = currentRoom.getCube();
			roomCube.shrink(parentRoom.getDoorExitDirection(parentDoor), 1);
			
			if(!roomCube.isEmpty()) {
				return false;
			}
			if(roomCube.getStartY() <= QueueRoom.MAX_HEIGHT) {
				return false;
			}
			
			structureManager.generateStructure(roomFile, currentRoom.getLocation(), currentRoom.getRotation());
			sem.acquire();

			allRooms.add(currentRoom);
			
			parentRoom.addChild(currentRoom, parentDoor);

		} catch(Exception e) {
			e.printStackTrace();
			this.fatalErrorFlag = true;
			return false;
		}
		return true;
	}

	/**
	 * Recursively postprocess rooms starting from given RoomData
	 */
	private void postProcessRooms() {
		if(fatalErrorFlag) return;
		
		// Prune hallway leaf-rooms
		List<RoomData> toRemove = new ArrayList<>();
		for(RoomData room : allRooms) {
			if(room.getChildren().size() > 0) continue; // Has children, isn't a leaf room
			if(room.getType() != StructureType.HALLWAY) continue;
			
			room.getCube().fill(Material.AIR);
			room.getParent().removeChild(room);
			toRemove.add(room);
		}
		allRooms.removeAll(toRemove);

		// Post process other rooms individually
		postProcessRoomsRecursive(startRoom);
		
	}
	
	private void postProcessRoomsRecursive(RoomData data) {
		
		// Remove keyword signs
		List<Vector3> offsetsToRemove = new ArrayList<>();
		offsetsToRemove.addAll(data.getDoorLocations());
		offsetsToRemove.addAll(data.getTeleporterLocations());
		for(Vector3 doorOffset : offsetsToRemove) {
			Vector3 worldLoc = new Vector3(data.getLocation().x + doorOffset.x, data.getLocation().y + doorOffset.y, data.getLocation().z + doorOffset.z);
			Block targBlock = worldLoc.getWorldLocation(main.getDungeonWorld()).getBlock();
			if(targBlock.getType() != Material.OAK_SIGN) continue;
			targBlock.setType(Material.AIR);
		}

		// Seal open doors
		for(Vector3 doorOffset : data.getOpenDoors()) {
			Vector3 worldLoc = new Vector3(data.getLocation().x + doorOffset.x, data.getLocation().y + doorOffset.y, data.getLocation().z + doorOffset.z);

			// Check which direction the door is
			BlockFace facing = data.getDoorExitDirection(doorOffset);
			boolean isXDirection = facing == BlockFace.NORTH || facing == BlockFace.SOUTH;
			
			Cube cube = null;
			if(isXDirection) {
				cube = new Cube(new Vector3(worldLoc.x - 2, worldLoc.y - 1, worldLoc.z), new Vector3(worldLoc.x + 2, worldLoc.y + 3, worldLoc.z));
			} else {
				cube = new Cube(new Vector3(worldLoc.x, worldLoc.y - 1, worldLoc.z - 2), new Vector3(worldLoc.x, worldLoc.y + 3, worldLoc.z + 2));
			}
			cube.setWorld(main.getDungeonWorld());
			cube.fill(main.getThemeManager().getTheme(settings.getTheme()).getDoorMaterial());
		}
		
		// Make recursive call
		for(RoomData child : data.getChildren()) {
			postProcessRoomsRecursive(child);
		}

	}
	
	/**
	 * Get the room data for the associated file.
	 * If the structure manager doesn't have the data, it will be loaded then returned.
	 */
	private RoomData loadgetRoomData(File roomFile) {
		if(!structureManager.hasData(roomFile)) {
			structureManager.preprocessStructure(roomFile);
			try {
				sem.acquire();
			} catch (InterruptedException e) {
				Bukkit.getLogger().log(Level.SEVERE, "Failed to acquire semaphore when loading RoomData.");
				e.printStackTrace();
				return null;
			}
		}
		
		// Generate current room data
		RoomData currentRoom = new RoomData(main, structureManager.getData(roomFile));
		return currentRoom;
	}
	
	public GeneratorSettings getSettings() { return settings; }
	public RoomData getStartRoom() { return startRoom; }
	public List<RoomData> getAllRooms() { return allRooms; }

	public HashMap<File, Integer> getInstances() { return instances; }
	public int getChestRoomCount() { return chestRoomCount; }
	
	// Utils wrapper
	public int getFurthestDistance() { return utils.getFurthestDistance(); }
	
}
