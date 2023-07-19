package com.mullen.ethan.dungeonrunner.dungeons.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.teleporter.PlayerUseTeleporterEvent;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Class responsible for managing players in the dungeon
 */
public class DungeonPlayerManager implements Listener {

	private Main main;
	private Dungeon dungeon;
	
	private List<Player> players;
	private boolean ranFirstTimeEvents;
	
	private HashMap<Player, Integer> playerScores;
	
	public DungeonPlayerManager(Main main, Dungeon dungeon) {
		this.main = main;
		this.dungeon = dungeon;
		this.players = new ArrayList<Player>();
		this.ranFirstTimeEvents = false;
		this.playerScores = new HashMap<Player, Integer>();
		
		Bukkit.getServer().getPluginManager().registerEvents(this, main);
	
		// Gamemode watchdog
		new BukkitRunnable() {
			public void run() {
				for(World world : Bukkit.getWorlds()) {
					boolean isDungeonWorld = world.getName().equals(main.getDungeonWorld().getName());
					for(Player p : world.getPlayers()) {
						if(p.getGameMode() == GameMode.ADVENTURE && !isDungeonWorld) {
							p.setGameMode(GameMode.SURVIVAL);
						}
						if(p.getGameMode() == GameMode.SURVIVAL && isDungeonWorld) {
							p.setGameMode(GameMode.ADVENTURE);
						}
					}
				}
			}
		}.runTaskTimer(main, 0L, 40L);
		
	}

	public void addPlayer(Player p) {
		
		// First player to join
		if(players.size() == 0 && !ranFirstTimeEvents) {
			main.getDungeonWorldManager().clearDungeonItems();
			ranFirstTimeEvents = true;
		}
		
		p.teleport(dungeon.getStartLocation());
		players.add(p);

		// Give player a map
		if(p.getEquipment().getItemInOffHand() == null || p.getEquipment().getItemInOffHand().getType() == Material.AIR) {
			p.getEquipment().setItemInOffHand(dungeon.createDungeonMap());
		} else {
			p.getInventory().addItem(dungeon.createDungeonMap());
		}
		
		p.sendMessage(ChatColor.AQUA + "Joined dungeon, use \"/dungeon leave\" to get out.");
		
		dungeon.updateScoreboardValues();
		
	}

	public void removePlayer(Player p, boolean wasForced) {
		if(!players.contains(p)) return;
		
		if(wasForced) {
			p.sendMessage(ChatColor.AQUA + "You were forced to leave the dungeon.");
		} else {
			p.sendMessage(ChatColor.AQUA + "You left the dungeon.");
		}

		p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		
		dungeon.clearDungeonItems(p);
		
		players.remove(p);
		main.getQueueRoom().sendPlayerToRoom(p, false);
		
		dungeon.updateScoreboardValues();
		
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
	
	@EventHandler
	public void playerUseTeleporterEvent(PlayerUseTeleporterEvent event) {
		if(!event.getTeleporter().getID().equals(Dungeon.DUNGEON_EXIT_TELEPORTER_ID)) return;
		if(!playerInDungeon(event.getP())) return;
		event.setCancelled(true);
		removePlayer(event.getP(), false);
	}
	
	@EventHandler
	public void playerKillEntityEvent(EntityDeathEvent event) {
		if(!event.getEntity().getWorld().getName().equals(DungeonWorldManager.DUNGEON_WORLD_NAME)) return;
		Player p = event.getEntity().getKiller();
		if(!playerInDungeon(p)) return;

		if(!playerScores.containsKey(p)) {
			playerScores.put(p, 0);
		}
		int score = playerScores.get(p);
		playerScores.put(p, score+1);
		dungeon.updateScoreboardValues();
	}
	
	public HashMap<Player, Integer> getScores() {
		return playerScores;
	}
	
	public List<Player> getHighestScorers() {
        List<Map.Entry<Player, Integer>> entries = new ArrayList<>(playerScores.entrySet());
        // Sort the entries based on the integer value in descending order
        Collections.sort(entries, new Comparator<Map.Entry<Player, Integer>>() {
            @Override
            public int compare(Map.Entry<Player, Integer> entry1, Map.Entry<Player, Integer> entry2) {
                // Sort in descending order
                return entry2.getValue().compareTo(entry1.getValue());
            }
        });

        List<Player> highestScorers = new ArrayList<>();
        for (Map.Entry<Player, Integer> entry : entries) {
            highestScorers.add(entry.getKey());
        }

        return highestScorers;
    }
	
	public List<Player> players() {
		return players;
	}
	
}
