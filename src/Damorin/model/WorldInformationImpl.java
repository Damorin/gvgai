package Damorin.model;

import java.util.ArrayList;
import java.util.List;

import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import Damorin.Agent;
import core.game.Observation;
import core.game.StateObservation;

/**
 * Represents the information that has been gathered about the game that is
 * currently being played.
 * 
 * This includes any analysis done by the voices, in order to cut down
 * processing time.
 * 
 * @author Damien Anderson (Damorin)
 *
 */
public class WorldInformationImpl implements WorldInformation {

	private List<StateObservation> immediateStates;
	private List<ACTIONS> pathToGoal;
	private Observation goal;
	private StateObservation beginState;
	private boolean goalValidity;

	public WorldInformationImpl(StateObservation stateObs,
			ElapsedCpuTimer elapsedTimer) {
		immediateStates = new ArrayList<>();
		beginState = stateObs;
		pathToGoal = new ArrayList<>();
	}

	@Override
	public void reset() {
		this.immediateStates.clear();
	}

	@Override
	public List<StateObservation> getImmediateStates() {
		return immediateStates;
	}

	private void analyseCloseStates() {
		for (int action = 0; action < Agent.numberOfAvailableActions; action++) {
			StateObservation nextState = beginState.copy();
			nextState.advance(Agent.availableActions[action]);
			immediateStates.add(nextState);
		}
	}

	@Override
	public void update(StateObservation stateObs) {
		this.beginState = stateObs;
		analyseCloseStates();
	}

	@Override
	public boolean hasGoalBeenSet() {
		return goalValidity;
	}

	@Override
	public void setGoal(Observation observation) {
		this.goal = observation;
	}

	@Override
	public Observation getGoal() {
		return this.goal;
	}

	@Override
	public void setGoalValidity(boolean validity) {
		this.goalValidity = validity;
	}

	@Override
	public List<ACTIONS> getPathToGoal() {
		return this.pathToGoal;
	}

	@Override
	public void setPathToGoal(List<ACTIONS> pathToGoal) {
		this.pathToGoal = pathToGoal;
	}
}
