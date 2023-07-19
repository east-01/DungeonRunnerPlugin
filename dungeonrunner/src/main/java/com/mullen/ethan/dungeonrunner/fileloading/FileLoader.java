package com.mullen.ethan.dungeonrunner.fileloading;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	
	public File loadResourceFile(String filePath) {
		try {
			InputStream inputStream = plugin.getClass().getResourceAsStream(filePath);
	        String fileNameWithExtension = filePath.substring(filePath.lastIndexOf('/') + 1);
	        String fileName = fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.'));
	        String fileExtension = fileNameWithExtension.substring(fileNameWithExtension.lastIndexOf('.') + 1);
	        File tempFile = File.createTempFile(fileName, fileExtension);
			tempFile.deleteOnExit();
	
			try (OutputStream outputStream = new FileOutputStream(tempFile)) {
			    byte[] buffer = new byte[8192];
			    int length;
			    while ((length = inputStream.read(buffer)) != -1) {
			        outputStream.write(buffer, 0, length);
			    }
			}
			return tempFile;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
