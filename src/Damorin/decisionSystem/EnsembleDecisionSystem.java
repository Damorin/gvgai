package Damorin.decisionSystem;

import java.util.ArrayList;
import java.util.List;

import tools.ElapsedCpuTimer;
import Damorin.model.WorldInformation;
import Damorin.model.WorldInformationImpl;
import Damorin.voices.Opinion;
import Damorin.voices.Voice;
import Damorin.voices.longRange.ExplorationVoice;
import Damorin.voices.mediumRange.MCTSVoice;
import Damorin.voices.shortRange.SurvivalVoice;
import core.game.StateObservation;

/**
 * A {@link DecisionSystem} which will use {@link Voice}s for deciding the best
 * Action to use. This particular implementation uses three voices. Short,
 * Medium and Long Range voices.
 * 
 * Voices are treated without any particular priority, their {@link Opinion}s
 * are trusted.
 * 
 * @author Damien Anderson (Damorin)
 *
 */
public class EnsembleDecisionSystem implements DecisionSystem {

	private List<Voice> voices;
	private WorldInformation worldInformation;

	public EnsembleDecisionSystem(StateObservation stateObs,
			ElapsedCpuTimer elapsedTimer) {

		if (stateObs == null) {
			throw new IllegalArgumentException();
		}

		worldInformation = new WorldInformationImpl(stateObs, elapsedTimer);
		initialiseVoices(stateObs);
	}

	private void initialiseVoices(StateObservation stateObs) {
		voices = new ArrayList<>();
		voices.add(new MCTSVoice(stateObs));
		voices.add(new SurvivalVoice(stateObs));
		voices.add(new ExplorationVoice(stateObs));
	}

	@Override
	public void update(StateObservation stateObs) {
		worldInformation.update(stateObs);

		for (Voice voice : voices) {
			voice.update(stateObs);
		}
	}

	@Override
	public int selectAction(ElapsedCpuTimer elapsedTimer) {
		List<Opinion> opinions = new ArrayList<>();
		for (Voice voice : voices) {
			opinions.add(voice.askOpinion(elapsedTimer, worldInformation));
		}

		worldInformation.reset();

		double firstUrgency = opinions.get(0).getUrgency();
		double secondUrgency = opinions.get(1).getUrgency();
		double thirdUrgency = opinions.get(2).getUrgency();

		if (firstUrgency > secondUrgency) {
			if (firstUrgency > thirdUrgency) {
				return opinions.get(0).getAction();
			}
		} else if (secondUrgency > thirdUrgency) {
			return opinions.get(1).getAction();
		} else {
			return opinions.get(2).getAction();
		}

		return opinions.get(0).getAction();
	}
}
