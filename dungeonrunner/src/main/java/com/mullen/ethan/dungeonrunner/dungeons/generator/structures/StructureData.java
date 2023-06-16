package com.mullen.ethan.dungeonrunner.dungeons.generator.structures;

import java.util.ArrayList;
import java.util.List;

import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class StructureData {

	private String name;
	private Vector3 size;
	private List<Vector3> doorLocations;
	private List<Vector3> chestLocations;
	private StructureType structureType;

	public StructureData(String name, Vector3 size, List<Vector3> doorLocations, List<Vector3> chestLocations) {
		setName(name);
		this.size = size;
		this.doorLocations = doorLocations;
		this.chestLocations = chestLocations;
	}

	public StructureData() {
		this("", new Vector3(), new ArrayList<Vector3>(), new ArrayList<Vector3>());
	}
	
	public StructureData clone() {
		List<Vector3> doorsCopy = new ArrayList<Vector3>();
		for(Vector3 door : doorLocations) {
			doorsCopy.add(door.clone());
		}
		List<Vector3> chestsCopy = new ArrayList<Vector3>();
		for(Vector3 chest : chestLocations) {
			chestsCopy.add(chest);
		}
		return new StructureData(name, size.clone(), doorsCopy, chestsCopy);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		// Infer structure type from name
		this.structureType = null;
		if(name.toLowerCase().contains("small_room")) {
			this.structureType = StructureType.SMALL_ROOM;
		} else if(name.toLowerCase().contains("large_room")) {
			this.structureType = StructureType.LARGE_ROOM;
		} else if(name.toLowerCase().contains("boss_room")) {
			this.structureType = StructureType.BOSS_ROOM;
		} else if(name.toLowerCase().contains("start_room")) {
			this.structureType = StructureType.START_ROOM;
		} else if(name.toLowerCase().contains("hallway")) {
			this.structureType = StructureType.HALLWAY;
		}
	}
	
	public Vector3 getSize() {
		return size;
	}

	public void setSize(Vector3 size) {
		this.size = size;
	}
	
	public void setSize(int xSize, int ySize, int zSize) {
		this.size = new Vector3(xSize, ySize, zSize);
	}

	public List<Vector3> getDoorLocations() {
		return doorLocations;
	}

	public void setDoorLocations(List<Vector3> doorLocations) {
		this.doorLocations = doorLocations;
	}

	public void addDoorlocation(int x, int y, int z) {
		doorLocations.add(new Vector3(x, y, z));
	}

	public void addDoorLocation(Vector3 offset) {
		doorLocations.add(offset);
	}
	
	public List<Vector3> getChestLocations() {
		return chestLocations;
	}

	public void setChestLocations(List<Vector3> chestLocations) {
		this.chestLocations = chestLocations;
	}
	
	public void addChestlocation(int x, int y, int z) {
		chestLocations.add(new Vector3(x, y, z));
	}

	public void addChestLocation(Vector3 offset) {
		chestLocations.add(offset);
	}
	
	public StructureType getStructureType() {
		return structureType;
	}
	
}
