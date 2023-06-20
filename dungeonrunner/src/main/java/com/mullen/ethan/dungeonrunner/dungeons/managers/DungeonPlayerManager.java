package com.mullen.ethan.dungeonrunner.dungeons.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.maps.DungeonMapRenderer;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Class responsible for managing players in the dungeon
 */
public class DungeonPlayerManager implements Listener {

	private Main main;
	private Dungeon dungeon;
	
	private List<Player> players;

	public DungeonPlayerManager(Main main, Dungeon dungeon) {
		this.main = main;
		this.dungeon = dungeon;
		this.players = new ArrayList<Player>();
		Bukkit.getServer().getPluginManager().registerEvents(this, main);
	}

	public void addPlayer(Player p) {
		
		p.teleport(dungeon.getStartLocation());
		players.add(p);

		// Give player a map
		MapView view = Bukkit.createMap(main.getDungeonWorld());
		// Clear other map renderers
		for(MapRenderer renderer : view.getRenderers()) {
			view.removeRenderer(renderer);
		}
		DungeonMapRenderer renderer = new DungeonMapRenderer(main);
		dungeon.addMapRenderer(renderer);
		view.addRenderer(renderer);
		ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
		MapMeta meta = (MapMeta) mapItem.getItemMeta();
		meta.setMapView(view);
		mapItem.setItemMeta(meta);
		
		p.getInventory().addItem(mapItem);
		
		p.sendMessage(ChatColor.AQUA + "Joined dungeon, use \"/dungeon leave\" to get out.");
		
	}

	public void removePlayer(Player p, boolean wasForced) {
		if(!players.contains(p)) return;
		
		if(wasForced) {
			p.sendMessage(ChatColor.AQUA + "You were forced to leave the dungeon.");
		} else {
			p.sendMessage(ChatColor.AQUA + "You left the dungeon.");
		}

		p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		
		players.remove(p);
		main.getQueueRoom().sendPlayerToRoom(p);
		
	}
		
	/**
	 * Check if the player is in the dungeon
	 */
	public boolean playerInDungeon(Player p) {
		return players.contains(p);
	}
	
	public void sendActionBarMessage(String message) {
		for(Player p : players) {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
		}
	}
	
	public void sendTitleMessage(String title, String subtitle, int fadeIn, int stay, int fadeout) {
		for(Player p : players) {
			p.sendTitle(title, subtitle, fadeIn, stay, fadeout);
		}		
	}
	
	/**
	 * Event for when the player dies. 
	 * DOES NOT REMOVE PLAYER FROM DUNGEON, playerRespawn does this when they respawn as a part of the dungeon
	 * @param event
	 */
	@EventHandler
	public void playerDeathEvent(PlayerDeathEvent event) {
		if(!playerInDungeon(event.getEntity())) return;
		event.setDeathMessage(ChatColor.AQUA + event.getEntity().getDisplayName() + " died trying to raid a dungeon.");
	}
		
	@EventHandler
	public void playerRespawnEvent(PlayerRespawnEvent event) {
		if(!playerInDungeon(event.getPlayer())) return;
		removePlayer(event.getPlayer(), true);
		event.setRespawnLocation(main.getQueueRoom().getEntranceLocation());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if(!playerInDungeon(event.getPlayer())) return;
		removePlayer(event.getPlayer(), true);		
	}
	
	public List<Player> players() {
		return players;
	}
	
}
