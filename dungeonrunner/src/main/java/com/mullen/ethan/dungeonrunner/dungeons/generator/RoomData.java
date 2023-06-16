package com.mullen.ethan.dungeonrunner.dungeons.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureData;
import com.mullen.ethan.dungeonrunner.utils.Cube;
import com.mullen.ethan.dungeonrunner.utils.Utils;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class RoomData implements Cloneable {

	private Random rand;
	private Main main;
	private StructureData structureData;

	private Vector3 location;
	private StructureRotation rotation;

	// The following data values are exclusive to each room and it's in world data.
	private RoomData parent;
	/** The child room and the door offset that it's associated with */
	private HashMap<RoomData, Vector3> children;
	private List<Vector3> closedDoors;
	private int distance;

	public RoomData(Main main, StructureData structureData, Vector3 location, StructureRotation rotation) {
		this.rand = new Random();
		this.main = main;
		this.structureData = structureData;
		
		this.location = location;
		this.rotation = StructureRotation.NONE;
		
		this.parent = null;
		this.children = new HashMap<>();
		this.closedDoors = new ArrayList<>();
		this.distance = 0;
	}

	public RoomData(Main main, StructureData structureData) {
		this(main, structureData, new Vector3(), StructureRotation.NONE);
	}

	public Cube getCube() {
		Vector3 structureSize = structureData.getSize();
		Vector3 endLoc = location.add(new Vector3(structureSize.x - 1, structureSize.y - 1, structureSize.z - 1));
		if(rotation != StructureRotation.NONE) endLoc.rotate(rotation, location);
		Cube cube = new Cube(location, endLoc);
		cube.setWorld(main.getDungeonWorld());
		return cube;
	}

	public BlockFace getDoorExitDirection(Vector3 doorLocOffset) {
		return getCube().getFace(location.add(doorLocOffset));
	}

	public BlockFace getDoorEntryDirection(Vector3 doorLocOffset) {
		return Utils.invertFace(getCube().getFace(location.add(doorLocOffset)));
	}

	// Getter/Setter methods:
	
	public StructureData getStructureData() {
		return structureData.clone();
	}
	
	public Vector3 getLocation() {
		return location;
	}

	public void setLocation(Vector3 location) {
		this.location = location;
	}

	public void setLocation(int x, int y, int z) {
		this.location = new Vector3(x, y, z);
	}

	public StructureRotation getRotation() {
		return rotation;
	}

	public void applyRotation(StructureRotation rotation) {
		this.rotation = rotation;
	}

	public RoomData getParent() {
		return parent;
	}

	public void setParent(RoomData parentData) {
		this.parent = parentData;
	}

	public HashMap<RoomData, Vector3> getChildrenMap() {
		return children;
	}
	
	public List<RoomData> getChildren() {
		List<RoomData> childList = new ArrayList<>();
		childList.addAll(children.keySet());
		return childList;
	}

	public void addChild(RoomData childRoom, Vector3 door) {
		children.put(childRoom, door);
	}

	public List<Vector3> getOpenDoors() {
		List<Vector3> openDoors = new ArrayList<>();
		openDoors.addAll(getDoorLocations());
		openDoors.removeAll(children.values());
		openDoors.removeAll(closedDoors);
		return openDoors;
	}

	public void addClosedDoor(Vector3 closedDoor) {
		closedDoors.add(closedDoor);
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public List<Vector3> getDoorLocations() {
		// Need to rotate the door locations before we return them
		List<Vector3> doorLocations = new ArrayList<Vector3>();
		for(Vector3 originalLoc : structureData.getDoorLocations()) {
			Vector3 newLoc = originalLoc.clone();
			newLoc.rotate(rotation);
			doorLocations.add(newLoc);
		}
		return doorLocations;
	}

	public List<Vector3> getChestLocations() {
		// Need to rotate the chest locations before we return them
		List<Vector3> chestLocations = new ArrayList<Vector3>();
		for(Vector3 originalLoc : structureData.getChestLocations()) {
			Vector3 newLoc = originalLoc.clone();
			newLoc.rotate(rotation);
			chestLocations.add(newLoc);
		}
		return chestLocations;
	}
	
	public Vector3 getDesireableSpawnLocation(int height) {
		Cube roomCube = getCube();
		for(int attempt = 0; attempt < 300; attempt++) {
			int spawnX = roomCube.getStartX() + rand.nextInt(roomCube.getEndX()-roomCube.getStartX());
			int spawnZ = roomCube.getStartZ() + rand.nextInt(roomCube.getEndZ()-roomCube.getStartZ());
			Vector3 spawnLoc = getDesireableSpawnLocation(height, spawnX, spawnZ);
			if(spawnLoc != null) {
				return spawnLoc.add(new Vector3(0.5f, 0.1f, 0.5f));
			}
		}
		return null;
	}
	
	public Vector3 getDesireableSpawnLocation(int height, float worldX, float worldZ) {
		Cube roomCube = getCube();
		// Start at maxHeight-height, because this is the first possible location that a mob can spawn
		for(int spawnY = roomCube.getEndY()-height; spawnY > roomCube.getStartY(); spawnY--) {
			// Analyze a cube of y - y+height to check for clear space.
			Cube zombieSpace = new Cube(new Vector3(worldX, spawnY, worldZ), new Vector3(worldX, spawnY+height, worldZ));
			zombieSpace.setWorld(main.getDungeonWorld());
			// If the space is empty and the block below is solid
			boolean isValidSpot = zombieSpace.isEmpty() && new Vector3(worldX, spawnY-1, worldZ).getWorldLocation(main.getDungeonWorld()).getBlock().getType() != Material.AIR;
			if(!isValidSpot) continue;
			
			boolean hasCeiling = false;
			for(int yOffset = 0; yOffset <= roomCube.getEndY()-spawnY; yOffset++) {
				boolean isSolidBlock = new Vector3(worldX, spawnY+yOffset, worldZ).getWorldLocation(main.getDungeonWorld()).getBlock().getType() != Material.AIR;
				if(isSolidBlock) {
					hasCeiling = true;
					break;
				}
			}
			if(!hasCeiling) continue;
			
			return new Vector3(worldX, spawnY, worldZ);
			
		}
		return null;
	}
	
}
