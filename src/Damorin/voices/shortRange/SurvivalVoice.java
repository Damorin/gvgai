package Damorin.voices.shortRange;

import java.util.List;

import ontology.Types;
import tools.ElapsedCpuTimer;
import Damorin.model.WorldInformation;
import Damorin.voices.Opinion;
import Damorin.voices.Voice;
import core.game.StateObservation;

/**
 * A Short range {@link Voice} which is designed with extremely accurate close
 * range sight for quick survival actions.
 * 
 * @author Damien Anderson (Damorin)
 *
 */
public class SurvivalVoice implements Voice {

	private static final int DEFAULT_ACTION = 0;

	private static final double SHOUT = 10.0;
	private static final double WHISPER = 0.0;

	private StateObservation stateObs;
	private Opinion opinion;

	private WorldInformation worldInformation;

	private double urgency;
	private int bestAction;

	public SurvivalVoice(StateObservation stateObs) {
		this.stateObs = stateObs;
	}

	@Override
	public Opinion askOpinion(ElapsedCpuTimer elapsedTimer,
			WorldInformation worldInformation) {
		this.worldInformation = worldInformation;
		checkUrgency();
		return opinion;
	}

	private void checkUrgency() {
		analyseVicinity();
		opinion = new Opinion(bestAction, urgency);
	}

	private void analyseVicinity() {
		urgency = WHISPER;
		bestAction = DEFAULT_ACTION;
		double bestScore = stateObs.getGameScore();

		List<StateObservation> nextStates = this.worldInformation
				.getImmediateStates();

		for (int action = 0; action < nextStates.size(); action++) {
			if (nextStates.get(action).getGameScore() < stateObs.getGameScore()
					|| nextStates.get(action).getGameWinner() == Types.WINNER.PLAYER_LOSES) {
				urgency = SHOUT;
			} else if (nextStates.get(action).getGameScore() > bestScore) {
				bestScore = nextStates.get(action).getGameScore();
				bestAction = action;
			} else if (nextStates.get(action).getGameScore() == bestScore
					&& nextStates.get(action).getGameWinner() != Types.WINNER.PLAYER_LOSES) {
				bestAction = action;
			}
		}
	}

	@Override
	public void update(StateObservation stateObs) {
		this.stateObs = stateObs;
	}

	@Override
	public WorldInformation getUpdatedWorldInformation() {
		return this.worldInformation;
	}
}
