package com.mullen.ethan.dungeonrunner.utils;

public class Vector2 {
	public float x, y;
	public Vector2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	public Vector2() {}
	@Override
	public Vector2 clone() {
		return new Vector2(x, y);
	}
	@Override
	public String toString() {
		return new String(x + ", " + y);
	}
	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof Vector2)) return false;
		Vector2 toCompare = (Vector2) o;
		return x == toCompare.x && y == toCompare.y;
	}
	public Vector2 add(Vector2 other) {
		return new Vector2(this.x + other.x, this.y + other.y);
	}
	public Vector2 subtract(Vector2 other) {
	    return new Vector2(this.x - other.x, this.y - other.y);
	}
}
