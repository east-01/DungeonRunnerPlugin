package com.mullen.ethan.dungeonrunner.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.mullen.ethan.dungeonrunner.dungeons.managers.RoomManager;

public class DungeonRoomPopulateEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private RoomManager populatedRoom;
	
	public DungeonRoomPopulateEvent(RoomManager populatedRoom) {
		this.populatedRoom = populatedRoom;
	}
	
	public RoomManager getPopulatedRoom() {
		return populatedRoom;
	}
	
	@Override
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
}
