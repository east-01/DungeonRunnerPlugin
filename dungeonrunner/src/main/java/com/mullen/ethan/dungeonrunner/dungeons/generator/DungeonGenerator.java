package com.mullen.ethan.dungeonrunner.dungeons.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

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

	private Vector3 startLocation;
	private RoomData startRoom;
	private List<RoomData> allRooms;
	private GeneratorSettings settings;
	
	public DungeonGenerator(Main main, GeneratorSettings settings, Runnable completeRunnable) {

		this.main = main;
		this.rand = settings.getSeed() != -1 ? new Random(settings.getSeed()) : new Random();
		this.utils = new DungeonGeneratorUtils(main, this);
		
		this.fatalErrorFlag = false;
		
		this.sem = new Semaphore(0);
		
		this.structureManager = new StructureManager(main, sem);
		this.completeRunnable = completeRunnable;

		this.allRooms = new ArrayList<RoomData>();

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

		postProcessRooms(startRoom);

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

		DungeonTheme theme = main.getThemeManager().getTheme(settings.getTheme());
		boolean success = true;
		for(int i = 0; i < settings.getRoomLimit(); i++) {
			if(fatalErrorFlag) break;
						
			// Select room type
			List<File> roomOptions = null;
			if(i == 0) {
				roomOptions = theme.getStructures(StructureType.START_ROOM);
			} else if(i > 0 && i < settings.getRoomLimit()-1) {
				roomOptions = theme.getRooms();
			} else {
				roomOptions = theme.getStructures(StructureType.BOSS_ROOM);
			}
			File roomFile = roomOptions.get(rand.nextInt(roomOptions.size()));

			int attempt;
			for(attempt = 0; attempt < ROOM_GENERATE_ATTEMPT_LIMIT; attempt++) {
				if(fatalErrorFlag) break;
				boolean roomGenerateSuccess = generateRoom(roomFile, i);
				if(roomGenerateSuccess) {
					if(i % 3 == 0) {
						float progress = (float)i/(float)settings.getRoomLimit();
						Bukkit.getConsoleSender().sendMessage(GENERATOR_PREFIX + "Dungeon generation progress: " + ChatColor.ITALIC + Utils.formatFloat(progress*100) + "%");
						Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
							@Override
							public void run() {
								main.getQueueRoom().setAnchorLevel(progress);	
							}							
						});
					}
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

	private boolean generateRoom(File roomFile, int roomNumber) {
		try {

			if(!structureManager.hasData(roomFile)) {
				structureManager.preprocessStructure(roomFile);
				sem.acquire();
			}
			
			// Generate current room data
			RoomData currentData = new RoomData(main, structureManager.getData(roomFile));

			// If this is the start room, just place it down. No need for door math
			if(roomNumber == 0) {
				startRoom = currentData;
				startRoom.setLocation(startLocation);
				startRoom.applyRotation(StructureRotation.NONE);
								
				structureManager.generateStructure(roomFile, startLocation, startRoom.getRotation());
				sem.acquire();
				
				allRooms.add(currentData);
				return true;
			}

			RoomData parentRoom = utils.doTreeWalk(startRoom);

			// Read door locs, do math to connect doors
			Vector3 parentLoc = parentRoom.getLocation();
			List<Vector3> openDoors = parentRoom.getOpenDoors();
			// Choose random door to connect to
			Vector3 parentDoor = openDoors.get(rand.nextInt(openDoors.size()));
			Vector3 parentDoorWorldLoc = parentLoc.add(parentDoor);
			BlockFace parentDirection = parentRoom.getDoorExitDirection(parentDoor);
			
			// Solve for the correct rotation using the existing one
			int destDoorNumber = rand.nextInt(currentData.getOpenDoors().size());
			Vector3 temp_childDoorLoc = currentData.getOpenDoors().get(destDoorNumber);
			BlockFace childDirection = currentData.getDoorEntryDirection(temp_childDoorLoc);

			// If the current door direction doesn't match the desired one, solve for the correct structure rotation
			if(childDirection != parentDirection) {
				currentData.applyRotation(Utils.convertDirections(childDirection, parentDirection));
			}

			Vector3 childDoor = currentData.getOpenDoors().get(destDoorNumber);
			// Follows rule: childLoc = (parentLoc + parentDoorOffset) - childDoorOffset
			Vector3 childLoc = parentDoorWorldLoc.subtract(childDoor);

			currentData.setLocation(childLoc);
			currentData.addClosedDoor(childDoor);
			currentData.setParent(parentRoom);
			currentData.setDistance(parentRoom.getDistance() + 1);

			Cube roomCube = currentData.getCube();
			roomCube.shrink(parentRoom.getDoorExitDirection(parentDoor), 1);
			
			if(!roomCube.isEmpty()) {
				return false;
			}
			if(roomCube.getStartY() <= QueueRoom.MAX_HEIGHT) {
				return false;
			}
			
			structureManager.generateStructure(roomFile, currentData.getLocation(), currentData.getRotation());
			sem.acquire();

			allRooms.add(currentData);
			
			parentRoom.addChild(currentData, parentDoor);
			parentRoom = currentData;

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
	private void postProcessRooms(RoomData data) {
		if(fatalErrorFlag) return;
		
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
			postProcessRooms(child);
		}

	}

	public RoomData getStartRoom() {
		return startRoom;
	}

	public List<RoomData> getAllRooms() {
		return allRooms;
	}
	
	public int getFurthestDistance() {
		return utils.getFurthestDistance();
	}

	public GeneratorSettings getSettings() {
		return settings;
	}
	
}
