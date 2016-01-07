package newDamorin.voices;

import core.game.StateObservation;
import tools.ElapsedCpuTimer;

public interface Voice {

	Opinion askOpinion(StateObservation state, ElapsedCpuTimer elapsedCpuTimer);

}
