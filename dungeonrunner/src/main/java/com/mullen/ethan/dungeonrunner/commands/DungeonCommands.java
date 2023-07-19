package com.mullen.ethan.dungeonrunner.commands;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.dungeons.generator.GeneratorSettings;
import com.mullen.ethan.dungeonrunner.dungeons.generator.RoomData;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureType;
import com.mullen.ethan.dungeonrunner.dungeons.managers.RoomManager;
import com.mullen.ethan.dungeonrunner.utils.Cube;
import com.mullen.ethan.dungeonrunner.utils.Utils;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class DungeonCommands implements CommandExecutor {

	private Main main;
	
	public DungeonCommands(Main main) {
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!command.getName().equalsIgnoreCase("dungeon")) return false;
		if(args.length >= 1 && args[0].equalsIgnoreCase("generate")) {
			if(!sender.isOp()) {
				sender.sendMessage(ChatColor.RED + "You must be op to execute this command.");
				return true;
			}
			if(main.getCurrentDungeon() != null) {
				sender.sendMessage(ChatColor.RED + "There is already a dungeon open! Use \"/dungeon close\" to close it first.");
				return true;
			}
			
			String theme = "cave";
			int roomCount = 10+(new Random().nextInt(10));
			int seed = -1;
			if(args.length > 1 && args.length != 4) {
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				sender.sendMessage(ChatColor.RED + "Usage: /dungeon generate or /dungeon generate <theme> <room count> <seed>");
				return true;
			}
			if(args.length == 4) {
				if(main.getThemeManager().getTheme(args[1].toLowerCase()) == null) {
					sender.sendMessage(ChatColor.RED + "Incorrect theme \"" + args[1] + "\"");
					sender.sendMessage(ChatColor.RED + "Usage: /dungeon generate or /dungeon generate <theme> <room count> <seed>");
					return true;				
				}
				if(!Utils.isInt(args[2])) {
					sender.sendMessage(ChatColor.RED + "Argument \"" + args[2] + "\" must be an integer.");
					sender.sendMessage(ChatColor.RED + "Usage: /dungeon generate or /dungeon generate <theme> <room count> <seed>");
					return true;								
				}
				if(Integer.parseInt(args[2]) < 2) {
					sender.sendMessage(ChatColor.RED + "Integer \"" + args[2] + "\" must be greater than 2.");
					sender.sendMessage(ChatColor.RED + "Usage: /dungeon generate or /dungeon generate <theme> <room count> <seed>");
					return true;												
				}
				if(!Utils.isInt(args[3])) {
					sender.sendMessage(ChatColor.RED + "Argument \"" + args[3] + "\" must be an integer.");
					sender.sendMessage(ChatColor.RED + "Usage: /dungeon generate or /dungeon generate <theme> <room count> <seed>");
					return true;								
				}
				
				theme = args[1].toLowerCase();
				roomCount = Integer.parseInt(args[2]);
				seed = Integer.parseInt(args[3]);
			}
			
			sender.sendMessage(ChatColor.AQUA + "Generating dungeon...");

			GeneratorSettings settings = new GeneratorSettings(theme, roomCount, seed);
			Dungeon dungeon = new Dungeon(main, settings);
			dungeon.generate();
			main.setCurrentDungeon(dungeon);
			return true;
			
		} else if(args.length == 1 && args[0].equalsIgnoreCase("close")) {
			if(!sender.isOp()) {
				sender.sendMessage(ChatColor.RED + "You must be op to execute this command.");
				return true;
			}
			if(main.getCurrentDungeon() == null) {
				sender.sendMessage(ChatColor.RED + "There are no dungeons open.");
				return true;				
			}
			main.setCurrentDungeon(null);
			sender.sendMessage(ChatColor.AQUA + "Successfully closed dungeon.");
			return true;
		} else if(args.length == 1 && args[0].equalsIgnoreCase("join")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You must be a player to execute this command.");
				return true;
			}
			if(main.getCurrentDungeon() == null) {
				sender.sendMessage(ChatColor.RED + "There are no dungeons open.");
				return true;				
			}
			Player p = (Player) sender;
			main.getCurrentDungeon().addPlayer(p);
			sender.sendMessage(ChatColor.AQUA + "Joined dungeon.");
			return true;
		} else if(args.length == 1 && args[0].equalsIgnoreCase("leave")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You must be a player to execute this command.");
				return true;
			}
			if(main.getCurrentDungeon() == null) {
				sender.sendMessage(ChatColor.RED + "There are no dungeons open.");
				return true;				
			}
			Player p = (Player) sender;
			if(!main.getCurrentDungeon().isPlayerInDungeon(p)) {
				sender.sendMessage(ChatColor.RED + "You are not in a dungeon.");
				return true;
			}
			main.getCurrentDungeon().removePlayer(p, false);
			return true;
		} else if(args.length >= 1 && args[0].equalsIgnoreCase("clearplot")) {			
			if(!sender.isOp()) {
				sender.sendMessage(ChatColor.RED + "You must be op to execute this command.");
				return true;
			}
			if(args.length != 3) {
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				sender.sendMessage(ChatColor.RED + "Usage: /dungeon clearplot <plotX> <plotZ>");
			}
			int offset = 200;
			Cube cube = new Cube(new Vector3(-offset, 0, -offset), new Vector3(offset, 140, offset));
			cube.setWorld(main.getDungeonWorld());

			sender.sendMessage(ChatColor.AQUA + "Clearing plot (this will cause lag).");
			cube.fill(Material.AIR);
			return true;
		} else if(args.length == 1 && args[0].equalsIgnoreCase("qg")) {
			if(!sender.isOp()) {
				sender.sendMessage(ChatColor.RED + "You must be op to execute this command.");
				return true;
			}
			GeneratorSettings settings = new GeneratorSettings("cave", 10, 12);
			Dungeon dungeon = new Dungeon(main, settings);
			dungeon.generate(true);
			main.setCurrentDungeon(dungeon);
			return true;
		} else if(args.length == 1 && args[0].equalsIgnoreCase("test")) {
			if(!sender.isOp()) {
				sender.sendMessage(ChatColor.RED + "You must be op to execute this command.");
				return true;
			}
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You must be a player to execute this command.");
				return true;
			}
			Dungeon dungeon = main.getCurrentDungeon();
			if(dungeon == null) {
				sender.sendMessage(ChatColor.RED + "Dungeon is null.");
				return true;
			}
			Player p = (Player) sender;
			RoomManager bossRoom = null;
			for(RoomManager rm : dungeon.getRoomManagers()) {
				if(rm.getType() != StructureType.BOSS_ROOM) continue;
				bossRoom = rm;
			}
			if(bossRoom == null) {
				sender.sendMessage(ChatColor.RED + "Couldn't find boss");
				return true;
			}
			if(!dungeon.isPlayerInDungeon(p)) {
				dungeon.addPlayer(p);
			}
			RoomData parent = bossRoom.getRoomData().getParent();
			p.teleport(parent.getCube().getCenter().getWorldLocation(main.getDungeonWorld()));
			dungeon.getBossDoor().setLocked(false);
			return true;
		} else {
			sender.sendMessage(ChatColor.RED + "Failed to parse command.");
			return true;
		}
	}

}
