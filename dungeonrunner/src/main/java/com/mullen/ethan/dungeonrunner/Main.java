package com.mullen.ethan.dungeonrunner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.mullen.ethan.custombosses.CustomBosses;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.dungeons.DungeonCommands;
import com.mullen.ethan.dungeonrunner.dungeons.DungeonWorldManager;
import com.mullen.ethan.dungeonrunner.dungeons.loot.LootTable;
import com.mullen.ethan.dungeonrunner.dungeons.loot.LootTableGenerator;
import com.mullen.ethan.dungeonrunner.startwell.QueueRoom;
import com.mullen.ethan.dungeonrunner.startwell.StartWell;



/*
 * - You jump into the well to get into the dungeon start room, in this room is a single portal which will get you into the dungeons
 *     - Kinda like a hub world
 * - In the dungeon room you can craft a dungeon by throwing items in which will influence the generation of the dungeon
 *     - Possibly can determine seed from combination of items thrown in
 * - Once a dungeon is generated the portal will open and all of the people in the hub room can go in
 */
// Thanks https://github.com/Shynixn/StructureBlockLib
public class Main extends JavaPlugin {

	private DungeonWorldManager dungeonWorldManager;
	private DungeonCommands dungeonCommands;
	
	private Dungeon currentDungeon;
	
	private CustomBosses customBosses;
	private LootTable[] lootTables;
	
	private QueueRoom queueRoom;
	private StartWell startingWell;
	
	@Override
	public void onEnable() {
		this.dungeonWorldManager = new DungeonWorldManager(this);
		this.dungeonCommands = new DungeonCommands(this);
			
		getCommand("dungeon").setExecutor(dungeonCommands);
	
		this.lootTables = LootTableGenerator.populateLootTables();
				
		this.queueRoom = new QueueRoom(this);
		this.startingWell = new StartWell(this);
		
		this.customBosses = (CustomBosses) getServer().getPluginManager().getPlugin("CustomBosses");
		
	}

	@Override
	public void onDisable() {

		if(currentDungeon != null) currentDungeon.close();
		
	}

	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("dungeonworld") || cmd.getName().equalsIgnoreCase("dw")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You need to be a player to execute this command.");
				return true;
			}
			if(!sender.isOp()) {
				sender.sendMessage(ChatColor.RED + "You need to be op to execute this command.");
				return true;
			}
			Player p = (Player) sender;
			boolean isInDungeonWorld = p.getLocation().getWorld().getName().equals(DungeonWorldManager.DUNGEON_WORLD_NAME);
			Location loc = new Location(isInDungeonWorld ? Bukkit.getWorlds().get(0) : dungeonWorldManager.getWorld(), 0, 100, 0);
			p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 255));
			p.teleport(loc);
			return true;
		}
		return false;
    }

	public DungeonWorldManager getDungeonWorldManager() {
		return dungeonWorldManager;
	}

	public World getDungeonWorld() {
		return dungeonWorldManager.getWorld();
	}

	public Dungeon getCurrentDungeon() {
		return currentDungeon;
	}
	
	public void setCurrentDungeon(Dungeon newDungeon) {
		if(currentDungeon != null) currentDungeon.close();
		this.currentDungeon = newDungeon;
	}
	
	public CustomBosses getCustomBosses() {
		return customBosses;
	}
	
	public LootTable[] getLootTable() {
		return lootTables;
	}
	
	public void setLootTables(LootTable[] newLootTables) {
		this.lootTables = newLootTables;
	}
	
	public StartWell getStartWell() {
		return startingWell;
	}
	
	public QueueRoom getQueueRoom() {
		return queueRoom;
	}
	
}