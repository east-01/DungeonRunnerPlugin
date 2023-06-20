package com.mullen.ethan.dungeonrunner.fileloading;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.mullen.ethan.dungeonrunner.Main;

public class FileLoader {

	private Main plugin;
	private HashMap<String, FileConfiguration> configs = new HashMap<>();
	
	public FileLoader(Main plugin) {
		this.plugin = plugin;
	}
	
	public FileConfiguration loadFile(String filePath) {
		File file = new File(filePath);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		configs.put(filePath, config);
		return config;
	}
	
	public FileConfiguration getConfig(String filePath) {
		FileConfiguration config = configs.get(filePath);
		if(config == null) {
			config = loadFile(filePath);
		}
		return config;
	}
		
}
