package Damorin.model;

import core.game.Observation;

/**
 * Holds information about an individual "sprite" within the game world.
 * 
 * Sprites are entities which represents objects and characters in the game.
 * 
 * @author Damien Anderson (Damorin)
 *
 */
public class SpriteInformationImpl implements SpriteInformation {

	private Position position;
	private Boolean valid;
	private Observation observation;
	
	public SpriteInformationImpl(Observation observation) {
		this.observation = observation;
	}

	public SpriteInformationImpl(Position position, Observation observation) {
		this.position = position;
		this.observation = observation;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void setValidGoal(Boolean valid) {
		this.valid = valid;
	}

	@Override
	public boolean isValidGoal() {
		return this.valid;
	}

	@Override
	public Observation getObservation() {
		return this.observation;
	}
}
