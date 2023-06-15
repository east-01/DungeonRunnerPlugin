package com.mullen.ethan.dungeonrunner.utils;

import org.bukkit.Location;
import org.bukkit.World;

import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;

public class Vector3 {
	public float x, y, z;
	public Vector3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public Vector3() {}
	@Override
	public Vector3 clone() {
		return new Vector3(x, y, z);
	}
	@Override
	public String toString() {
		return new String(x + ", " + y + ", " + z);
	}
	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof Vector3)) return false;
		Vector3 toCompare = (Vector3) o;
		return x == toCompare.x && y == toCompare.y && z == toCompare.z;
	}
	public Vector3 add(Vector3 other) {
		return new Vector3(this.x + other.x, this.y + other.y, this.z + other.z);
	}
	public Vector3 subtract(Vector3 other) {
	    return new Vector3(this.x - other.x, this.y - other.y, this.z - other.z);
	}
	public void rotate(StructureRotation rotation) {
	    Vector3 newVec;
	    switch (rotation) {
	        case ROTATION_90:
	            newVec = new Vector3(-this.z, this.y, this.x);
	            break;
	        case ROTATION_180:
	            newVec = new Vector3(-this.x, this.y, -this.z);
	            break;
	        case ROTATION_270:
	            newVec = new Vector3(this.z, this.y, -this.x);
	            break;
	        case NONE:
	            return; // No rotation needed, return without modifying the vector
	        default:
	            return; // Invalid rotation value, return without modifying the vector
	    }
	    this.x = newVec.x;
	    this.y = newVec.y;
	    this.z = newVec.z;
	}
	public void rotate(StructureRotation rotation, Vector3 pivotPoint) {
	    Vector3 relativeVector = this.subtract(pivotPoint);
	    relativeVector.rotate(rotation);
	    this.x = relativeVector.x + pivotPoint.x;
	    this.y = relativeVector.y + pivotPoint.y;
	    this.z = relativeVector.z + pivotPoint.z;
	}
	public Location getWorldLocation(World w) {
		return new Location(w, x, y, z);
	}
}
