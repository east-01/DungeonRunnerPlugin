package com.mullen.ethan.dungeonrunner.teleporter;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerUseTeleporterEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private Player p;
	private Teleporter teleporter;
	private Location destination;
	
	private boolean cancelled;
	
	public PlayerUseTeleporterEvent(Player p, Teleporter teleporter) {
		this.p = p;
		this.teleporter = teleporter;
		this.destination = teleporter.getDestination();
		this.cancelled = false;
	}
		
	@Override
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}

	public Player getP() {
		return p;
	}

	public Teleporter getTeleporter() {
		return teleporter;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
	
	public Location getDestination() {
		return destination;
	}
	
	public void setDestination(Location destination) {
		this.destination = destination;
	}
	
}
