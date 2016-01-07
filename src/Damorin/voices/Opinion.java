package Damorin.voices;


/**
 * Represents what a {@link Voice} is saying. The action and how urgent the
 * action is are represented here.
 * 
 * @author Damien Anderson (Damorin)
 *
 */
public class Opinion {

	private int action;
	private double urgency;

	public Opinion(int action, double urgency) {
		this.action = action;
		this.urgency = urgency;
	}

	public int getAction() {
		return action;
	}

	public double getUrgency() {
		return urgency;
	}

}
