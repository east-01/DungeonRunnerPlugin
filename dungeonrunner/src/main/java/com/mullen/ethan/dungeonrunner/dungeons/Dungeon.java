package com.mullen.ethan.dungeonrunner.dungeons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.generator.DungeonGenerator;
import com.mullen.ethan.dungeonrunner.dungeons.generator.DungeonTheme;
import com.mullen.ethan.dungeonrunner.dungeons.generator.GeneratorSettings;
import com.mullen.ethan.dungeonrunner.dungeons.generator.RoomData;
import com.mullen.ethan.dungeonrunner.dungeons.loot.LootTable;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class Dungeon {

	private Main main;

	private GeneratorSettings settings;
	private DungeonGenerator dungeonGenerator;
	private DungeonPlayerManager dungeonPlayerManager;
	private DungeonMobManager dungeonMobManager;
	private List<RoomManager> roomManagers;
	
	private Location startLocation;

	public Dungeon(Main instance, GeneratorSettings settings) {
		
		this.main = instance;
		
		this.settings = settings;
		this.dungeonGenerator = new DungeonGenerator(instance, settings, dungeonCompleteRunnable);
		this.dungeonPlayerManager = new DungeonPlayerManager(instance, this);
		this.dungeonMobManager = new DungeonMobManager(instance, this);
		this.roomManagers = new ArrayList<RoomManager>();
		
	}

	public void generate() {
		Bukkit.broadcastMessage(ChatColor.AQUA + "Generating new dungeon, expect lag.");
		dungeonGenerator.generate();
	}
	
	private Runnable dungeonCompleteRunnable = new Runnable() {
		@Override
		public void run() {
			
			Vector3 startRoomCenter = dungeonGenerator.getStartRoom().getCube().getCenter();
			startLocation = new Location(main.getDungeonWorld(), startRoomCenter.x, startRoomCenter.y, startRoomCenter.z);

			fillChests();			
			
			main.getQueueRoom().setPortalOpen(true);
			main.getQueueRoom().setAnchorLevel(1);
	
			// Create room managers
			for(RoomData room : dungeonGenerator.getAllRooms()) {
				if(room.equals(dungeonGenerator.getStartRoom())) continue;
				roomManagers.add(new RoomManager(main, Dungeon.this, room));
			}		

		}
	};
	
	public void close() {
		
		// Clone the players list so that we don't get a concurrent modification exception
		List<Player> allPlayers = new ArrayList<Player>();
		allPlayers.addAll(dungeonPlayerManager.players());
		for(Player p : allPlayers) {  
			dungeonPlayerManager.removePlayer(p, true);
		}
		
		main.getQueueRoom().setPortalOpen(false);
		main.getQueueRoom().setAnchorLevel(0);
		
		dungeonMobManager.clearMobs();
		
		// Make sure to unregister from events
		HandlerList.unregisterAll(dungeonPlayerManager);
		HandlerList.unregisterAll(dungeonMobManager);
		for(RoomManager rm : roomManagers) {
			for(DungeonDoor dd : rm.getRoomsDoors()) {
				HandlerList.unregisterAll(dd);
			}
		}
		
	}
	
	public void fillChests() {
		for(RoomData room : dungeonGenerator.getAllRooms()) {
			for(Vector3 chestOffset : room.getChestLocations()) {
				Vector3 chestWorldLoc = room.getLocation().add(chestOffset);
				Block b = chestWorldLoc.getWorldLocation(main.getDungeonWorld()).getBlock();
				if(b.getType() != Material.CHEST) {
					continue;
				}
				int tierCount = main.getLootTable().length-1;
				float distancePercent = (float)room.getDistance()/(float)dungeonGenerator.getFurthestDistance();
				int tier = (int) Math.floor((distancePercent)*(tierCount));
				LootTable table = main.getLootTable()[tier];
				fillChest(b, table);
			}
		}
	}
	
	public static void fillChest(Block b, LootTable table) {
		Chest c = (Chest) b.getState();
		Inventory inv = c.getInventory();
		for(int i = 0; i < inv.getSize(); i++) {
			ItemStack item = table.pollItem();
			inv.setItem(i, item);
		}
	}
	
	// Wrappers so that other classes can access Manager classes from the Dungeon class
	public List<Player> getPlayersInDungeon() { return dungeonPlayerManager.players(); }
	public void addPlayer(Player p) { dungeonPlayerManager.addPlayer(p); }
	public void removePlayer(Player p, boolean wasForced) { dungeonPlayerManager.removePlayer(p, wasForced); }
	public boolean isPlayerInDungeon(Player p) { return dungeonPlayerManager.playerInDungeon(p); }
	
	public List<Entity> getDungeonEntities() { return dungeonMobManager.getMobs(); }
	public void addEntity(Entity e) { dungeonMobManager.addMob(e); }
	public boolean isBossSpawned() { return dungeonMobManager.isBossSpawned(); }
	public Entity getBoss() { return dungeonMobManager.getBoss(); }
	
	public DungeonTheme getDungeonTheme() { return dungeonGenerator.getSettings().getTheme(); }
	
	public Location getStartLocation() {
		return startLocation;
	}
				
}