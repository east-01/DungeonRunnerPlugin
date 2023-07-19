package com.mullen.ethan.dungeonrunner.dungeons;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.bukkit.inventory.ItemStack;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.generator.DungeonGenerator;
import com.mullen.ethan.dungeonrunner.dungeons.generator.GeneratorSettings;
import com.mullen.ethan.dungeonrunner.dungeons.generator.RoomData;
import com.mullen.ethan.dungeonrunner.dungeons.loot.LootTableManager;
import com.mullen.ethan.dungeonrunner.dungeons.managers.DungeonItemManager;
import com.mullen.ethan.dungeonrunner.dungeons.managers.DungeonLifecycleManager;
import com.mullen.ethan.dungeonrunner.dungeons.managers.DungeonMapManager;
import com.mullen.ethan.dungeonrunner.dungeons.managers.DungeonMobManager;
import com.mullen.ethan.dungeonrunner.dungeons.managers.DungeonPlayerManager;
import com.mullen.ethan.dungeonrunner.dungeons.managers.DungeonScoreboardManager;
import com.mullen.ethan.dungeonrunner.dungeons.managers.RoomManager;
import com.mullen.ethan.dungeonrunner.fileloading.DungeonTheme;
import com.mullen.ethan.dungeonrunner.maps.DungeonMapRenderer;
import com.mullen.ethan.dungeonrunner.teleporter.Teleporter;
import com.mullen.ethan.dungeonrunner.utils.Cube;
import com.mullen.ethan.dungeonrunner.utils.Utils;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class Dungeon {

	public static String DUNGEON_EXIT_TELEPORTER_ID = "DUNGEON_EXIT_TELEPORT";
	
	private Random rand;
	
	private Main main;

	private GeneratorSettings settings;
	private DungeonGenerator dungeonGenerator;
	
	private DungeonItemManager dungeonItemManager;
	private DungeonLifecycleManager dungeonLifecycleManager;
	private DungeonPlayerManager dungeonPlayerManager;
	private DungeonScoreboardManager dungeonScoreboardManager;
	private DungeonMapManager dungeonMapManager;
	private DungeonMobManager dungeonMobManager;
	private List<RoomManager> roomManagers;
	
	private Location startLocation;
	private DungeonDoor bossDoor;
	private Cube bounds;
	
	private List<Teleporter> dungeonTeleporters;
	
	// Information about the dungeon
	private DungeonSize size;
	
	public Dungeon(Main instance, GeneratorSettings settings) {
		
		this.rand = new Random();
		
		this.main = instance;
		
		this.settings = settings;
		this.dungeonGenerator = new DungeonGenerator(instance, settings, dungeonCompleteRunnable);

		this.roomManagers = new ArrayList<RoomManager>();
		this.dungeonTeleporters = new ArrayList<Teleporter>();
		
		this.dungeonItemManager = new DungeonItemManager(instance, this);
		this.dungeonLifecycleManager = new DungeonLifecycleManager(instance, this);
		this.dungeonPlayerManager = new DungeonPlayerManager(instance, this);
		this.dungeonScoreboardManager = new DungeonScoreboardManager(instance, this);
		this.dungeonMapManager = new DungeonMapManager(instance, this);
		this.dungeonMobManager = new DungeonMobManager(instance, this);
		
	}

	public void generate() { generate(false); }
	public void generate(boolean quickGen) {
		Bukkit.broadcastMessage(ChatColor.AQUA + "Generating new dungeon, expect lag.");

		int roomCount = settings.getRoomLimit();
		size = DungeonSize.SMALL;
		if(roomCount >= 10 && roomCount < 20) {
			size = DungeonSize.MEDIUM;
		} else if(roomCount >= 20) {
			size = DungeonSize.LARGE;
		}
		
		dungeonGenerator.generate(quickGen);
	}
	
	private Runnable dungeonCompleteRunnable = new Runnable() {
		@Override
		public void run() {

			startLocation = dungeonGenerator.getStartRoom().centerRoomFloor(2).getWorldLocation(main.getDungeonWorld()).add(0.5, 0, 0.5);	

			Location teleporterLocation = null;
			RoomData startRoom = dungeonGenerator.getStartRoom();
			if(startRoom.getTeleporterLocations().size() != 0) {
				List<Vector3> teleporterOffsets = startRoom.getTeleporterLocations();
				Vector3 offset = teleporterOffsets.get(rand.nextInt(teleporterOffsets.size()));
				teleporterLocation = startRoom.getLocation().add(offset).getWorldLocation(main.getDungeonWorld()).add(0.5, 0, 0.5);
			} else {
				Vector3 startRoomCenter = dungeonGenerator.getStartRoom().centerRoomFloor(2);
				Vector3 startDoorOffset = dungeonGenerator.getStartRoom().getDoorLocations().get(0);
				Vector3 differenceRaw = startDoorOffset.subtract(startRoomCenter);
				Vector3 difference = new Vector3(Math.signum(differenceRaw.x)*2, 0, Math.signum(differenceRaw.z)*2);
				teleporterLocation = startRoomCenter.subtract(difference).getWorldLocation(main.getDungeonWorld()).add(0.5, 0, 0.5);			
			}

			// Set the destination the same because the teleport gets intercepted
			addTeleporter(main.getTeleporterManager().spawnTeleporter(DUNGEON_EXIT_TELEPORTER_ID, teleporterLocation, ChatColor.GRAY + "Out", teleporterLocation));

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
		
		for(Teleporter porter : dungeonTeleporters) {
			porter.remove();
		}
		
		// Make sure to unregister from events
		HandlerList.unregisterAll(dungeonItemManager);
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
		// Sort all rooms from furthest to nearest
		List<RoomData> allRooms = new ArrayList<RoomData>();
		allRooms.addAll(dungeonGenerator.getAllRooms());
		// Sorted list of rooms
		RoomData[] rooms = new RoomData[allRooms.size()];
		for(int pass = 0; pass < rooms.length; pass++) {
			RoomData furthestRoom = null;
			int furthestDistance = -1;
			for(RoomData room : allRooms) {
				if(furthestRoom == null || furthestDistance < room.getDistance()) {
					furthestRoom = room;
					furthestDistance = room.getDistance();
				}
			}
			rooms[pass] = furthestRoom;
			allRooms.remove(furthestRoom);
		}
		
		// Count all chests in dungeon and load up chest map
		HashMap<RoomData, List<Chest>> chestMaps = new HashMap<RoomData, List<Chest>>();
		int chestCount = 0;
		for(RoomData room : rooms) {
			List<Chest> chests = new ArrayList<Chest>();
			for(Vector3 chestOffset : room.getChestLocations()) {
				Vector3 chestWorldLoc = room.getLocation().add(chestOffset);
				Block b = chestWorldLoc.getWorldLocation(main.getDungeonWorld()).getBlock();
				if(b.getType() != Material.CHEST) continue;
				chests.add((Chest) b.getState());
				chestCount++;
			}
			chestMaps.put(room, chests);
		}
		
		// Figure out chest distribution
		float[] chestDistribution = new float[] {0.4f, 0.3f, 0.2f, 0.15f}; // chestDistribution[tier] = percentage of chests in that tier
		int[] chestAmounts = new int[LootTableManager.REGULAR_TIER_COUNT];
		int allocatedChests = 0;
		// Allocate chest tiers from highest to lowest
		// This allows us to do the chestAmounts[0] thing in a couple lines
		for(int tier = LootTableManager.REGULAR_TIER_COUNT-1; tier > 0; tier--) {
			chestAmounts[tier] = (int) Math.floor(chestCount * chestDistribution[tier]);
			allocatedChests += chestAmounts[tier];
		}
		chestAmounts[0] = chestCount - allocatedChests; // Ensures that the sum of chestAmounts[] will equal total chest count
						
		// Loop through the highest tier first, 
		for(int tier = LootTableManager.REGULAR_TIER_COUNT-1; tier >= 0; tier--) {
			for(int roomNum = 0; roomNum < rooms.length; roomNum++) {
				RoomData room = rooms[roomNum];
				List<Chest> chestOptions = chestMaps.get(room);
				if(chestOptions == null || chestOptions.size() == 0) continue;
				if(chestAmounts[tier] <= 0) break;
				
				Chest chest = chestOptions.get(chestOptions.size() > 0 ? rand.nextInt(chestOptions.size()) : 0);
				main.getLootTableManager().populateChest(chest, false, tier);
				chestMaps.get(room).remove(chest);
				chestAmounts[tier]--;
				
			}			
			// If we haven't finished filling this tier, we need to run this loop again as this tier so we can go through again
			if(chestAmounts[tier] > 0) {
				tier++;
			}
		}
	}
		
	// Wrappers so that other classes can access Manager classes from the Dungeon class
	public Location getStartLocation() { return startLocation; }
	public Cube getBounds() { return bounds; }
	public DungeonSize getSize() { return size; }
	public void setSize(DungeonSize size) { this.size = size; }

	public List<Player> getPlayersInDungeon() { return dungeonPlayerManager.players(); }
	public void addPlayer(Player p) { dungeonPlayerManager.addPlayer(p); }
	public void removePlayer(Player p, boolean wasForced) { dungeonPlayerManager.removePlayer(p, wasForced); }
	public boolean isPlayerInDungeon(Player p) { return dungeonPlayerManager.playerInDungeon(p); }
	public void sendActionBarMessage(String message) { dungeonPlayerManager.sendActionBarMessage(message); }
	public void sendTitleMessage(String title, String subtitle, int fadeIn, int stay, int fadeout) { dungeonPlayerManager.sendTitleMessage(title, subtitle, fadeIn, stay, fadeout); }
	public HashMap<Player, Integer> getScores() { return dungeonPlayerManager.getScores(); }
	public List<Player> getHighestScorers() { return dungeonPlayerManager.getHighestScorers(); }
	
	public void updateScoreboardValues() { dungeonScoreboardManager.updateScoreboardValues(); }
	
	public List<Entity> getDungeonEntities() { return dungeonMobManager.getMobs(); }
	public void addEntity(Entity e) { dungeonMobManager.addMob(e); }
	public boolean isBossSpawned() { return dungeonMobManager.isBossSpawned(); }
	public boolean isBossDefeated() { return dungeonMobManager.isBossDefeated(); }
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
		
	public ItemStack createDungeonMap() { return dungeonItemManager.createDungeonMap(); }
	public void clearDungeonItems(Player p) { dungeonItemManager.clearDungeonItems(p); }
	
	public boolean isGenerated() { return startLocation != null; }
	
	public List<RoomManager> getRoomManagers() { return roomManagers; }
	public void addTeleporter(Teleporter porter) { dungeonTeleporters.add(porter); }		
	
}