package com.mullen.ethan.dungeonrunner.dungeons.generator.structures;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class StructureData {

	private File file;
	private Vector3 size;
	private List<Vector3> doorLocations;
	private List<Vector3> chestLocations;
	private List<Vector3> teleporterLocations;
	private StructureType structureType;

	public StructureData(File file, Vector3 size, List<Vector3> doorLocations, List<Vector3> chestLocations, List<Vector3> teleporterLocations) {
		this.file = file;
		this.size = size;
		this.doorLocations = doorLocations;
		this.chestLocations = chestLocations;
		this.teleporterLocations = teleporterLocations;
		
		String parentName = file.getParentFile().getName().toUpperCase();
		try {
			this.structureType = StructureType.valueOf(parentName);
		} catch(Exception e) {
			Bukkit.getLogger().severe("Wasn't able to interpret StructureType from parent folder name \"" + parentName + "\"");
		}
	}

	public StructureData(File file) {
		this(file, new Vector3(), new ArrayList<Vector3>(), new ArrayList<Vector3>(), new ArrayList<Vector3>());
	}
	
	public StructureData clone() {
		List<Vector3> doorsCopy = new ArrayList<Vector3>();
		for(Vector3 door : doorLocations) {
			doorsCopy.add(door.clone());
		}
		List<Vector3> chestsCopy = new ArrayList<Vector3>();
		for(Vector3 chest : chestLocations) {
			chestsCopy.add(chest.clone());
		}
		List<Vector3> teleportersCopy = new ArrayList<Vector3>();
		for(Vector3 teleporter: teleporterLocations) {
			teleportersCopy.add(teleporter.clone());
		}
		return new StructureData(file, size.clone(), doorsCopy, chestsCopy, teleportersCopy);
	}
		
	public File getFile() {
		return file;
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

	public List<Vector3> getTeleporterLocations() {
		return teleporterLocations;
	}

	public void setTeleporterLocations(List<Vector3> teleporterLocations) {
		this.teleporterLocations = teleporterLocations;
	}
	
	public void addTeleporterlocation(int x, int y, int z) {
		teleporterLocations.add(new Vector3(x, y, z));
	}

	public void addTeleporterLocation(Vector3 offset) {
		teleporterLocations.add(offset);
	}

	public StructureType getStructureType() {
		return structureType;
	}
	
	public void setStructureType(StructureType type) {
		this.structureType = type;
	}
	
}
