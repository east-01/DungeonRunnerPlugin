package com.mullen.ethan.dungeonrunner.hordes;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.utils.Utils;

import net.md_5.bungee.api.ChatColor;

public class HordeManager {

	private Main main;
	private HashMap<String, Horde> hordes;
	
	public HordeManager(Main main) {
		this.main = main;
		this.hordes = new HashMap<>();
		
		FileConfiguration config = main.getConfig(main.getDataFolder().toPath().toString() + File.separatorChar + "hordes.yml");
		if(config == null) {
			Bukkit.getLogger().log(Level.SEVERE, "Failed to load hordes file.");
			return;
		}
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Loading hordes...");

		for(String hordeName : config.getConfigurationSection("").getKeys(false)) {
			Horde horde = new Horde(hordeName);
			boolean isValid = true;
			for(String mobString : config.getStringList(hordeName)) {
				String[] elements = mobString.split(", ");
				if(elements.length != 4) {
					isValid = false;
					Bukkit.getLogger().log(Level.SEVERE, "Failed to load horde \"" + hordeName + "\" the amount of elements in line \"" + mobString + "\" must equal 4.");
					Bukkit.getLogger().log(Level.SEVERE, "  - MOB_NAME, weight, min amt, max amt");
					break;
				}
				if(!Utils.isInt(elements[1]) || !Utils.isInt(elements[2]) || !Utils.isInt(elements[3])) {
					isValid = false;
					Bukkit.getLogger().log(Level.SEVERE, "Failed to load horde \"" + hordeName + "\" the at least one of the required integers in line \"" + mobString + "\" is not an integer.");
					Bukkit.getLogger().log(Level.SEVERE, "  - MOB_NAME, weight, min amt, max amt");
					break;					
				}
				String name = elements[0];
				int weight = Integer.parseInt(elements[1]);
				int min = Integer.parseInt(elements[2]);
				int max = Integer.parseInt(elements[3]);
				horde.addMob(name, weight, min, max);
			}
			if(isValid) {
				addHorde(hordeName, horde);
			}
		}
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Loaded " + hordes.size() + " horde(s).");
		
	}
	
	public boolean hasHorde(String name) {
		return hordes.containsKey(name);
	}
	
	public Horde getHorde(String name) {
		return hordes.get(name);
	}
	
	public void addHorde(String name, Horde horde) {
		hordes.put(name, horde);
	}
	
}
