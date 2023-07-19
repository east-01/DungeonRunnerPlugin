package com.mullen.ethan.dungeonrunner.startwell;

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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class CraftingAnchor implements Listener {
	
	private Random rand;
	
	private Main main;
	private QueueRoom room;
	private CraftingAnchorMenu menu;
	
	public CraftingAnchor(Main main, QueueRoom room) {
		this.rand = new Random();
		this.main = main;
		this.room = room;
		this.menu = new CraftingAnchorMenu(main, room);
		
		main.getServer().getPluginManager().registerEvents(this, main);
		
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerInteractEvent(PlayerInteractEvent event) {
		
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Block b = event.getClickedBlock();
		if(b.getType() != Material.RESPAWN_ANCHOR) return;
		Player p = event.getPlayer();
		if(!room.isPlayerInRoom(p)) return;

		event.setCancelled(true);

		if(main.getCurrentDungeon() != null) {
			p.sendMessage(ChatColor.RED + "You can't craft a new dungeon until the existing one is closed...");
			return;
		}
		
		p.openInventory(menu.getCrafterInventory());
		
	}
		
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
        if(!QueueRoom.ANCHOR_LOCATION.clone().add(new Vector3(0, 1, 0)).equals(new Vector3(x, y, z))) return;

    	if(main.getCurrentDungeon() == null) {
    		menu.submitCrafting(p);
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
        if(!QueueRoom.ANCHOR_LOCATION.equals(new Vector3(x, y, z))) return;
		event.setCancelled(true);
	}
	
	public void setAnchorLevel(float percentage) {
		Block anchorBlock = QueueRoom.ANCHOR_LOCATION.getWorldLocation(main.getDungeonWorld()).getBlock();
		if(anchorBlock.getType() != Material.RESPAWN_ANCHOR) {
			Bukkit.getLogger().log(Level.WARNING, "Tried to setAnchorLevel to a block that isn't a RespawnAnchor.");
			return;
		}
		RespawnAnchor r = (RespawnAnchor) anchorBlock.getBlockData();
		r.setCharges((int) (r.getMaximumCharges()*percentage));
		anchorBlock.setBlockData(r);
	}
	
}
