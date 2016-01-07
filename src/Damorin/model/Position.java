package Damorin.model;

/**
 * Provides an X and Y Coordinate for a sprite in the game world.
 * 
 * @author Damien Anderson (Damorin)
 *
 */
public class Position {
	
	private int x;
	private int y;
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

}
