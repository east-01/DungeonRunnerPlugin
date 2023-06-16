package com.mullen.ethan.dungeonrunner.dungeons.generator.structures;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

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

	private HashMap<String, StructureData> processedData;

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
	public void preprocessStructure(String name) {
		Path filePath = main.getDataFolder().toPath().resolve(name + ".nbt");
		processedData.put(name, new StructureData());
		processedData.get(name).setName(name);
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
				.loadFromPath(filePath)
				.onException(e -> main.getLogger().log(Level.SEVERE, "Failed to load structure.", e))
				.onResult(e -> {

					for(Vector3 offset : signOffsets) {
						Location signLoc = new Location(main.getDungeonWorld(), offset.x, offset.y, offset.z);
						Block b = signLoc.getBlock();
						boolean isReadableSign = b.getType() == Material.OAK_SIGN && (b.getState() instanceof Sign);
						if(!isReadableSign) continue;
						Sign s = (Sign) b.getState();
						if(s.getSide(Side.FRONT).getLine(0).equalsIgnoreCase("door")) {
							processedData.get(name).addDoorLocation(offset);
						}
					}

					for(Vector3 offset : chestOffsets) {
						Location signLoc = new Location(main.getDungeonWorld(), offset.x, offset.y, offset.z);
						Block b = signLoc.getBlock();
						if(b.getType() == Material.CHEST) {
							processedData.get(name).addChestLocation(offset);
						}
					}

					Vector3 structureSize = getStructureSize(name);
					processedData.get(name).setSize(structureSize);

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

	public void generateStructure(String name, Vector3 location, StructureRotation rotation) {
		Path filePath = main.getDataFolder().toPath().resolve(name + ".nbt");
		StructureBlockLibApi.INSTANCE.loadStructure(main)
				.at(new Location(main.getDungeonWorld(), location.x, location.y, location.z))
				.rotation(rotation)
				.loadFromPath(filePath)
				.onException(e -> main.getLogger().log(Level.SEVERE, "Failed to load structure.", e))
				.onResult(e -> {
					sem.release();
				});
	}

	public static void generateStructure(Main main, String name, Location loc, StructureRotation rotation, Runnable completionTask) {
		Path filePath = main.getDataFolder().toPath().resolve(name + ".nbt");
		StructureBlockLibApi.INSTANCE.loadStructure(main)
				.at(loc)
				.rotation(rotation)
				.loadFromPath(filePath)
				.onException(e -> main.getLogger().log(Level.SEVERE, "Failed to load structure.", e))
				.onResult(e -> {
					if(completionTask != null) completionTask.run();
				});
	}
	
	public Vector3 getStructureSize(String name) {
		try {
			NBTFile file = new NBTFile(new File(main.getDataFolder(), name + ".nbt"));
			NBTList<Integer> intList = file.getIntegerList("size");
			return new Vector3(intList.get(0), intList.get(1), intList.get(2));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean hasData(String structureName) {
		return processedData.containsKey(structureName);
	}

	public StructureData getData(String structureName) {
		return processedData.get(structureName).clone();
	}

}
