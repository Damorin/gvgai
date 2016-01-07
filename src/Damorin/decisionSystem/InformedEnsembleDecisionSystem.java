package Damorin.decisionSystem;

import tools.ElapsedCpuTimer;
import Damorin.model.WorldInformation;
import Damorin.model.WorldInformationImpl;
import Damorin.voices.Opinion;
import Damorin.voices.Voice;
import Damorin.voices.longRange.SimulatedExplorationVoice;
import Damorin.voices.mediumRange.OpenLoopMCTSVoice;
import Damorin.voices.shortRange.SurvivalVoice;
import core.game.StateObservation;

/**
 * A {@link DecisionSystem} which uses pre-specified {@link Voice}s to make its
 * decisions.
 * 
 * @author Damien Anderson (Damorin)
 *
 */
public class InformedEnsembleDecisionSystem implements DecisionSystem {

	private Voice longRangeVoice;
	private Voice midRangeVoice;
	private Voice shortRangeVoice;

	private WorldInformation worldInformation;

	/**
	 * Constructor for the {@link InformedEnsembleDecisionSystem} which
	 * initialises the {@link Voice}s for decision making.
	 * 
	 * @param stateObs
	 *            The {@link StateObservation} to initialise {@link Voice}s with
	 * @param elapsedTimer
	 *            The {@link ElapsedCpuTimer}
	 */
	public InformedEnsembleDecisionSystem(StateObservation stateObs,
			ElapsedCpuTimer elapsedTimer) {

		if (stateObs == null) {
			throw new IllegalArgumentException();
		}

		worldInformation = new WorldInformationImpl(stateObs, elapsedTimer);
		initialiseVoices(stateObs);
	}

	private void initialiseVoices(StateObservation stateObs) {
		midRangeVoice = new OpenLoopMCTSVoice(stateObs);
		shortRangeVoice = new SurvivalVoice(stateObs);
		longRangeVoice = new SimulatedExplorationVoice(stateObs);
	}

	@Override
	public void update(StateObservation stateObs) {
		worldInformation.update(stateObs);

		longRangeVoice.update(stateObs);
		midRangeVoice.update(stateObs);
		shortRangeVoice.update(stateObs);
	}

	@Override
	public int selectAction(ElapsedCpuTimer elapsedTimer) {
		if (elapsedTimer.elapsedMillis() > 20) {
			return midRangeVoice.askOpinion(elapsedTimer, worldInformation)
					.getAction();
		}

		Opinion shortOpinion = shortRangeVoice.askOpinion(elapsedTimer,
				worldInformation);
		if (shortOpinion.getUrgency() == 10.0) {
			worldInformation.reset();
			return shortOpinion.getAction();
		}

		longRangeVoice.askOpinion(elapsedTimer, worldInformation);
		Opinion midOpinion = midRangeVoice.askOpinion(elapsedTimer,
				worldInformation);
		worldInformation.reset();
		return midOpinion.getAction();
	}

}
