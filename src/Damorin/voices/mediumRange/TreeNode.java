package Damorin.voices.mediumRange;

import java.util.Random;

import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;
import Damorin.Agent;
import Damorin.model.WorldInformation;
import core.game.StateObservation;

/**
 * Represents an individual node of the MCTS Tree.
 * 
 * This particular algorithm uses UCT (Upper Constraint bound for Trees) to
 * select the next node for expansion.
 * 
 * The design is heavily inspired from the SimpleMCTS model developed by the
 * GVG-AI competition team and the enhanced version used for the Shmokin agent
 * from the 2014 competition by Blaine Ross.
 * 
 * @author Damien Anderson (Damorin)
 *
 */
public class TreeNode {

	private static final int MAX_DEPTH = 15;
	private static final double EPSILON = 1e-6;
	private static final double HUGE_NUMBER = 10000000.0;
	private static double[] bounds = new double[] { Double.MAX_VALUE,
			-Double.MAX_VALUE };

	private TreeNode parent;
	private TreeNode[] children;
	private StateObservation state;
	private Random random;
	private double value;
	private int depth;
	private int visits;
	private WorldInformation worldInformation;

	public TreeNode(Random random) {
		this(null, null, random);
	}

	public TreeNode(StateObservation stateObs, TreeNode parent, Random random) {
		this.state = stateObs;
		this.parent = parent;
		this.children = new TreeNode[Agent.availableActions.length];
		this.random = random;
		this.value = 0.0;

		setDepth();
	}

	public void performMcts(ElapsedCpuTimer timer,
			WorldInformation worldInformation) {

		this.worldInformation = worldInformation;

		double avgTimeTaken = 0;
		double totalTimeTaken = 0;
		long remaining = timer.remainingTimeMillis();
		int iterations = 0;

		if (this.worldInformation.hasGoalBeenSet()) {
			double goalScore = this.rollOutToGoal();
			backUp(this, goalScore);
		}

		while (remaining > 2 * avgTimeTaken && remaining > 5) {
			ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
			TreeNode selectedNode = selectANode();
			double score = selectedNode.rollOut();

			backUp(selectedNode, score);

			iterations++;
			totalTimeTaken += (elapsedTimerIteration.elapsedMillis());

			avgTimeTaken = totalTimeTaken / iterations;
			remaining = timer.remainingTimeMillis();
		}
	}

	private TreeNode selectANode() {
		TreeNode current = this;

		while (!current.state.isGameOver() && current.depth < MAX_DEPTH) {
			if (current.hasNotExpanded()) {
				return current.expand();
			} else {
				TreeNode next = current.uct();
				current = next;
			}
		}
		return current;
	}

	private TreeNode uct() {

		TreeNode selected = null;
		double bestValue = -Double.MAX_VALUE;
		for (TreeNode child : this.children) {
			double hvVal = child.value;
			double childValue = hvVal / (child.visits + EPSILON);

			double uctValue = childValue
					+ Math.sqrt(2)
					* Math.sqrt(Math.log(this.visits + 1)
							/ (child.visits + EPSILON))
					+ this.random.nextDouble() * EPSILON;

			if (uctValue > bestValue) {
				selected = child;
				bestValue = uctValue;
			}
		}

		if (selected == null) {
			throw new RuntimeException("Warning! returning null: " + bestValue
					+ " : " + this.children.length);
		}

		return selected;
	}

	public int mostVisitedAction() {
		int selected = -1;
		double bestValue = -Double.MAX_VALUE;
		boolean allEqual = true;
		double first = -1;

		for (int i = 0; i < children.length; i++) {

			if (children[i] != null) {
				if (first == -1)
					first = children[i].visits;
				else if (first != children[i].visits) {
					allEqual = false;
				}

				double childValue = children[i].visits;
				childValue = Utils.noise(childValue, EPSILON,
						this.random.nextDouble()); // break ties randomly
				if (childValue > bestValue) {
					bestValue = childValue;
					selected = i;
				}
			}
		}

		if (actionIsValid(selected)) {
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

			if (children[i] != null
					&& children[i].value + random.nextDouble() * EPSILON > bestValue) {
				bestValue = children[i].value;
				selected = i;
			}
		}

		if (actionIsValid(selected)) {
			System.out.println("Unexpected selection!");
			selected = 0;
		}

		return selected;
	}

	private boolean actionIsValid(int selected) {
		return selected == -1;
	}

	private Double rollOutToGoal() {
		StateObservation rollOutState = state.copy();

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

	private Double rollOut() {

		StateObservation rollOutState = state.copy();
		int thisDepth = this.depth;

		while (!isRollOutFinished(rollOutState, thisDepth)) {
			int action = random.nextInt(children.length);
			rollOutState.advance(Agent.availableActions[action]);
			thisDepth++;
		}

		double delta = value(rollOutState);

		if (delta < bounds[0]) {
			bounds[0] = delta;
		}

		if (delta > bounds[1]) {
			bounds[1] = delta;
		}
		return delta;
	}

	private boolean isRollOutFinished(StateObservation nextState, int depth) {
		if (depth >= MAX_DEPTH) {
			return true;
		}

		if (nextState.isGameOver()) {
			return true;
		}
		return false;
	}

	private double value(StateObservation a_gameState) {

		boolean gameOver = a_gameState.isGameOver();
		Types.WINNER win = a_gameState.getGameWinner();
		double rawScore = a_gameState.getGameScore();

		if (gameOver && win == Types.WINNER.PLAYER_LOSES) {
			rawScore += -HUGE_NUMBER;
		}

		if (gameOver && win == Types.WINNER.PLAYER_WINS) {
			rawScore += HUGE_NUMBER;
		}

		return rawScore;
	}

	private void backUp(TreeNode node, double result) {
		TreeNode nodeToPropogate = node;
		while (nodeHasParent(nodeToPropogate)) {
			nodeToPropogate.visits++;
			nodeToPropogate.value += result;
			nodeToPropogate = nodeToPropogate.parent;
		}
	}

	private boolean nodeHasParent(TreeNode nodeToPropogate) {
		return nodeToPropogate != null;
	}

	private boolean hasNotExpanded() {
		for (TreeNode child : this.children) {
			if (child == null) {
				return true;
			}
		}
		return false;
	}

	private TreeNode expand() {

		int bestAction = 0;
		double bestValue = -1;

		for (int i = 0; i < children.length; i++) {
			StateObservation nextState = state.copy();
			nextState.advance(Agent.availableActions[i]);

			double nextDouble = nextState.getGameScore();

			if (nextDouble > bestValue && children[i] == null) {
				bestAction = i;
				bestValue = nextDouble;
			}
		}

		StateObservation returnState = state.copy();
		returnState.advance(Agent.availableActions[bestAction]);

		TreeNode node = new TreeNode(returnState, this, this.random);

		children[bestAction] = node;
		return node;
	}

	private void setDepth() {
		if (this.parent != null) {
			this.depth = this.parent.depth + 1;
		} else {
			this.depth = 0;
		}
	}

	public void setState(StateObservation stateObs) {
		this.state = stateObs;
	}

}
