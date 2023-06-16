package com.mullen.ethan.dungeonrunner.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.mullen.ethan.dungeonrunner.dungeons.managers.RoomManager;

public class DungeonRoomClearEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private RoomManager clearedRoom;
	
	public DungeonRoomClearEvent(RoomManager clearedRoom) {
		this.clearedRoom = clearedRoom;
	}
	
	public RoomManager getClearedRoom() {
		return clearedRoom;
	}
	
	@Override
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
}
