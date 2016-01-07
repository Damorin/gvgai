package newDamorin;

import java.util.List;
import java.util.Random;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer {

	public static int NUM_ACTIONS;
	public static Random random;
	public static List<ACTIONS> actions;
	public static double K = Math.sqrt(2.);

	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		actions = stateObs.getAvailableActions();
		NUM_ACTIONS = stateObs.getAvailableActions().size();
		random = new Random();

	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		return ACTIONS.ACTION_USE;
	}

}
