package Damorin.voices.mediumRange;

import java.util.Random;

import tools.ElapsedCpuTimer;
import Damorin.model.WorldInformation;
import Damorin.voices.Opinion;
import Damorin.voices.Voice;
import core.game.StateObservation;

/**
 * A Voice for mid range decisions. Uses the MCTS algorithm through
 * {@link TreeNode}.
 * 
 * @author Damien Anderson (Damorin)
 *
 */
public class MCTSVoice implements Voice {

	private TreeNode rootNode;
	private Random random;
	private WorldInformation worldInformation;

	public MCTSVoice(StateObservation stateObs) {
		this.random = new Random();
		rootNode = new TreeNode(random);
		rootNode.setState(stateObs);
	}

	@Override
	public Opinion askOpinion(ElapsedCpuTimer elapsedTimer,
			WorldInformation worldInformation) {
		this.worldInformation = worldInformation;
		int suggestedAction = run(elapsedTimer);
		return new Opinion(suggestedAction, 5.0);
	}

	@Override
	public void update(StateObservation stateObs) {
		rootNode = new TreeNode(random);
		rootNode.setState(stateObs);
	}

	@Override
	public WorldInformation getUpdatedWorldInformation() {
		return this.worldInformation;
	}

	private int run(ElapsedCpuTimer elapsedTimer) {
		rootNode.performMcts(elapsedTimer, worldInformation);
		int action = rootNode.bestAction();
		return action;
	}

}
