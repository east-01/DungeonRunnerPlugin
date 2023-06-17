package com.mullen.ethan.dungeonrunner.dungeons.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class DungeonScoreboardManager {

	private Main main;
	private Dungeon dungeon;
		
	public DungeonScoreboardManager(Main main, Dungeon dungeon) {
		this.main = main;
		this.dungeon = dungeon;	
		new BukkitRunnable() {
			public void run() {
				tick();
			}
		}.runTaskTimer(main, 0L, 5L);
	}
	
	public void tick() {
		for(Player p : dungeon.getPlayersInDungeon()) {
			updateScoreboard(p);
		}
	}
	
	public void updateScoreboard(Player p) {
					
		ChatColor primary = ChatColor.WHITE;
		ChatColor secondary = ChatColor.AQUA;
		
		String title = ChatColor.RED + "Dungeon Raid";
		List<String> rows = new ArrayList<String>();
		rows.add("");
		rows.add(primary + "Progress: " + secondary + (int)(Math.round(dungeon.getProgress()*100)) + "%");
		rows.add(" "); // Each blank row needs to be unique
		rows.add(primary + "Players: " + secondary + dungeon.getPlayersInDungeon().size());
		rows.add(primary + "Difficulty: " + secondary + "Normal");
		
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
	
}
