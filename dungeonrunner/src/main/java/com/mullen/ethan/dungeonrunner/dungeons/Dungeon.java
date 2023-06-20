package com.mullen.ethan.dungeonrunner.dungeons;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import com.mullen.ethan.dungeonrunner.dungeons.generator.GeneratorSettings;
import com.mullen.ethan.dungeonrunner.dungeons.generator.RoomData;
import com.mullen.ethan.dungeonrunner.dungeons.loot.LootTableGenerator;
import com.mullen.ethan.dungeonrunner.dungeons.loot.TieredLootTable;
import com.mullen.ethan.dungeonrunner.dungeons.managers.DungeonLifecycleManager;
import com.mullen.ethan.dungeonrunner.dungeons.managers.DungeonMapManager;
import com.mullen.ethan.dungeonrunner.dungeons.managers.DungeonMobManager;
import com.mullen.ethan.dungeonrunner.dungeons.managers.DungeonPlayerManager;
import com.mullen.ethan.dungeonrunner.dungeons.managers.DungeonScoreboardManager;
import com.mullen.ethan.dungeonrunner.dungeons.managers.RoomManager;
import com.mullen.ethan.dungeonrunner.fileloading.DungeonTheme;
import com.mullen.ethan.dungeonrunner.maps.DungeonMapRenderer;
import com.mullen.ethan.dungeonrunner.utils.Cube;
import com.mullen.ethan.dungeonrunner.utils.Utils;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class Dungeon {

	private Main main;

	private GeneratorSettings settings;
	private DungeonGenerator dungeonGenerator;
	
	private DungeonLifecycleManager dungeonLifecycleManager;
	private DungeonPlayerManager dungeonPlayerManager;
	private DungeonScoreboardManager dungeonScoreboardManager;
	private DungeonMapManager dungeonMapManager;
	private DungeonMobManager dungeonMobManager;
	private List<RoomManager> roomManagers;
	
	private Location startLocation;
	private DungeonDoor bossDoor;
	private Cube bounds;
	
	public Dungeon(Main instance, GeneratorSettings settings) {
		
		this.main = instance;
		
		this.settings = settings;
		this.dungeonGenerator = new DungeonGenerator(instance, settings, dungeonCompleteRunnable);

		this.roomManagers = new ArrayList<RoomManager>();

		this.dungeonLifecycleManager = new DungeonLifecycleManager(instance, this);
		this.dungeonPlayerManager = new DungeonPlayerManager(instance, this);
		this.dungeonScoreboardManager = new DungeonScoreboardManager(instance, this);
		this.dungeonMapManager = new DungeonMapManager(instance, this);
		this.dungeonMobManager = new DungeonMobManager(instance, this);
		
	}

	public void generate() { generate(false); }
	public void generate(boolean quickGen) {
		Bukkit.broadcastMessage(ChatColor.AQUA + "Generating new dungeon, expect lag.");
		dungeonGenerator.generate(quickGen);
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
				roomManagers.add(new RoomManager(main, Dungeon.this, room));
			}		
			
			bounds = Utils.getDungeonBounds(Dungeon.this);
			
			dungeonLifecycleManager.updateRoomsToComplete();
			
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
		HandlerList.unregisterAll(dungeonLifecycleManager);
		HandlerList.unregisterAll(dungeonPlayerManager);
		HandlerList.unregisterAll(dungeonMapManager);
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
				float distancePercent = (float)room.getDistance()/(float)dungeonGenerator.getFurthestDistance();
				float tier = distancePercent * LootTableGenerator.MAX_TIER;
				fillChest(b, main.getLootTable(), tier);
			}
		}
	}
	
	public static void fillChest(Block b, TieredLootTable table, float tier) {
		Chest c = (Chest) b.getState();
		Inventory inv = c.getInventory();
		Random rand = new Random();
		for(int i = 0; i < inv.getSize(); i++) {
			if(rand.nextInt(100) > 15) continue; // Only 15% of slots get filled
			ItemStack item = table.pollEntry(tier);
			inv.setItem(i, item);
		}
	}
		
	// Wrappers so that other classes can access Manager classes from the Dungeon class
	public List<Player> getPlayersInDungeon() { return dungeonPlayerManager.players(); }
	public void addPlayer(Player p) { dungeonPlayerManager.addPlayer(p); }
	public void removePlayer(Player p, boolean wasForced) { dungeonPlayerManager.removePlayer(p, wasForced); }
	public boolean isPlayerInDungeon(Player p) { return dungeonPlayerManager.playerInDungeon(p); }
	public void sendActionBarMessage(String message) { dungeonPlayerManager.sendActionBarMessage(message); }
	public void sendTitleMessage(String title, String subtitle, int fadeIn, int stay, int fadeout) { dungeonPlayerManager.sendTitleMessage(title, subtitle, fadeIn, stay, fadeout); }
	
	public List<Entity> getDungeonEntities() { return dungeonMobManager.getMobs(); }
	public void addEntity(Entity e) { dungeonMobManager.addMob(e); }
	public boolean isBossSpawned() { return dungeonMobManager.isBossSpawned(); }
	public Entity getBoss() { return dungeonMobManager.getBoss(); }

	public List<RoomManager> getDiscoveredRooms() { return dungeonLifecycleManager.getDiscoveredRooms(); }
	public void addDiscoveredRoom(RoomManager rm) { dungeonLifecycleManager.addDiscoveredRoom(rm); }
	public float getProgress() { return dungeonLifecycleManager.getProgress(); }

	public String getDungeonThemeName() { return dungeonGenerator.getSettings().getTheme(); }
	public DungeonTheme getDungeonTheme() { return main.getThemeManager().getTheme(getDungeonThemeName()); }

	public DungeonDoor getBossDoor() { return bossDoor; }
	public void setBossDoor(DungeonDoor bossDoor) { this.bossDoor = bossDoor; }	
	
	public void addMapRenderer(DungeonMapRenderer renderer) { dungeonMapManager.addRenderer(renderer); }
	public void invalidateMasterImages() { dungeonMapManager.invalidateMasterImages(); }
	
	public Location getStartLocation() { return startLocation; }
	public Cube getBounds() { return bounds; }
	
	public List<RoomManager> getRoomManagers() {
		return roomManagers;
	}
			
}