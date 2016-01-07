package Damorin;

import java.util.List;

import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import Damorin.decisionSystem.DecisionSystem;
import Damorin.decisionSystem.EnsembleDecisionSystem;
import Damorin.decisionSystem.InformedEnsembleDecisionSystem;
import Damorin.model.Position;
import core.game.Game;
import core.game.StateObservation;
import core.player.AbstractPlayer;

/**
 * The agent for the Solusar GVG-AI Competition submission (www.gvgai.net).
 * 
 * The goal for this agent is to use a variety of algorithms as part of an
 * Ensemble Decision System.
 * 
 * @author Damien Anderson (Damorin)
 *
 */
public class Agent extends AbstractPlayer {

	public static ACTIONS[] availableActions;
	public static int numberOfAvailableActions;
	public static Position position;

	private DecisionSystem decisionSystem;

	/**
	 * Constructs the {@link Agent} and pulls out the available {@link ACTIONS}
	 * available in the {@link Game}.
	 * 
	 * @param stateObs
	 *            The current state of the {@link Game}
	 * @param elapsedTimer
	 *            The amount of time remaining to make a decision
	 */
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		List<ACTIONS> actions = stateObs.getAvailableActions();
		availableActions = new ACTIONS[actions.size()];

		for (int i = 0; i < availableActions.length; ++i) {
			availableActions[i] = actions.get(i);
		}

		numberOfAvailableActions = availableActions.length;

		decisionSystem = new InformedEnsembleDecisionSystem(stateObs, elapsedTimer);
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		decisionSystem.update(stateObs);

		int action = decisionSystem.selectAction(elapsedTimer);
		
		return availableActions[action];
	}
}
