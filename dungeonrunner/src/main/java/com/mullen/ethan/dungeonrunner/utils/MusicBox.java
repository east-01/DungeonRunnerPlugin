package com.mullen.ethan.dungeonrunner.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;

import com.mullen.ethan.dungeonrunner.Main;

public enum MusicBox {
	SMALL_WIN, LARGE_WIN;
	private Main main;
	private Location loc;
	private float volume;
	public void playSong(Main main, Location loc, float volume) {
		this.main = main;
		this.loc = loc;
		this.volume = volume;
		switch(this) {
		case LARGE_WIN:
			scheduleSoundLater(10, Sound.BLOCK_NOTE_BLOCK_PLING, 1.75f);
			scheduleSoundLater(11, Sound.BLOCK_NOTE_BLOCK_PLING, 1.5f);
			scheduleSoundLater(12, Sound.BLOCK_NOTE_BLOCK_PLING, 1.25f);
			scheduleSoundLater(13, Sound.BLOCK_NOTE_BLOCK_PLING, 1f);
			scheduleSoundLater(14, Sound.BLOCK_NOTE_BLOCK_PLING, 1.25f);
			scheduleSoundLater(15, Sound.BLOCK_NOTE_BLOCK_PLING, 1.5f);
			scheduleSoundLater(16, Sound.BLOCK_NOTE_BLOCK_PLING, 1.75f);
			break;
		case SMALL_WIN:
			scheduleSoundLater(10, Sound.BLOCK_NOTE_BLOCK_BELL, 1f);
			scheduleSoundLater(11, Sound.BLOCK_NOTE_BLOCK_BELL, 1.35f);
			scheduleSoundLater(12, Sound.BLOCK_NOTE_BLOCK_BELL, 2f);
			break;
		default:
			break;
		}
	}
	private void scheduleSoundLater(long time, Sound sound, float pitch) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
			@Override
			public void run() {
				loc.getWorld().playSound(loc, sound, time, pitch);
			}
		}, time);
	}
}
