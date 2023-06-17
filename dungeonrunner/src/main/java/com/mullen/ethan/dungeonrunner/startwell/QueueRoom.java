package com.mullen.ethan.dungeonrunner.startwell;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureManager;
import com.mullen.ethan.dungeonrunner.utils.Cube;
import com.mullen.ethan.dungeonrunner.utils.NameLabelUtils;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class QueueRoom {

	private Random rand;
	
	public static int MAX_HEIGHT = -44;
	public static Vector3 START_LOCATION = new Vector3(22.5f, -61, 14.5f);
	public static Vector3 RETURN_LOCATION = new Vector3(10.5f, -61, 14.5f);
	public static Vector3 PORTAL_LOCATION_1 = new Vector3(1, -58, 13);
	public static Vector3 PORTAL_LOCATION_2 = new Vector3(1, -52, 15);
	public static Vector3 ANCHOR_LOCATION = new Vector3(14, -62, 17);
	public static Vector3 LODESTONE_LOCATION = new Vector3(14, -62, 11);
	
	private Main main;
	private CraftingAnchor crafting;
	
	private int taskID;
	private List<Player> playersInRoom;
	
	private Cube leaveCube;
	private Cube portalCube;
	
	public QueueRoom(Main main) {
		this.rand = new Random();
		this.main = main;
		this.crafting = new CraftingAnchor(main, this, ANCHOR_LOCATION);
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
			if(leaveCube.contains(pbx, pby, pbz)) {
				toRemove.remove(p);
				main.getStartWell().sendPlayerToWell(p);
			}
			// Check for people in the portal
			if(main.getCurrentDungeon() != null && portalCube.contains(pbx, pby, pbz)) {
				toRemove.remove(p);
				main.getCurrentDungeon().addPlayer(p);
			}
		} 
		playersInRoom.removeAll(toRemove);
		
		// Show particles for leaving
		Random rand = new Random();
		float leaveCenterX = leaveCube.getCenter().x + 0.5f;
		float leaveCenterZ = leaveCube.getCenter().z + 0.5f;
		for(int y = leaveCube.getStartY(); y <= leaveCube.getEndY(); y++) {
			main.getDungeonWorld().spawnParticle(Particle.SPELL, leaveCenterX, y, leaveCenterZ, 10, rand.nextDouble()/2d, rand.nextDouble()/2d, rand.nextDouble()/2d, 0, null);
		}
		
	}
	
	public void sendPlayerToRoom(Player p) {
		if(!isRoomGenerated()) {
			generate();
		}
		addPlayerToRoom(p);
		p.teleport(getEntranceLocation());		
	}
		
	private void addPlayerToRoom(Player p) {
		if(playersInRoom.size() == 0) {
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
			e.remove();
		}
		
		World dungeonWorld = main.getDungeonWorld();
		NameLabelUtils.createTextDisplay(ChatColor.AQUA + "Leave here", RETURN_LOCATION.clone().add(new Vector3(0, 1.5f, 0)).getWorldLocation(dungeonWorld));
		Vector3 anchorTextLoc = ANCHOR_LOCATION.clone().add(new Vector3(0.5f, 2.5f, 0.5f));
		String[] anchorText = {
				ChatColor.AQUA + "Dungeon Crafting",
				ChatColor.WHITE + "Put items here to craft a dungeon",
				ChatColor.WHITE + "Press button to craft"
		};
		NameLabelUtils.createTextDisplay(anchorText, anchorTextLoc.getWorldLocation(dungeonWorld));
		StructureManager.generateStructure(main, "dungeon_queue", new Location(dungeonWorld, 0, dungeonWorld.getMinHeight(), 0), StructureRotation.NONE, null);
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
	
}
