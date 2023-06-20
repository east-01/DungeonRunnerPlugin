package com.mullen.ethan.dungeonrunner.startwell;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.dungeons.generator.GeneratorSettings;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class CraftingAnchor implements Listener {
	
	private Main main;
	private QueueRoom room;
	private Vector3 anchorLocation;
	
	public CraftingAnchor(Main main, QueueRoom room, Vector3 anchorLocation) {
		this.main = main;
		this.room = room;
		this.anchorLocation = anchorLocation;
		
		main.getServer().getPluginManager().registerEvents(this, main);
		
	}
	
	public void submitCrafting() {
		
		Bukkit.broadcastMessage(ChatColor.AQUA + "Generating dungeon. Expect server lag...");
		setAnchorLevel(0);
		for(Player p : room.getPlayersInRoom()) {
			p.playSound(anchorLocation.getWorldLocation(main.getDungeonWorld()), Sound.BLOCK_ANVIL_USE, 0.8f, 0.25f);
		}
		
		// TODO: Make crafting influence generator settings
		List<String> themes = main.getThemeManager().getThemeNames();
		String theme = themes.get(new Random().nextInt(themes.size()));
		int roomCount = 15+(new Random().nextInt(15)); 
		int seed = -1;
		Dungeon dungeon = new Dungeon(main, new GeneratorSettings(theme, roomCount, seed));
		dungeon.generate();
		main.setCurrentDungeon(dungeon);
		
	}

// TODO: The dungeon crafting feature is disabled for now
//	@EventHandler(priority = EventPriority.LOW)
//	public void playerInteractEvent(PlayerInteractEvent event) {
//		
//		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
//		Block b = event.getClickedBlock();
//		if(b.getType() != Material.RESPAWN_ANCHOR) return;
//		Player p = event.getPlayer();
//		if(!room.isPlayerInRoom(p)) return;
//
//		event.setCancelled(true);
//
//		if(main.getCurrentDungeon() != null) {
//			p.sendMessage(ChatColor.RED + "You can't craft a new dungeon until the existing one is closed...");
//			return;
//		}
//		
//		Block blockBelow = b.getLocation().clone().add(0, -1, 0).getBlock();
//		if(blockBelow.getType() != Material.BARREL) return;
//		Barrel barrel = (Barrel) blockBelow.getState();
//		barrel.setCustomName(ChatColor.AQUA + "Dungeon crafter");
//		barrel.update();
//		Inventory inv = barrel.getInventory();
//		p.openInventory(inv);
//		
//	}
		
	@EventHandler
	public void buttonInteractEvent(PlayerInteractEvent event) {
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Block b = event.getClickedBlock();
		if(b.getType() != Material.STONE_BUTTON) return;
        Player p = event.getPlayer();
        if(!room.isPlayerInRoom(p)) return;
        int x = event.getClickedBlock().getX();
        int y = event.getClickedBlock().getY();
        int z = event.getClickedBlock().getZ();
        if(!anchorLocation.clone().add(new Vector3(0, 1, 0)).equals(new Vector3(x, y, z))) return;

    	if(main.getCurrentDungeon() == null) {
    		submitCrafting();
    	} else {
    		p.sendMessage(ChatColor.RED + "You can't craft a new dungeon until the existing one is closed...");
    		for(Player pr : room.getPlayersInRoom()) {
    			pr.playSound(pr, Sound.BLOCK_LAVA_EXTINGUISH, 0.8f, 0.2f);
    		}
    	}
	}

	
	@EventHandler
	public void onRespawnAnchorExplode(BlockExplodeEvent event) {
        int x = event.getBlock().getX();
        int y = event.getBlock().getY();
        int z = event.getBlock().getZ();
        if(!anchorLocation.equals(new Vector3(x, y, z))) return;
		event.setCancelled(true);
	}
	
	public void setAnchorLevel(float percentage) {
		Block anchorBlock = anchorLocation.getWorldLocation(main.getDungeonWorld()).getBlock();
		if(anchorBlock.getType() != Material.RESPAWN_ANCHOR) {
			Bukkit.getLogger().log(Level.WARNING, "Tried to setAnchorLevel to a block that isn't a RespawnAnchor.");
			return;
		}
		RespawnAnchor r = (RespawnAnchor) anchorBlock.getBlockData();
		r.setCharges((int) (r.getMaximumCharges()*percentage));
		anchorBlock.setBlockData(r);
	}
	
	@EventHandler
	public void netherPortalEnter(PlayerPortalEvent event) {
		if(!room.isPlayerInRoom(event.getPlayer())) return;
		event.setCancelled(true);
	}
	
}
