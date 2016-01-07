package Damorin.model;

import core.game.Observation;

/**
 * Stores information regarding a sprite in the game world.
 * 
 * @author Damien Anderson (Damorin)
 *
 */
public interface SpriteInformation {

	/**
	 * The current position of the sprite in the game world as a
	 * {@link Position}
	 * 
	 * @return the x and y coordinate stored in {@link Position}
	 */
	Position getPosition();

	/**
	 * Returns if this {@link Observation} is a valid goal.
	 * 
	 * @return true if valid, false if not.
	 */
	boolean isValidGoal();

	/**
	 * Set true if this {@link Observation} is valid, or false if not.
	 * 
	 * @param valid boolean true or false.
	 */
	void setValidGoal(Boolean valid);

	/**
	 * Returns the {@link Observation} held within this {@link SpriteInformation}
	 * 
	 * @return an {@link Observation}
	 */
	Observation getObservation();
}
