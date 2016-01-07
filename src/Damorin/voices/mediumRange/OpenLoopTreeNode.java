package Damorin.voices.mediumRange;

import java.util.Random;

import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;
import Damorin.Agent;
import Damorin.model.WorldInformation;
import core.game.StateObservation;

/**
 * This is an experimental version of the MCTS version which uses open-loop
 * scheduling.
 * 
 * This version is based heavily upon the SampleOLMCTS agent provided by the GVG
 * Framework.
 * 
 * @author Damien Anderson (Damorin)
 *
 */
public class OpenLoopTreeNode {
	private final double HUGE_NEGATIVE = -10000000.0;
	private final double HUGE_POSITIVE = 10000000.0;
	private final int MAX_DEPTH = 10;
	private double epsilon = 1e-6;
	private OpenLoopTreeNode parent;
	private OpenLoopTreeNode[] children;
	private double totValue;
	private int nVisits;
	private Random m_rnd;
	private int m_depth;
	private static double[] bounds = new double[] { Double.MAX_VALUE,
			-Double.MAX_VALUE };
	private int childIdx;

	private StateObservation rootState;
	private WorldInformation worldInformation;

	public OpenLoopTreeNode(Random rnd) {
		this(null, -1, rnd);
	}

	public OpenLoopTreeNode(OpenLoopTreeNode parent, int childIdx, Random rnd) {
		this.parent = parent;
		this.m_rnd = rnd;
		children = new OpenLoopTreeNode[Agent.numberOfAvailableActions];
		totValue = 0.0;
		this.childIdx = childIdx;
		if (parent != null)
			m_depth = parent.m_depth + 1;
		else
			m_depth = 0;
	}

	public void mctsSearch(ElapsedCpuTimer elapsedTimer,
			WorldInformation worldInformation) {
		this.worldInformation = worldInformation;

		double avgTimeTaken = 0;
		double acumTimeTaken = 0;
		long remaining = elapsedTimer.remainingTimeMillis();
		int numIters = 0;

		if (this.worldInformation.hasGoalBeenSet()) {
			double goalScore = this.rollOutToGoal(rootState.copy());
			backUp(this, goalScore);
		}

		int remainingLimit = 5;
		while (remaining > 2 * avgTimeTaken && remaining > remainingLimit) {

			StateObservation state = rootState.copy();

			ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
			OpenLoopTreeNode selected = treePolicy(state);
			double delta = selected.rollOut(state);
			backUp(selected, delta);

			numIters++;
			acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
			avgTimeTaken = acumTimeTaken / numIters;
			remaining = elapsedTimer.remainingTimeMillis();
		}
	}

	public OpenLoopTreeNode treePolicy(StateObservation state) {

		OpenLoopTreeNode cur = this;

		while (!state.isGameOver() && cur.m_depth < MAX_DEPTH) {
			if (cur.notFullyExpanded()) {
				return cur.expand(state);

			} else {
				OpenLoopTreeNode next = cur.uct(state);
				cur = next;
			}
		}

		return cur;
	}

	public OpenLoopTreeNode expand(StateObservation state) {

		int bestAction = 0;
		double bestValue = -1;

		for (int i = 0; i < children.length; i++) {
			double x = m_rnd.nextDouble();
			if (x > bestValue && children[i] == null) {
				bestAction = i;
				bestValue = x;
			}
		}

		// Roll the state
		state.advance(Agent.availableActions[bestAction]);

		OpenLoopTreeNode tn = new OpenLoopTreeNode(this, bestAction, this.m_rnd);
		children[bestAction] = tn;
		return tn;
	}

	public OpenLoopTreeNode uct(StateObservation state) {

		OpenLoopTreeNode selected = null;
		double bestValue = -Double.MAX_VALUE;
		for (OpenLoopTreeNode child : this.children) {
			double hvVal = child.totValue;
			double childValue = hvVal / (child.nVisits + this.epsilon);

			childValue = Utils.normalise(childValue, bounds[0], bounds[1]);

			double uctValue = childValue
					+ Math.sqrt(2)
					* Math.sqrt(Math.log(this.nVisits + 1)
							/ (child.nVisits + this.epsilon));

			uctValue = Utils.noise(uctValue, this.epsilon,
					this.m_rnd.nextDouble()); // break ties randomly

			// small sampleRandom numbers: break ties in unexpanded nodes
			if (uctValue > bestValue) {
				selected = child;
				bestValue = uctValue;
			}
		}
		if (selected == null) {
			throw new RuntimeException("Warning! returning null: " + bestValue
					+ " : " + this.children.length + " " + +bounds[0] + " "
					+ bounds[1]);
		}

		// Roll the state:
		state.advance(Agent.availableActions[selected.childIdx]);

		return selected;
	}

	private Double rollOutToGoal(StateObservation rollOutState) {

		for (int step = 0; step < this.worldInformation.getPathToGoal().size(); step++) {
			if (step < MAX_DEPTH) {
				rollOutState.advance(this.worldInformation.getPathToGoal().get(
						step));
			}
		}

		double delta = value(rollOutState);
		if (delta < 0) {
			worldInformation.setGoalValidity(false);
		}

		if (delta < bounds[0]) {
			bounds[0] = delta;
		}

		if (delta > bounds[1]) {
			bounds[1] = delta;
		}
		return delta;
	}

	public double rollOut(StateObservation state) {
		int thisDepth = this.m_depth;

		while (!finishRollout(state, thisDepth)) {

			int action = m_rnd.nextInt(Agent.numberOfAvailableActions);
			state.advance(Agent.availableActions[action]);
			thisDepth++;
		}

		double delta = value(state);

		if (delta < bounds[0])
			bounds[0] = delta;
		if (delta > bounds[1])
			bounds[1] = delta;

		return delta;
	}

	public double value(StateObservation a_gameState) {

		boolean gameOver = a_gameState.isGameOver();
		Types.WINNER win = a_gameState.getGameWinner();
		double rawScore = a_gameState.getGameScore();

		if (gameOver && win == Types.WINNER.PLAYER_LOSES)
			rawScore += HUGE_NEGATIVE;

		if (gameOver && win == Types.WINNER.PLAYER_WINS)
			rawScore += HUGE_POSITIVE;

		return rawScore;
	}

	public boolean finishRollout(StateObservation rollerState, int depth) {
		if (depth >= MAX_DEPTH) // rollout end condition.
			return true;

		if (rollerState.isGameOver()) // end of game
			return true;

		return false;
	}

	public void backUp(OpenLoopTreeNode node, double result) {
		OpenLoopTreeNode n = node;
		while (n != null) {
			n.nVisits++;
			n.totValue += result;
			n = n.parent;
		}
	}

	public int mostVisitedAction() {
		int selected = -1;
		double bestValue = -Double.MAX_VALUE;
		boolean allEqual = true;
		double first = -1;

		for (int i = 0; i < children.length; i++) {

			if (children[i] != null) {
				if (first == -1)
					first = children[i].nVisits;
				else if (first != children[i].nVisits) {
					allEqual = false;
				}

				double childValue = children[i].nVisits;
				childValue = Utils.noise(childValue, this.epsilon,
						this.m_rnd.nextDouble()); // break ties randomly
				if (childValue > bestValue) {
					bestValue = childValue;
					selected = i;
				}
			}
		}

		if (selected == -1) {
			System.out.println("Unexpected selection!");
			selected = 0;
		} else if (allEqual) {
			// If all are equal, we opt to choose for the one with the best Q.
			selected = bestAction();
		}
		return selected;
	}

	public int bestAction() {
		int selected = -1;
		double bestValue = -Double.MAX_VALUE;

		for (int i = 0; i < children.length; i++) {

			if (children[i] != null) {
				// double tieBreaker = m_rnd.nextDouble() * epsilon;
				double childValue = children[i].totValue
						/ (children[i].nVisits + this.epsilon);
				childValue = Utils.noise(childValue, this.epsilon,
						this.m_rnd.nextDouble()); // break ties randomly
				if (childValue > bestValue) {
					bestValue = childValue;
					selected = i;
				}
			}
		}

		if (selected == -1) {
			System.out.println("Unexpected selection!");
			selected = 0;
		}

		return selected;
	}

	public boolean notFullyExpanded() {
		for (OpenLoopTreeNode tn : children) {
			if (tn == null) {
				return true;
			}
		}

		return false;
	}

	public void setState(StateObservation stateObs) {
		this.rootState = stateObs;
	}
}
