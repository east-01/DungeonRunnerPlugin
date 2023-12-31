package com.mullen.ethan.dungeonrunner.dungeons.generator.structures;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

import de.tr7zw.nbtapi.NBTFile;
import de.tr7zw.nbtapi.NBTList;

public class StructureManager {

	private Main main;
	private Semaphore sem;

	private HashMap<File, StructureData> processedData;

	public StructureManager(Main instance, Semaphore sem) {
		this.main = instance;
		this.sem = sem;
		this.processedData = new HashMap<>();
	}

	/**
	 * Preprocess data for a structure.
	 * Once completed, you will be able to read the data with getData(String name)
	 * @param name The name of the structure you want to process.
	 */
	public void preprocessStructure(File file) {
		processedData.put(file, new StructureData(file));
		
		// A list of the offsets from the original locations that have oak signs
		List<Vector3> signOffsets = new ArrayList<>();
		List<Vector3> chestOffsets = new ArrayList<>();

		StructureBlockLibApi.INSTANCE.loadStructure(main)
				.at(new Location(main.getDungeonWorld(), 0, 0, 0))
				.rotation(StructureRotation.NONE)
				.onProcessBlock(part -> {

					Block b = part.getSourceBlock();
					if(b.getBlockData().getMaterial() == Material.OAK_SIGN) signOffsets.add(new Vector3(b.getX(), b.getY(), b.getZ()));
					if(b.getBlockData().getMaterial() == Material.CHEST) chestOffsets.add(new Vector3(b.getX(), b.getY(), b.getZ()));
					return true; // Return true to confirm block placement, false to cancel block placement

				})
				.loadFromPath(file.toPath())
				.onException(e -> main.getLogger().log(Level.SEVERE, "Failed to load structure.", e))
				.onResult(e -> {

					for(Vector3 offset : signOffsets) {
						Location signLoc = new Location(main.getDungeonWorld(), offset.x, offset.y, offset.z);
						Block b = signLoc.getBlock();
						boolean isReadableSign = b.getType() == Material.OAK_SIGN && (b.getState() instanceof Sign);
						if(!isReadableSign) continue;
						Sign s = (Sign) b.getState();
						String targetLine = s.getSide(Side.FRONT).getLine(0);
						if(targetLine.equalsIgnoreCase("door")) {
							processedData.get(file).addDoorLocation(offset);
						} else if(targetLine.equalsIgnoreCase("teleporter")) {
							processedData.get(file).addTeleporterLocation(offset);							
						}
					}

					if(processedData.get(file).getDoorLocations().size() == 0) {
						Bukkit.getLogger().severe("Structure " + file.getName() + " has " + processedData.get(file).getDoorLocations().size() + " doors. Rooms need to have at least one door to be valid.");
						processedData.remove(file);
						return;
					}
										
					for(Vector3 offset : chestOffsets) {
						Location signLoc = new Location(main.getDungeonWorld(), offset.x, offset.y, offset.z);
						Block b = signLoc.getBlock();
						if(b.getType() == Material.CHEST) {
							processedData.get(file).addChestLocation(offset);
						}
					}

					Vector3 structureSize = getStructureSize(file);
					processedData.get(file).setSize(structureSize);

					// Clear blocks
					for(int x = 0; x < structureSize.x; x++) {
						for(int y = 0; y < structureSize.y; y++) {
							for(int z = 0; z < structureSize.z; z++) {
								new Location(main.getDungeonWorld(), x, y, z).getBlock().setType(Material.AIR);
							}
						}
					}
					
					sem.release();

				});
	}

	public void generateStructure(File file, Vector3 location, StructureRotation rotation) {
		StructureBlockLibApi.INSTANCE.loadStructure(main)
				.at(new Location(main.getDungeonWorld(), location.x, location.y, location.z))
				.rotation(rotation)
				.loadFromPath(file.toPath())
				.onException(e -> main.getLogger().log(Level.SEVERE, "Failed to load structure.", e))
				.onResult(e -> {
					sem.release();
				});
	}

	public static void generateStructure(Main main, String name, Location loc, StructureRotation rotation, Runnable completionTask) {
		Path filePath = main.getDataFolder().toPath().resolve(name + ".nbt");
		generateStructure(main, filePath.toFile(), loc, rotation, completionTask);
	}

	public static void generateStructure(Main main, File file, Location loc, StructureRotation rotation, Runnable completionTask) {
		StructureBlockLibApi.INSTANCE.loadStructure(main)
				.at(loc)
				.rotation(rotation)
				.loadFromPath(file.toPath())
				.onException(e -> main.getLogger().log(Level.SEVERE, "Failed to load structure.", e))
				.onResult(e -> {
					if(completionTask != null) completionTask.run();
				});
	}

	public Vector3 getStructureSize(File file) {
		try {
			NBTFile nbtFile = new NBTFile(file);
			NBTList<Integer> intList = nbtFile.getIntegerList("size");
			return new Vector3(intList.get(0), intList.get(1), intList.get(2));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean hasData(File structureFile) {
		return processedData.containsKey(structureFile);
	}

	public StructureData getData(File structureFile) {
		return processedData.get(structureFile).clone();
	}

}
