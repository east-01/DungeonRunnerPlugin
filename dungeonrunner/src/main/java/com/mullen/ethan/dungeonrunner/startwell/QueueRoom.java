package com.mullen.ethan.dungeonrunner.startwell;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureManager;
import com.mullen.ethan.dungeonrunner.dungeons.managers.DungeonWorldManager;
import com.mullen.ethan.dungeonrunner.teleporter.PlayerUseTeleporterEvent;
import com.mullen.ethan.dungeonrunner.teleporter.Teleporter;
import com.mullen.ethan.dungeonrunner.utils.Cube;
import com.mullen.ethan.dungeonrunner.utils.NameLabelUtils;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class QueueRoom implements Listener {

	private Random rand;
	
	public static int MAX_HEIGHT = -44;
	public static Vector3 START_LOCATION = new Vector3(23.5f, -62, 13.5f);
	public static Vector3 RETURN_LOCATION = new Vector3(18.5f, -61, 13.5f);
	public static Vector3 PORTAL_LOCATION_1 = new Vector3(1, -58, 12);
	public static Vector3 PORTAL_LOCATION_2 = new Vector3(1, -52, 14);
	public static Vector3 ANCHOR_LOCATION = new Vector3(11, -58, 16);
	public static Vector3 LODESTONE_LOCATION = new Vector3(11, -58, 10);
	public static Vector3 BOOK_LOCATION = new Vector3(12, -57, 13);
	public static Vector3 DUNGEON_ENTRANCE = new Vector3(6.5f, -58, 13.5f);
	
	public static String TELEPORTER_ID = "QUEUEROOM_RETURN_TO_OVERWORLD";
	
	private Main main;
	private CraftingAnchor crafting;
	
	private int taskID;
	private List<Player> playersInRoom;
	
	private Cube leaveCube;
	private Cube portalCube;
	
	private Teleporter exitPorter;
	
	public QueueRoom(Main main) {
		this.rand = new Random();
		this.main = main;
		this.crafting = new CraftingAnchor(main, this);
		this.playersInRoom = new ArrayList<Player>();
		
		leaveCube = new Cube(RETURN_LOCATION, RETURN_LOCATION.clone().add(new Vector3(0, 1, 0)));
		leaveCube.setWorld(main.getDungeonWorld());
		portalCube = new Cube(PORTAL_LOCATION_1, PORTAL_LOCATION_2);
		portalCube.setWorld(main.getDungeonWorld());
		
		// We can assume that all players in the dungeon world are in the queue room
		//   since dungeons automatically kick out players
		for(Player p : main.getDungeonWorld().getPlayers()) {
			addPlayerToRoom(p);
		}
		
		Bukkit.getPluginManager().registerEvents(this, main);
		
		Location origin = RETURN_LOCATION.getWorldLocation(main.getDungeonWorld());
		// Set destinatination as the origin as, when the teleport event is called the real destination will be set
		this.exitPorter = main.getTeleporterManager().spawnTeleporter(TELEPORTER_ID, origin, ChatColor.GRAY + "Back to Overworld", origin);
		
	}
	
	public void tick() {
		
		// If there are no players in the room cancel the task
		if(playersInRoom.size() == 0) {
			Bukkit.getScheduler().cancelTask(taskID);
		}
				
		List<Player> toRemove = new ArrayList<Player>();
		for(Player p : playersInRoom) {
			// Check for people trying to leave
			int pbx = p.getLocation().getBlockX();
			int pby = p.getLocation().getBlockY();
			int pbz = p.getLocation().getBlockZ();
			// Check for people in the portal
			if(main.getCurrentDungeon() != null && main.getCurrentDungeon().isGenerated() && portalCube.contains(pbx, pby, pbz)) {
				toRemove.add(p);
				main.getCurrentDungeon().addPlayer(p);
			}
		} 
		playersInRoom.removeAll(toRemove);
		
	}
	
	@EventHandler
	public void useTeleporter(PlayerUseTeleporterEvent event) {
		if(!event.getTeleporter().getID().equals(TELEPORTER_ID)) return;
		event.setCancelled(true);
		main.getStartWell().sendPlayerToWell(event.getP());
	}
	
	public void sendPlayerToRoom(Player p, boolean worldEntrance) {
		if(!isRoomGenerated()) {
			generate();
		}
		addPlayerToRoom(p);
		if(worldEntrance) {
			p.teleport(getEntranceLocation());		
		} else {
			p.teleport(getDungeonEntranceLocation());
		}
	}
		
	private void addPlayerToRoom(Player p) {
		if(playersInRoom.size() == 0 && main.isEnabled()) {
			taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
				@Override
				public void run() {
					tick();
				}
			}, 0L, 5L);
		}
		playersInRoom.add(p);
	}
	
	public void generate() {
		// Clear old armor stands
		for(Entity e : main.getDungeonWorld().getEntities()) {
			if(!(e instanceof ArmorStand)) continue;
			if(!e.hasMetadata("QUEUE_ROOM_NAME_STAND")) continue;
			e.remove();
		}
		
		World dungeonWorld = main.getDungeonWorld();
				
		Vector3 anchorTextLoc = ANCHOR_LOCATION.clone().add(new Vector3(0.5f, 1.75f, 0.5f));
		String[] anchorText = {
				ChatColor.LIGHT_PURPLE + "Dungeon Crafting",
				ChatColor.WHITE + "Put items here to craft a dungeon",
				ChatColor.WHITE + "Press button to craft"
		};		
		Vector3 lodestoneTextLoc = LODESTONE_LOCATION.clone().add(new Vector3(0.5f, 1.5f, 0.5f));
		String[] lodestoneText = {
				ChatColor.RED + "Close dungeon",
				ChatColor.WHITE + "Press button to close dungeon"
		};

		ArmorStand dungeonTome = NameLabelUtils.createTextDisplay(ChatColor.GRAY + "Dungeon Tome", BOOK_LOCATION.clone().add(new Vector3(0.5f, 1.5f, 0.5f)).getWorldLocation(dungeonWorld));
		ArmorStand[] anchor = NameLabelUtils.createTextDisplay(anchorText, anchorTextLoc.getWorldLocation(dungeonWorld));
		ArmorStand[] lodestone = NameLabelUtils.createTextDisplay(lodestoneText, lodestoneTextLoc.getWorldLocation(dungeonWorld));

		List<ArmorStand> nameStands = new ArrayList<ArmorStand>();
		nameStands.add(dungeonTome);
		nameStands.addAll(Arrays.asList(anchor));
		nameStands.addAll(Arrays.asList(lodestone));
		for(ArmorStand a : nameStands) {
			a.setMetadata("QUEUE_ROOM_NAME_STAND", new FixedMetadataValue(main, "yup"));
		}
		
		File queueRoomFile = main.getFileLoader().loadResourceFile("resources/dungeon_queue.nbt");
		StructureManager.generateStructure(main, queueRoomFile, new Location(dungeonWorld, 0, dungeonWorld.getMinHeight(), 0), StructureRotation.NONE, null);
		
	}
	
	public void setPortalOpen(boolean isOpen) {
		if(!isOpen) { 
			portalCube.fill(Material.AIR);
		} else {
			for(Block b : portalCube.getAllBlocks()) {
				b.setType(Material.NETHER_PORTAL);
				Orientable orientation = (Orientable) b.getBlockData();
				orientation.setAxis(Axis.Z);
				b.setBlockData(orientation);
			}
		}
	}
	
	private boolean warned;
	@EventHandler
	public void buttonInteractEvent(PlayerInteractEvent event) {
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Block b = event.getClickedBlock();
		if(b.getType() != Material.STONE_BUTTON) return;
        Player p = event.getPlayer();
        if(!isPlayerInRoom(p)) return;
        int x = event.getClickedBlock().getX();
        int y = event.getClickedBlock().getY();
        int z = event.getClickedBlock().getZ();
        if(!LODESTONE_LOCATION.clone().add(new Vector3(0, 1, 0)).equals(new Vector3(x, y, z))) return;

    	if(main.getCurrentDungeon() == null) {
    		p.sendMessage(ChatColor.RED + "There are no open dungeons right now.");
    		return;
    	}
		
    	if(!main.getCurrentDungeon().isGenerated()) {
    		p.sendMessage(ChatColor.RED + "Wait for the dungeon to finish generating.");
    		return;    		
    	}
    	
    	Dungeon dungeon = main.getCurrentDungeon();
    	
    	if(!warned) {
    		p.sendMessage(ChatColor.RED + "Are you sure you want to close the dungeon? Click again to confirm.");
    		warned = true;
    		return;
    	}
    	
    	dungeon.close();
    	main.setCurrentDungeon(null);
    	p.sendMessage(ChatColor.AQUA + "Dungeon closed.");
    	return;
    	
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void playerInteractEvent(PlayerInteractEvent event) {
		
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Block b = event.getClickedBlock();
		if(b.getType() != Material.LECTERN) return;
		Player p = event.getPlayer();
		if(!isPlayerInRoom(p)) return;

		event.setCancelled(true);
		main.getTome().openBook(p);
		
	}
	
	@EventHandler
	public void playerJoinEvent(PlayerJoinEvent event) {
		if(event.getPlayer().getWorld().getUID().equals(main.getDungeonWorld().getUID())) {
			addPlayerToRoom(event.getPlayer());
		}
	}
	
	@EventHandler
	public void playerUseNetherPortal(PlayerPortalEvent event) {
		if(!event.getFrom().getWorld().getName().equals(DungeonWorldManager.DUNGEON_WORLD_NAME)) return;
		event.setCancelled(true);
	}

	public boolean isRoomGenerated() {
		return ANCHOR_LOCATION.getWorldLocation(main.getDungeonWorld()).getBlock().getType() == Material.RESPAWN_ANCHOR;
	}
	
	public boolean isPlayerInRoom(Player p) {
		return playersInRoom.contains(p);
	}
	
	public List<Player> getPlayersInRoom() {
		return playersInRoom;
	}
	
	public void setAnchorLevel(float percentage) {
		crafting.setAnchorLevel(percentage);
	}
	
	public Location getEntranceLocation() {
		return START_LOCATION.getWorldLocation(main.getDungeonWorld());
	}
	
	public Location getDungeonEntranceLocation() {
		return DUNGEON_ENTRANCE.getWorldLocation(main.getDungeonWorld());
	}
	
}
