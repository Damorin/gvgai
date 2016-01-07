package Damorin.model;

import java.util.List;

import ontology.Types.ACTIONS;
import core.game.Observation;
import core.game.StateObservation;

/**
 * Used to store and analyse data about the game world.
 * 
 * @author Damien Anderson (Damorin)
 *
 */
public interface WorldInformation {

	void reset();

	List<StateObservation> getImmediateStates();

	void update(StateObservation stateObs);

	boolean hasGoalBeenSet();

	void setGoalValidity(boolean validity);

	void setGoal(Observation observation);

	Observation getGoal();

	void setPathToGoal(List<ACTIONS> pathToGoal);

	List<ACTIONS> getPathToGoal();
}
