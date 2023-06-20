package com.mullen.ethan.dungeonrunner.fileloading;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;

import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureType;

public class DungeonTheme {

	private String name;
	private HashMap<StructureType, List<File>> structures;
	private Material doorMaterial;
	private HashMap<StructureType, List<String>> hordes;
	
	public DungeonTheme(String name) {
		this.name = name;
		this.structures = new HashMap<StructureType, List<File>>();
		this.doorMaterial = Material.STONE;
		this.hordes = new HashMap<StructureType, List<String>>();
	}
	
	public String getName() {
		return name;
	}
	
	public void addStructure(StructureType type, File file) {
		if(!structures.containsKey(type)) {
			structures.put(type, new ArrayList<File>());
		}
		structures.get(type).add(file);
	}
	
	public List<File> getStructures(StructureType type) {
		return structures.get(type);
	}
	
	public List<File> getRooms() {
		List<File> rooms = new ArrayList<File>();
		rooms.addAll(structures.get(StructureType.HALLWAY));
		rooms.addAll(structures.get(StructureType.SMALL_ROOM));
		rooms.addAll(structures.get(StructureType.LARGE_ROOM));
		return rooms;
	}
	
	public Material getDoorMaterial() {
		return doorMaterial;
	}
	
	public void setDoorMaterial(Material doorMaterial) {
		this.doorMaterial = doorMaterial;
	}
	
	public List<String> getHordes(StructureType type) {
		if(!hordes.containsKey(type)) {
			hordes.put(type, new ArrayList<String>());
		}
		return hordes.get(type);
	}
	
	public void addHorde(StructureType type, String hordeName) {
		getHordes(type).add(hordeName);
	}
	
	public void addHordes(StructureType type, List<String> hordeNames) {
		getHordes(type).addAll(hordeNames);
	}
	
}
