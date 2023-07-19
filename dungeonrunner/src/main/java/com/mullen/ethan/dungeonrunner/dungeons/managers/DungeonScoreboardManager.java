package com.mullen.ethan.dungeonrunner.dungeons.managers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;

import net.md_5.bungee.api.ChatColor;

public class DungeonScoreboardManager {

	private Main main;
	private Dungeon dungeon;
		
	private String bossStatus;
	private HashMap<Player, Integer> playerScores;
	private List<Player> highestScorers;
	
	public DungeonScoreboardManager(Main main, Dungeon dungeon) {
		this.main = main;
		this.dungeon = dungeon;	
				
		new BukkitRunnable() {
			public void run() {
				tick();
			}
		}.runTaskTimer(main, 0L, 10L);
	}
	
	public void tick() {
		for(Player p : dungeon.getPlayersInDungeon()) {
			updateScoreboard(p);
		}
	}
	
	public void updateScoreboard(Player p) {
					
		ChatColor primary = ChatColor.WHITE;
		ChatColor secondary = ChatColor.LIGHT_PURPLE;
		
		String title = ChatColor.RED + "" + ChatColor.BOLD + "Dungeon Raid";
		List<String> rows = new ArrayList<String>();
		rows.add("");
		rows.add(primary + "Progress: " + secondary + (int)(Math.round(dungeon.getProgress()*100)) + "%");
		rows.add(primary + "Boss: " + bossStatus);
		rows.add(" "); // Each blank row needs to be unique
		rows.add(primary + "Difficulty: " + secondary + "Normal");
		rows.add("  ");
		rows.add(primary + "Players: " + secondary + dungeon.getPlayersInDungeon().size());
		// Show player scores
		for(int i = 0; i < highestScorers.size(); i++) {
			if(i >= highestScorers.size()) break;
			Player targ = highestScorers.get(i);
			ChatColor col = getPlaceColor(i);
			rows.add(col + "" + (i+1) + ". " + primary + targ.getName() + secondary + " (" + playerScores.get(p) + ")");
		}
		
		ScoreboardManager sm = Bukkit.getScoreboardManager();
		Scoreboard board = sm.getNewScoreboard();
		Objective o = board.registerNewObjective("obj", Criteria.DUMMY, title);
		o.setDisplaySlot(DisplaySlot.SIDEBAR);
		for(int i = 0; i < rows.size(); i++) {
			Score s = o.getScore(rows.get(rows.size()-i-1));
			s.setScore(i);
		}

		p.setScoreboard(board);
		
	}

	/** Update the scoreboard values. Computationally expensive so should be used sparingly. **/
	public void updateScoreboardValues() {
		this.bossStatus = getBossStatusString();
		this.playerScores = dungeon.getScores();
		this.highestScorers = dungeon.getHighestScorers();
	}
	
	private String getBossStatusString() {
		String s = ChatColor.GRAY + "Locked";
		if(!dungeon.getBossDoor().isLocked()) s = ChatColor.GREEN + "Unlocked";
		if(dungeon.isBossSpawned()) s = ChatColor.RED + "Alive";
		if(dungeon.isBossDefeated()) s = ChatColor.GOLD + "Defeated";
		return s;
	}
	
	private ChatColor getPlaceColor(int i) {
		switch(i) {
		case 0:
			return ChatColor.GOLD;
		case 1:
			return ChatColor.WHITE;
		case 2:
			return ChatColor.of(new Color(205, 127, 50));	
		default:
			return ChatColor.GRAY;
		}
	}
	
}
