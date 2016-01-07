package newDamorin.voices.mcts;

import core.game.StateObservation;
import newDamorin.Agent;
import newDamorin.voices.Opinion;
import newDamorin.voices.OpinionImpl;
import newDamorin.voices.Voice;
import tools.ElapsedCpuTimer;

public class MctsVoice implements Voice {
	
	private SingleMCTSPlayer player;

	@Override
	public Opinion askOpinion(StateObservation state, ElapsedCpuTimer elapsedCpuTimer) {
		player = new SingleMCTSPlayer(Agent.random);
		player.init(state);
		return new OpinionImpl(player.run(elapsedCpuTimer));
	}

}
