package com.mullen.ethan.dungeonrunner.fileloading;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureType;

import net.md_5.bungee.api.ChatColor;

public class ThemeManager {

	private Main main;
	private HashMap<String, DungeonTheme> themes;
	
	public ThemeManager(Main main) {
		
		this.main = main;
		this.themes = new HashMap<String, DungeonTheme>();

		Path themeDirectory = Paths.get(main.getDataFolder().toPath().toString() + File.separatorChar + "themes");
		File themeFile = themeDirectory.toFile();
		if(!themeFile.exists()) {
			try {
				themeFile.mkdirs();
			} catch (Exception e) {
				System.err.println("Failed to create theme folder");
				e.printStackTrace();
			}
		}
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Loading themes...");
				
		for(File subFolder : themeFile.listFiles()) {
			if(!subFolder.isDirectory()) continue; 
			String name = subFolder.getName();
			DungeonTheme theme = new DungeonTheme(name);
			
			boolean success = loadConfig(subFolder, theme) && 
					          loadStructures(subFolder, theme);
			
			if(success) {
				themes.put(name, theme);
			}

		}
		
		for(String themeName : themes.keySet()) {
			int totalRoomCount = 0;
			DungeonTheme theme = themes.get(themeName);
			for(StructureType type : StructureType.values()) {
				totalRoomCount += theme.getStructures(type).size();
			}
			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Loaded theme \"" + themeName + "\" with " + totalRoomCount + " structures.");
		}
		
	}
	
	/**
	 * Load theme config file
	 * @return Success status
	 */
	private boolean loadConfig(File subFolder, DungeonTheme theme) {
		
		// Load config file
		FileConfiguration config = main.getConfig(subFolder.getPath() + File.separatorChar + "config.yml");
		if(config == null) {
			Bukkit.getLogger().log(Level.SEVERE, "Failed to load config file for theme \"" + subFolder.getName() + "\"");	
			return false;
		}

		String materialString = config.getString("doormaterial");
		try {
			Material material = Material.valueOf(materialString.toUpperCase());
			theme.setDoorMaterial(material);
		} catch(Exception e) {
			Bukkit.getLogger().log(Level.SEVERE, "Failed to load config for theme \"" + subFolder.getName() + "\"");	
			Bukkit.getLogger().log(Level.SEVERE, "Material string \"" + materialString + "\" unrecognized.");	
			return false;
		}
		
		ConfigurationSection hordesSection = config.getConfigurationSection("hordes");
		if(hordesSection != null) {
			for(String structureType : hordesSection.getKeys(false)) {
				StructureType type = null;
				try {
					type = StructureType.valueOf(structureType.toUpperCase());
				} catch(Exception e) {
					Bukkit.getLogger().log(Level.SEVERE, "Failed to load config for theme \"" + subFolder.getName() + "\"");	
					Bukkit.getLogger().log(Level.SEVERE, "Structure type \"" + structureType + "\" unrecognized when trying to find hordes.");	
					return false;
				}
				List<String> hordes = config.getStringList("hordes." + structureType);
				theme.addHordes(type, hordes);
			}
		} 
		
		return true;
		
	}
	
	/**
	 * Load theme's structures
	 * @return Success status
	 */
	private boolean loadStructures(File subFolder, DungeonTheme theme) {
		
		// Load structures
		for(File structureTypeFolder : subFolder.listFiles()) {
			if(!structureTypeFolder.isDirectory()) continue;
			StructureType type;
			try {
				type = StructureType.valueOf(structureTypeFolder.getName().toUpperCase());
			} catch(Exception e) {
				Bukkit.getLogger().log(Level.SEVERE, "Failed to load theme \"" + subFolder.getName() + "\"");
				Bukkit.getLogger().log(Level.SEVERE, "The subfolder \"" + structureTypeFolder.getName() + "\" isn't recognized as a structure type.");					
				return false;
			}
			
			for(File structureFile : structureTypeFolder.listFiles()) {
				if(structureFile.isDirectory()) continue;
				if(!getFileExtension(structureFile.toPath()).equals("nbt")) continue;
				theme.addStructure(type, structureFile);
			}
		}	

		return true;
		
	}
	
	private static String getFileExtension(Path path) {
		String fileName = path.getFileName().toString();
		int dotIndex = fileName.lastIndexOf(".");
		if(dotIndex > 0 && dotIndex < fileName.length() - 1) {
			return fileName.substring(dotIndex + 1);
		}
		return "";
	}
	
	public List<String> getThemeNames() {
		return new ArrayList<String>(themes.keySet());
	}
	
	public DungeonTheme getTheme(String name) {
		return themes.get(name);
	}
	
}
